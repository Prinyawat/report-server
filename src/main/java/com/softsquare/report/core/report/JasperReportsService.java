package com.softsquare.report.core.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.apache.catalina.util.URLEncoder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.softsquare.report.core.utils.BeanUtils;
import com.softsquare.report.model.base.ReportModel;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;

@Slf4j
@Service
@Transactional
public class JasperReportsService implements ReportService {

  private FileSystemStorageService storageService;

  private DataSource dataSource;

  private DataSource dataSourceSecond;

  private JdbcTemplate jdbcTemplate;

  private JdbcTemplate jdbcTemplateSecond;

  private final CacheManager cacheManager;

  @Autowired
  public Boolean clearCache() {
    for (String Name : cacheManager.getCacheNames()) {
      log.info("Clear Cache : {}", Name);
      cacheManager.getCache(Name).clear();
    }
    return true;
  };

  @Autowired
  public JasperReportsService(@Qualifier("primary-db") DataSource dataSource,
      @Qualifier("second-db") DataSource dataSourceSecond, FileSystemStorageService storageService,
      CacheManager cacheManager) {
    this.storageService = storageService;
    this.dataSource = dataSource;
    this.dataSourceSecond = dataSourceSecond;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.jdbcTemplateSecond = new JdbcTemplate(dataSourceSecond);
    this.cacheManager = cacheManager;
  }

  @Override
  public ResponseEntity<byte[]> generateReport(ReportModel model, Map<String, Object> params)
      throws JRException, SQLException {

    String name = model.getFileName();
    String inputFileName = model.getReportName();
    String exportType = model.getExportType();

    String FileName = BeanUtils.isNotEmpty(name) ? name : inputFileName;
    HttpHeaders httpHeaders = new HttpHeaders();
    ContentDisposition contentDisposition = ContentDisposition.builder("inline")
        // .filename("FileName ทดสอบ" + getFileExtension(exportType)).build();
        .filename(URLEncoder.DEFAULT.encode(FileName, StandardCharsets.UTF_8)).build();
    httpHeaders.setContentType(MediaType.parseMediaType(getMediaType(exportType)));
    httpHeaders.setContentDisposition(contentDisposition);
    httpHeaders.setAccessControlAllowCredentials(true);
    List<String> exposedHeaders = new ArrayList<String>();
    exposedHeaders.add("Content-Disposition");
    httpHeaders.setAccessControlExposeHeaders(exposedHeaders);

    return ResponseEntity.ok().headers(httpHeaders).body(generate(model, params));
  }

  @Override
  public byte[] generate(ReportModel model, Map<String, Object> params) throws SQLException {
    String module = model.getModule();
    String inputFileName = model.getReportName();
    String exportType = model.getExportType();
    String autoLoadLabel = model.getAutoLoadLabel();

    Path filePath = Paths.get(module, inputFileName);
    String pathReport = filePath.toString();
    log.info("generate : FileName : {}", pathReport);
    byte[] bytes = null;
    Connection connection = null;
    JasperReport jasperReport = null;
    String mainPath = storageService.loadPathFile(module);
    log.info("mainPath : {}", mainPath);
    try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
      // Check if a compiled report exists
      if (storageService.jasperFileExists(pathReport)) {
        jasperReport = (JasperReport) JRLoader.loadObject(storageService.loadJasperFile(pathReport));
      }
      // Compile report from source and save
      else {
        File dir = new File(mainPath);
        File[] files = dir.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.startsWith(inputFileName + "_") && name.endsWith(".jrxml");
          }
        });

        for (File jrxmlfile : files) {
          String subFileName = jrxmlfile.getName().replace(".jrxml", "");
          Path filePathSub = Paths.get(module, subFileName);
          String pathReportSub = filePathSub.toString();
          String jrxmlSub = storageService.loadJrxmlFile(pathReportSub);
          jasperReport = JasperCompileManager.compileReport(jrxmlSub);
          JRSaver.saveObject(jasperReport, storageService.loadJasperFile(pathReportSub));
        }
        String jrxml = storageService.loadJrxmlFile(pathReport);
        jasperReport = JasperCompileManager.compileReport(jrxml);
        // Save compiled report. Compiled report is loaded next time
        JRSaver.saveObject(jasperReport, storageService.loadJasperFile(pathReport));
      }

      String database = getReportDataSource(inputFileName);

      // Map<String, Object> labelMap = autoLoadLabel(database, autoLoadLabel, (String) params.get("lin_id"));
      // for (Map.Entry<String, Object> entry : labelMap.entrySet()) {
      //   params.put(entry.getKey(), entry.getValue());
      // }

      if ("master".equals(database)) {
        connection = dataSource.getConnection();
      } else {
        connection = dataSourceSecond.getConnection();
      }
      params.put("sub_report_path", mainPath);

      // setProperties(exportType, params);

      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, connection);
      bytes = export(jasperPrint, exportType);
    } catch (JRException | IOException e) {
      log.error(e.getMessage(), e);
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
    return bytes;
  }

  private Map<String, Object> autoLoadLabel(final String database, final String programCode,
      final String langCode) {
    try {
      final String key = String.format("%s-%s-%s", database, programCode, langCode);
      return cacheManager.getCache("autoLoadLabel").get(key, new Callable<Map<String, Object>>() {
        @Override
        public Map<String, Object> call() throws Exception {
          log.info(" autoLoadLabel database:{}, programCode:{}, langCode:{}", database, programCode,
              langCode);
          return retriveLabel(database, programCode, langCode);
        }
      });
    } catch (Exception e) {
      log.error(e.getMessage());
      return retriveLabel(database, programCode, langCode);
    }
  }

  private Map<String, Object> retriveLabel(final String database, final String programCode,
      final String langCode) {
    Map<String, Object> labelMap = new HashMap<String, Object>();
    List<Map<String, Object>> list = loadLabel(database, programCode, langCode);
    for (Map<String, Object> map : list) {
      labelMap.put((String) map.get("parameterName"), map.get("labelName"));
    }
    return labelMap;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public byte[] export(final JasperPrint print, String exportType) throws JRException {
    final Exporter exporter;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    switch (exportType.toUpperCase()) {

      case "HTML":
        exporter = new HtmlExporter();
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(out));
        break;

      case "CSV":
        exporter = new JRCsvExporter();
        exporter.setExporterOutput(new SimpleWriterExporterOutput(out));
        break;

      case "XML":
        exporter = new JRXmlExporter();
        exporter.setExporterOutput(new SimpleXmlExporterOutput(out));
        break;

      case "XLSX":
        exporter = new JRXlsxExporter();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        break;

      case "PDF":
        exporter = new JRPdfExporter();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        break;

      case "DOCX":
        exporter = new JRDocxExporter();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        break;

      default:
        throw new JRException("Unknown report format: " + exportType);
    }

    exporter.setExporterInput(new SimpleExporterInput(print));
    exporter.exportReport();

    return out.toByteArray();
  }

  @Override
  public String getFileExtension(String exportType) throws JRException {
    String result;
    switch (exportType.toUpperCase()) {
      case "HTML":
        result = ".html";
        break;

      case "CSV":
        result = ".csv";
        break;

      case "XML":
        result = ".xml";
        break;

      case "XLSX":
        result = ".xlsx";
        break;

      case "PDF":
        result = ".pdf";
        break;

      case "DOCX":
        result = ".docx";
        break;

      default:
        throw new JRException("Unknown report format: " + exportType);
    }

    return result;
  }

  @Override
  public String getMediaType(String exportType) throws JRException {
    String result;
    switch (exportType.toUpperCase()) {
      case "HTML":
        result = "text/html";
        break;

      case "CSV":
        result = "application/csv";
        break;

      case "XML":
        result = "application/xml";
        break;

      case "XLSX":
        result = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        break;

      case "PDF":
        result = "application/pdf";
        break;

      case "DOCX":
        result = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        break;

      default:
        throw new JRException("Unknown report format: " + exportType);
    }

    return result;
  }

  private List<Map<String, Object>> loadLabel(String database, String programCode,
      String langCode) {
    StringBuilder sql = new StringBuilder();
    sql.append(" SELECT 'L_' || srl.field_name AS \"parameterName\", srl.label_name \"labelName\" ");
    sql.append(" FROM su_report_label srl ");
    sql.append(" WHERE (srl.program_code = ? or srl.program_code = 'ALL') ");
    sql.append(" AND lang_code = cast(? AS lang) ");
    // sql.append(" SELECT 'param' AS parameterName, 'label' AS labelName ");

    if ("replica".equals(database)) {
      return this.jdbcTemplateSecond.queryForList(sql.toString(), programCode, langCode);
    }

    return this.jdbcTemplate.queryForList(sql.toString(), programCode, langCode);
  }

  private String getReportDataSource(final String reportName) {
    try {
      final String key = reportName;
      return cacheManager.getCache("reportDatasource").get(key, () -> {
        log.info(" reportDatasource reportName:{}", reportName);
        return retriveReportDatasource(reportName);
      });
    } catch (Exception e) {
      log.error(e.getMessage());
      return retriveReportDatasource(reportName);
    }
  }

  private String retriveReportDatasource(final String reportName) {
    StringBuilder sql = new StringBuilder();
    // sql.append(" SELECT srd.datasource ");
    // sql.append(" FROM su_report_datasource srd ");
    // sql.append(" WHERE srd.report_name = ? ");
    // sql.append(" SELECT 1 ");

    try {
      // String datasource = this.jdbcTemplate.queryForObject(sql.toString(), new
      // Object[] { reportName },
      // String.class);
      // return datasource == null || "replica".equals(datasource) ? "replica" :
      // "master";
      return "master";
    } catch (Exception ex) {
      return "master";
    }
  }

  public List<Map<String, Object>> checkGuestReport(String reportName) {

    String database = getReportDataSource(reportName);

    StringBuilder sql = new StringBuilder();
    sql.append(" SELECT count(1) AS check ");
    sql.append(" FROM db_list_item ");
    sql.append(
        " WHERE (list_item_group_code = 'AdmissionReport' or  list_item_group_code ='AnnounceReport') ");
    sql.append("     AND list_item_code = ? AND attibute1 = 'Guest'");

    if ("replica".equals(database)) {
      return this.jdbcTemplateSecond.queryForList(sql.toString(), reportName);
    }

    return this.jdbcTemplate.queryForList(sql.toString(), reportName);
  }

  public String generateErrorResult(Exception e, ReportModel model,
      Map<String, Object> parameterMap) throws Exception {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    e.printStackTrace(pw);
    try {
      StringBuilder insertSql = new StringBuilder();
      insertSql.append(
          "insert into su_error_log (error_message , error_stack_trace , log_date , log_time , company_code , created_by , created_program) ");
      insertSql.append("values(?, ?, now(), now(), ?, ?, ?) RETURNING error_id");
      connection = dataSource.getConnection();
      statement = connection.prepareStatement(insertSql.toString());
      statement.setString(1, e.getMessage());
      statement.setString(2, sw.getBuffer().toString());
      statement.setString(3, String.valueOf(parameterMap.get("company_code")));
      statement.setString(4, String.valueOf(parameterMap.get("user_name")));
      statement.setString(5, model.getReportName());
      resultSet = statement.executeQuery();
      resultSet.next();
      long errorId = resultSet.getLong(1);
      String errorMessage = this.getErrorMessageTemplate(String.valueOf(parameterMap.get("lin_id")),
          String.valueOf(errorId));
      log.info("Error Message => {} ", errorMessage);
      if (("XLSX").equals(model.getExportType().toUpperCase())) {
        String result = new String(this.generateErrorExcel(errorMessage));
        return "data:" + this.getMediaType("XLSX") + ";base64," + result;
      } else {
        return new String(this.generateErrorPdf(errorMessage));
      }
    } catch (Exception ex) {
      log.error("ERROR DURING EXCEPTION CATCHING => " + ex.getMessage(), ex);
      throw e;
    } finally {
      pw.close();
      try {
        sw.close();
        if (resultSet != null) {
          resultSet.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (IOException ioe) {
      } catch (SQLException se) {
      }
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException se) {
      }
    }
  }

  public byte[] generateErrorExcel(String message) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Error");
    int rowNum = 0;
    Row row = sheet.createRow(rowNum++);
    int colNum = 0;
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 20);
    XSSFCellStyle style = workbook.createCellStyle();
    style.setFont(font);
    Cell cell = row.createCell(colNum++);
    cell.setCellStyle(style);
    cell.setCellValue(message);
    workbook.write(bos);
    workbook.close();
    return Base64.getEncoder().encode(bos.toByteArray());
  }

  public byte[] generateErrorPdf(String message) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (PDDocument doc = new PDDocument()) {
      PDPage myPage = new PDPage();
      PDFont font = PDType0Font.load(doc,
          getClass().getClassLoader().getResourceAsStream("THSarabun Bold.ttf"));
      doc.addPage(myPage);
      try (PDPageContentStream cont = new PDPageContentStream(doc, myPage)) {
        cont.beginText();
        cont.setFont(font, 20);
        cont.setLeading(14.5f);
        cont.newLineAtOffset(25, 700);
        cont.showText(message);
        cont.newLine();
        cont.endText();
      }
      doc.save(bos);
    }
    return Base64.getEncoder().encode(bos.toByteArray());
  }

  private String getErrorMessageTemplate(String langCode, String errorId) {
    String language = langCode;
    if (BeanUtils.isEmpty(language)) {
      language = "th";
    }
    StringBuilder sql = new StringBuilder();
    sql.append(" select replace(sm.message_desc , '{{0}}' , ?) as message_desc  ");
    sql.append(" from su_message sm  ");
    sql.append(" WHERE sm.message_code = ? ");
    sql.append(" 	AND sm.lang_code = cast(? AS lang) ");
    List<Map<String, Object>> result = this.jdbcTemplate.queryForList(sql.toString(), errorId, "STD00042", language);
    if (BeanUtils.isNotEmpty(result) && result.size() > 0) {
      return String.valueOf(result.get(0).get("message_desc"));
    } else {
      return "An error occurs during process with error code " + errorId
          + ". Please contact administrator.";
    }
  }
}

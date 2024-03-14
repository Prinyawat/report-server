package com.softsquare.report.core.report;

import lombok.extern.slf4j.Slf4j;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiContext;
import org.jxls.transform.poi.PoiTransformer;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.softsquare.report.core.utils.BeanUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Slf4j
@Component
public class ExcelReportsService {

    public ResponseEntity<Object> generateReport(String reportName, String startPoint, Map<String, Object> params) throws Exception {
        if (BeanUtils.isEmpty(reportName) || BeanUtils.isEmpty(startPoint)) {
            log.error("Empty reportName or startPoint");
            return ResponseEntity.badRequest().body("Empty reportName or startPoint");
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        reportName += ".xlsx";
        ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(reportName).build();
        httpHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        httpHeaders.setContentDisposition(contentDisposition);
        httpHeaders.setAccessControlAllowCredentials(true);
        List<String> exposedHeaders = new ArrayList<String>();
        exposedHeaders.add("Content-Disposition");
        httpHeaders.setAccessControlExposeHeaders(exposedHeaders);
        return ResponseEntity.ok().headers(httpHeaders).body(generate(reportName, startPoint, params));
    }

    private byte[] generate(String reportName, String startPoint, Map<String, Object> params) throws Exception {
        log.info("Running Generate Excel Reports Service");
        byte[] result;
        try (InputStream is = new FileInputStream(ResourceUtils.getFile("classpath:reports/" + reportName))) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PoiTransformer transformer = PoiTransformer.createTransformer(is, os);
                AreaBuilder areaBuilder = new XlsCommentAreaBuilder(transformer);
                List<Area> xlsAreaList = areaBuilder.build();
                Area xlsArea = xlsAreaList.get(0);
                Context context = new PoiContext();
                for (Entry<String, Object> entry : params.entrySet()) {
                    context.putVar(entry.getKey(), entry.getValue());
                }
                xlsArea.applyAt(new CellRef(startPoint), context);
                xlsArea.processFormulas();
                result = write(os, transformer);
            }
        }
        log.info("Success");
        return result;
    }

    private byte[] write(ByteArrayOutputStream os, PoiTransformer transformer) throws IOException {
        if (os == null) {
            throw new IllegalStateException("Cannot write a workbook with an uninitialized output stream");
        }
        if (transformer.getWorkbook() == null) {
            throw new IllegalStateException("Cannot write an uninitialized workbook");
        }
        transformer.getWorkbook().write(os);
        byte[] result = os.toByteArray();
        os.close();
        transformer.dispose();
        return result;
    }
}

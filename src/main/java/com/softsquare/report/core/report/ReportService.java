package com.softsquare.report.core.report;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.softsquare.report.model.base.ReportModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

public interface ReportService {

    ResponseEntity<byte[]> generateReport(ReportModel model, Map<String, Object> params) throws JRException, SQLException;
    
    Boolean clearCache();

    byte[] generate(ReportModel model, Map<String, Object> params) throws SQLException;

    byte[] export(JasperPrint print, String exportType) throws JRException;

    String getFileExtension(String exportType) throws JRException;

    String getMediaType(String exportType) throws JRException;
    
    String generateErrorResult(Exception e, ReportModel model, Map<String, Object> parameterMap) throws Exception;
    
    byte[] generateErrorExcel(String message) throws IOException ;
    
    byte[] generateErrorPdf(String message) throws IOException;
    
}

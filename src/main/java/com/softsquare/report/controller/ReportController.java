package com.softsquare.report.controller;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.softsquare.report.core.ip.IpService;
import com.softsquare.report.core.report.JasperReportsService;
import com.softsquare.report.core.utils.BeanUtils;
import com.softsquare.report.model.base.ReportModel;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;

@Slf4j
@RequestMapping(path = "/report")
@CrossOrigin
@RestController
public class ReportController {

	@Autowired
	private JasperReportsService reportService;
	
	@SuppressWarnings({ "unchecked" })
	@PostMapping(path = "/exportReport")
	public ResponseEntity<byte[]> exportReport(@AuthenticationPrincipal Jwt principal, @RequestBody ReportModel model,
			HttpServletRequest request) throws FileNotFoundException, JRException, SQLException {
		log.info("exportReport Controller :: Param : {}", model);
		log.info("userName : {}", SecurityContextHolder.getContext().getAuthentication());
		ObjectMapper oMapper = new ObjectMapper();
		Map<String, Object> parameterMap = (Map<String, Object>) oMapper.convertValue(model.getParamsJson(), Map.class);
		parameterMap.put("user_name", principal.getClaimAsString("name"));
		parameterMap.put("export_type", model.getExportType());
		parameterMap.put("root_report_path", ResourceUtils.getFile("classpath:reports").getAbsolutePath());

		if (BeanUtils.isNull(parameterMap.get("ip_address"))
				|| BeanUtils.isEmpty(parameterMap.get("ip_address").toString())) {
			String ipAddress = IpService.getClientIpAddress(request);
			parameterMap.put("ip_address", ipAddress);
		}

		log.info("exportReport Controller :: parameterMap : {}", parameterMap);
		return reportService.generateReport(model, parameterMap);
	}

	@PostMapping(path = "/clearCache")
	public Boolean clearCache(@RequestBody ReportModel model) throws Exception {
		return reportService.clearCache();
	}

	@SuppressWarnings("unchecked")
	@PostMapping(path = "/exportReportBase64")
	public String exportReportBase64(@AuthenticationPrincipal Jwt principal, @RequestBody ReportModel model,
			HttpServletRequest request) throws FileNotFoundException, JRException, SQLException, Exception {

		log.info("exportReport Controller :: Param : {}", model);
		ObjectMapper oMapper = new ObjectMapper();
		Map<String, Object> parameterMap = (Map<String, Object>) oMapper.convertValue(model.getParamsJson(), Map.class);
		parameterMap.put("user_name", principal.getClaimAsString("name"));
		parameterMap.put("export_type", model.getExportType());
		parameterMap.put("root_report_path", ResourceUtils.getFile("classpath:reports").getAbsolutePath());

		if (BeanUtils.isNull(parameterMap.get("ip_address"))
				|| BeanUtils.isEmpty(parameterMap.get("ip_address").toString())) {
			String ipAddress = IpService.getClientIpAddress(request);
			parameterMap.put("ip_address", ipAddress);
		}
		try {
			byte[] encoded = Base64.getEncoder().encode(reportService.generate(model, parameterMap));
			String result = new String(encoded);

			// Fix bug csv file, It have a bom character
			if (("CSV").equals(model.getExportType().toUpperCase())) {
				result = removeUTF8BOM(result);
			}

			if (("XLSX").equals(model.getExportType().toUpperCase())
					|| ("CSV").equals(model.getExportType().toUpperCase())
					|| ("DOCX").equals(model.getExportType().toUpperCase())) {
				result = "data:" + reportService.getMediaType(model.getExportType().toUpperCase()) + ";base64,"
						+ result;
			}
			
			return result;
		} catch (Exception e) {
			return this.reportService.generateErrorResult(e, model, parameterMap);
		}
	}

	public static final String UTF8_BOM = "77u/";

	private static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM) && s.length() == 4) {
			s = s.substring(4);
		}
		return s;
	}
}
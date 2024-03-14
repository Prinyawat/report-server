package com.softsquare.report.controller;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.softsquare.report.core.ip.IpService;
import com.softsquare.report.core.report.JasperReportsService;
import com.softsquare.report.core.utils.BeanUtils;
import com.softsquare.report.model.base.ReportModel;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;

//import com.softsquare.spa.model.cs.*;

@Slf4j
@RequestMapping(path = "/report/guest")
@CrossOrigin
@RestController
public class GuestController {

	@Autowired
	private JasperReportsService reportService;

	@SuppressWarnings({ "unchecked" })
	@PostMapping(path = "/exportReport")
	public ResponseEntity<byte[]> exportReport(@RequestBody ReportModel model, HttpServletRequest request)
			throws FileNotFoundException, JRException, SQLException {
		log.info("exportReport Controller :: Param : {}", model);
		ObjectMapper oMapper = new ObjectMapper();
		Map<String, Object> parameterMap = (Map<String, Object>) oMapper.convertValue(model.getParamsJson(), Map.class);
		parameterMap.put("export_type", model.getExportType());
		parameterMap.put("root_report_path", ResourceUtils.getFile("classpath:reports").getAbsolutePath());

		if (BeanUtils.isNull(parameterMap.get("ip_address"))
				|| BeanUtils.isEmpty(parameterMap.get("ip_address").toString())) {
			String ipAddress = IpService.getClientIpAddress(request);
			parameterMap.put("ip_address", ipAddress);
		}
		log.info("exportReport Controller :: parameterMap : {}", parameterMap);
		List<Map<String, Object>> list = reportService.checkGuestReport(model.getReportName());
		if (Integer.parseInt(list.get(0).get("check").toString()) > 0) {
			return reportService.generateReport(model, parameterMap);
		} else {
			return new ResponseEntity<byte[]>(null, null, HttpStatus.UNAUTHORIZED);
		}
	}

	@GetMapping(path = "/clearCache")
	public Boolean clearCache() throws Exception {
		return reportService.clearCache();
	}
}
package com.softsquare.report.model.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ReportModel extends BaseModel {

    private Object paramsJson;
    private String module;
    private String reportName;
    private String exportType;
    private String autoLoadLabel;
    private String fileName;
}

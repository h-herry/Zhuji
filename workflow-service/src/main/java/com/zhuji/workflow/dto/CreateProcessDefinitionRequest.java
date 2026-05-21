package com.zhuji.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateProcessDefinitionRequest {

    @NotBlank(message = "流程编码不能为空")
    private String processCode;

    @NotBlank(message = "流程名称不能为空")
    private String processName;

    private String description;

    private String processXml;

    public CreateProcessDefinitionRequest() {
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProcessXml() {
        return processXml;
    }

    public void setProcessXml(String processXml) {
        this.processXml = processXml;
    }
}
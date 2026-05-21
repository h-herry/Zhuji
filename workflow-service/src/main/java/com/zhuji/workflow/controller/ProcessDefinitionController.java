package com.zhuji.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.workflow.service.FlowableProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "流程定义管理", description = "Flowable流程定义管理接口")
@RestController
@RequestMapping("/api/v1/workflow/definition")
public class ProcessDefinitionController {

    private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionController.class);

    private final FlowableProcessService flowableProcessService;
    private final ObjectMapper objectMapper;

    public ProcessDefinitionController(FlowableProcessService flowableProcessService, ObjectMapper objectMapper) {
        this.flowableProcessService = flowableProcessService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "分页查询流程定义列表")
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String processKey) {
        List<Map<String, Object>> definitions = flowableProcessService.listProcessDefinitions(page, size);
        return ApiResponse.success(definitions);
    }

    @Operation(summary = "根据ID查询流程定义")
    @GetMapping("/{processDefinitionId}")
    public ApiResponse<ObjectNode> getById(@PathVariable String processDefinitionId) {
        org.flowable.engine.repository.ProcessDefinition definition =
                flowableProcessService.getProcessDefinition(processDefinitionId);

        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", definition.getId());
        result.put("key", definition.getKey());
        result.put("name", definition.getName());
        result.put("version", definition.getVersion());
        result.put("deploymentId", definition.getDeploymentId());
        result.put("suspended", definition.isSuspended());

        return ApiResponse.success(result);
    }

    @Operation(summary = "获取流程定义的BPMN XML")
    @GetMapping("/{processDefinitionId}/xml")
    public ApiResponse<String> getProcessXml(@PathVariable String processDefinitionId) {
        String xml = flowableProcessService.getProcessDefinitionBpmnXml(processDefinitionId);
        return ApiResponse.success(xml);
    }

    @Operation(summary = "部署流程定义")
    @PostMapping("/deploy")
    public ApiResponse<String> deployProcess(
            @RequestParam String processName,
            @RequestParam String bpmnXml) {
        String deploymentId = flowableProcessService.deployProcess(processName, bpmnXml);
        return ApiResponse.success(deploymentId);
    }

    @Operation(summary = "获取流程定义的JSON结构")
    @GetMapping("/{processDefinitionId}/json")
    public ApiResponse<ObjectNode> getProcessJson(@PathVariable String processDefinitionId) {
        ObjectNode json = flowableProcessService.convertProcessDefinitionToJson(processDefinitionId);
        return ApiResponse.success(json);
    }

    @Operation(summary = "挂起流程定义")
    @PostMapping("/{processDefinitionId}/suspend")
    public ApiResponse<Void> suspend(@PathVariable String processDefinitionId) {
        flowableProcessService.suspendProcessDefinition(processDefinitionId);
        return ApiResponse.success();
    }

    @Operation(summary = "激活流程定义")
    @PostMapping("/{processDefinitionId}/activate")
    public ApiResponse<Void> activate(@PathVariable String processDefinitionId) {
        flowableProcessService.activateProcessDefinition(processDefinitionId);
        return ApiResponse.success();
    }
}
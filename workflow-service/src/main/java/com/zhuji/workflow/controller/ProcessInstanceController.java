package com.zhuji.workflow.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.workflow.dto.CompleteTaskRequest;
import com.zhuji.workflow.dto.StartProcessRequest;
import com.zhuji.workflow.service.FlowableProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "流程实例管理", description = "Flowable流程实例管理接口")
@RestController
@RequestMapping("/api/v1/workflow/instance")
public class ProcessInstanceController {

    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceController.class);

    private final FlowableProcessService flowableProcessService;

    public ProcessInstanceController(FlowableProcessService flowableProcessService) {
        this.flowableProcessService = flowableProcessService;
    }

    @Operation(summary = "分页查询流程实例列表")
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String processDefinitionKey) {
        List<Map<String, Object>> instances = flowableProcessService.listProcessInstances(page, size, processDefinitionKey);
        return ApiResponse.success(instances);
    }

    @Operation(summary = "启动流程实例")
    @PostMapping("/start")
    public ApiResponse<Map<String, Object>> startProcess(@RequestBody StartProcessRequest request) {
        Map<String, Object> variables = new HashMap<>();
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }

        variables.put("startUserId", request.getStartUserId());
        variables.put("startUserName", request.getStartUserName());

        String instanceId;
        if (request.getBusinessKey() != null && !request.getBusinessKey().isEmpty()) {
            instanceId = flowableProcessService.startProcessInstanceWithBusinessKey(
                    request.getProcessCode(),
                    request.getBusinessKey(),
                    variables
            );
        } else {
            instanceId = flowableProcessService.startProcessInstance(
                    request.getProcessCode(),
                    variables
            );
        }

        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", instanceId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "删除流程实例")
    @DeleteMapping("/{processInstanceId}")
    public ApiResponse<Void> deleteProcessInstance(
            @PathVariable String processInstanceId,
            @RequestParam(defaultValue = "用户删除") String reason) {
        flowableProcessService.deleteProcessInstance(processInstanceId, reason);
        return ApiResponse.success();
    }

    @Operation(summary = "获取流程实例变量")
    @GetMapping("/{processInstanceId}/variables")
    public ApiResponse<List<Map<String, Object>>> getProcessInstanceVariables(
            @PathVariable String processInstanceId) {
        List<Map<String, Object>> variables = flowableProcessService.getProcessInstanceVariables(processInstanceId);
        return ApiResponse.success(variables);
    }

    @Operation(summary = "分页查询任务列表")
    @GetMapping("/task/list")
    public ApiResponse<List<Map<String, Object>>> listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String processDefinitionKey) {
        List<Map<String, Object>> tasks = flowableProcessService.listTasks(page, size, assignee, processDefinitionKey);
        return ApiResponse.success(tasks);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/task/{taskId}")
    public ApiResponse<Map<String, Object>> getTask(@PathVariable String taskId) {
        Task task = flowableProcessService.getTask(taskId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("name", task.getName());
        result.put("description", task.getDescription());
        result.put("assignee", task.getAssignee());
        result.put("processInstanceId", task.getProcessInstanceId());
        result.put("processDefinitionKey", task.getProcessDefinitionId());
        result.put("taskDefinitionKey", task.getTaskDefinitionKey());
        result.put("priority", task.getPriority());
        result.put("createTime", task.getCreateTime());
        result.put("dueDate", task.getDueDate());

        return ApiResponse.success(result);
    }

    @Operation(summary = "获取任务变量")
    @GetMapping("/task/{taskId}/variables")
    public ApiResponse<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        Map<String, Object> variables = flowableProcessService.getTaskVariables(taskId);
        return ApiResponse.success(variables);
    }

    @Operation(summary = "完成任务")
    @PostMapping("/task/{taskId}/complete")
    public ApiResponse<Void> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {
        flowableProcessService.completeTask(taskId, variables);
        return ApiResponse.success();
    }

    @Operation(summary = "转派任务")
    @PostMapping("/task/{taskId}/delegate")
    public ApiResponse<Void> delegateTask(
            @PathVariable String taskId,
            @RequestParam String userId) {
        flowableProcessService.delegateTask(taskId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "设置任务受理人")
    @PostMapping("/task/{taskId}/assignee")
    public ApiResponse<Void> setTaskAssignee(
            @PathVariable String taskId,
            @RequestParam String userId) {
        flowableProcessService.setTaskAssignee(taskId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "设置任务优先级")
    @PostMapping("/task/{taskId}/priority")
    public ApiResponse<Void> setTaskPriority(
            @PathVariable String taskId,
            @RequestParam int priority) {
        flowableProcessService.setTaskPriority(taskId, priority);
        return ApiResponse.success();
    }
}
package com.zhuji.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FlowableProcessService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final ProcessEngine processEngine;
    private final ObjectMapper objectMapper;

    public FlowableProcessService(RepositoryService repositoryService,
                                 RuntimeService runtimeService,
                                 TaskService taskService,
                                 ProcessEngine processEngine,
                                 ObjectMapper objectMapper) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.processEngine = processEngine;
        this.objectMapper = objectMapper;
    }

    public String deployProcess(String processName, String bpmnXml) {
        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(processName)
                .key(processName)
                .addString(processName + ".bpmn20.xml", bpmnXml)
                .tenantId("zhuji");

        Deployment deployment = builder.deploy();
        return deployment.getId();
    }

    public String deployProcessFromInputStream(String processName, InputStream inputStream) {
        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(processName)
                .key(processName)
                .addInputStream(processName + ".bpmn20.xml", inputStream)
                .tenantId("zhuji");

        Deployment deployment = builder.deploy();
        return deployment.getId();
    }

    public List<Map<String, Object>> listProcessDefinitions(int page, int size) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey()
                .asc();

        long total = query.count();
        List<ProcessDefinition> definitions = query.listPage(page * size, size);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessDefinition def : definitions) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", def.getId());
            map.put("key", def.getKey());
            map.put("name", def.getName());
            map.put("version", def.getVersion());
            map.put("deploymentId", def.getDeploymentId());
            map.put("suspended", def.isSuspended());
            map.put("total", total);
            result.add(map);
        }
        return result;
    }

    public ProcessDefinition getProcessDefinition(String processDefinitionId) {
        ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinitionId);
        if (definition == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("workflow.definition.not.found"));
        }
        return definition;
    }

    public String getProcessDefinitionBpmnXml(String processDefinitionId) {
        InputStream inputStream = repositoryService.getProcessModel(processDefinitionId);
        try {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(500, I18nMessageUtil.getMessage("workflow.definition.xml.read.error"));
        }
    }

    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
    }

    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
    }

    public String startProcessInstance(String processDefinitionKey, Map<String, Object> variables) {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
        return instance.getId();
    }

    public String startProcessInstanceWithBusinessKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        ProcessInstance instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(processDefinitionKey)
                .businessKey(businessKey)
                .variables(variables)
                .start();
        return instance.getId();
    }

    public List<Map<String, Object>> listProcessInstances(int page, int size, String processDefinitionKey) {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

        if (processDefinitionKey != null && !processDefinitionKey.isEmpty()) {
            query.processDefinitionKey(processDefinitionKey);
        }

        long total = query.count();
        List<ProcessInstance> instances = query.listPage(page * size, size);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessInstance inst : instances) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", inst.getId());
            map.put("processDefinitionId", inst.getProcessDefinitionId());
            map.put("processDefinitionKey", inst.getProcessDefinitionKey());
            map.put("businessKey", inst.getBusinessKey());
            map.put("suspended", inst.isSuspended());
            map.put("total", total);
            result.add(map);
        }
        return result;
    }

    public void deleteProcessInstance(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }

    public List<Map<String, Object>> listTasks(int page, int size, String assignee, String processDefinitionKey) {
        TaskQuery query = taskService.createTaskQuery();

        if (assignee != null && !assignee.isEmpty()) {
            query.taskAssignee(assignee);
        }

        if (processDefinitionKey != null && !processDefinitionKey.isEmpty()) {
            query.processDefinitionKey(processDefinitionKey);
        }

        long total = query.count();
        List<Task> tasks = query.orderByTaskCreateTime().desc().listPage(page * size, size);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("name", task.getName());
            map.put("description", task.getDescription());
            map.put("assignee", task.getAssignee());
            map.put("processInstanceId", task.getProcessInstanceId());
            map.put("processDefinitionKey", task.getProcessDefinitionId() != null ?
                repositoryService.getProcessDefinition(task.getProcessDefinitionId()).getKey() : null);
            map.put("taskDefinitionKey", task.getTaskDefinitionKey());
            map.put("priority", task.getPriority());
            map.put("createTime", task.getCreateTime());
            map.put("dueDate", task.getDueDate());
            map.put("total", total);
            result.add(map);
        }
        return result;
    }

    public Task getTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("workflow.task.not.found"));
        }
        return task;
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        if (variables != null && !variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
    }

    public void delegateTask(String taskId, String userId) {
        taskService.delegateTask(taskId, userId);
    }

    public void setTaskAssignee(String taskId, String userId) {
        taskService.setAssignee(taskId, userId);
    }

    public void setTaskPriority(String taskId, int priority) {
        taskService.setPriority(taskId, priority);
    }

    public List<Map<String, Object>> getProcessInstanceVariables(String processInstanceId) {
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .list();

        if (executions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Object> variables = runtimeService.getVariables(executions.get(0).getId());
        return variables.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", e.getKey());
                    map.put("value", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public ObjectNode convertProcessDefinitionToJson(String processDefinitionId) {
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);

        ObjectNode json = objectMapper.createObjectNode();
        json.put("id", model.getMainProcess().getId());
        json.put("name", model.getMainProcess().getName());

        ObjectNode tasksNode = json.putObject("tasks");
        for (org.flowable.bpmn.model.FlowElement element : model.getMainProcess().getFlowElements()) {
            if (element instanceof UserTask) {
                UserTask userTask = (UserTask) element;
                ObjectNode taskNode = tasksNode.putObject(userTask.getId());
                taskNode.put("name", userTask.getName());
                taskNode.put("assignee", userTask.getAssignee() != null ? userTask.getAssignee() : "");
                taskNode.put("formKey", userTask.getFormKey() != null ? userTask.getFormKey() : "");
            }
        }

        return json;
    }
}
package com.my.sample.activiti.activitisample.controller;


import org.activiti.engine.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HolidayReleaseProcessController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value = "/holiday-release-process/{userId}/{noOfDays}", method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> startHolidayReleaseProcess(@PathVariable String userId, @PathVariable Integer noOfDays){

        identityService.setAuthenticatedUserId(userId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("noofdays", noOfDays);
        variables.put("owner", userId);

        ProcessEngine processEngine
                = ProcessEngines.getDefaultProcessEngine();

        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("process", userId, variables);

        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId())
                .list();

        Map<String,String> res = new HashMap<>();
        tasks.forEach(s -> res.put(s.getProcessInstanceId(),s.getName()));

        return res;
    }

    @RequestMapping(value = "/holiday-release-process/{userId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getUsersProcessesAndCurrentTasks(@PathVariable String userId){

        identityService.setAuthenticatedUserId(userId);
        ProcessEngine processEngine
                = ProcessEngines.getDefaultProcessEngine();

        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().processVariableValueEquals("owner",userId).list();
        Map<String,String> res = new HashMap<>();
        tasks.forEach(s -> res.put(s.getProcessInstanceId(),s.getName()));

        return res;
    }

    @RequestMapping(value = "/holiday-release-process/approval/{managerId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getTasksPendingApproval(@PathVariable String managerId){

        ProcessEngine processEngine
                = ProcessEngines.getDefaultProcessEngine();

        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(managerId).list();
        Map<String,String> res = new HashMap<>();
        tasks.forEach(s -> res.put(s.getProcessInstanceId(),s.getName()));

        return res;
    }

    @RequestMapping(value = "/holiday-release-process/approval/{managerId}/{processId}/{status}", method = RequestMethod.GET,
                   produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> completeTask(@PathVariable String managerId, @PathVariable String processId, @PathVariable String status){

        ProcessEngine processEngine
                = ProcessEngines.getDefaultProcessEngine();

        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(managerId)
                .processInstanceId(processId).list();

        Map<String,String> res = new HashMap<>();
        if(!CollectionUtils.isEmpty(tasks)){
            Map<String, Object> variables = new HashMap<>();
            variables.put("selectedStatus", status);
            taskService.complete(tasks.get(0).getId(),variables);
            res.put(tasks.get(0).getProcessInstanceId(),tasks.get(0).getName());
        }

        return res;
    }


}

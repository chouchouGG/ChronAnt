package cn.uhoc.trigger.http;

import cn.uhoc.domain.convertor.TaskConverter;
import cn.uhoc.domain.manager.model.entity.TaskEntity;
import cn.uhoc.domain.manager.service.ITaskCfgService;
import cn.uhoc.domain.manager.service.ITaskService;
import cn.uhoc.trigger.api.ITaskController;
import cn.uhoc.trigger.api.dto.*;
import cn.uhoc.type.enums.ExceptionStatus;
import cn.uhoc.type.model.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-02 18:16
 **/
@Slf4j
@RestController
@RequestMapping("/task")
// TODO 学习不跨域和跨域的demo实现
public class TaskController implements ITaskController {

    @Resource
    private ITaskService taskService;

    @Resource
    private ITaskCfgService taskCfgService;

    @PostMapping("/create_task")
    @Override
    public R createTask(@RequestBody TaskCreateReq taskCreateReq) {
        if (StringUtils.isNotBlank(taskCreateReq.getTaskType())) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        String taskId = taskService.createTask(taskCreateReq);
        return new R(TaskCreateRes.builder().taskId(taskId));
    }

    @GetMapping("/get_task")
    @Override
    public R<TaskRes> getTask(@RequestParam("taskId") String taskId) {
        if (StringUtils.isNotBlank(taskId)) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        TaskEntity task = taskService.getTask(taskId);
        TaskRes taskDTO = TaskConverter.toDTO(task);
        return new R(taskDTO);
    }

    @GetMapping("/get_task_list")
    @Override
    public R<List<TaskRes>> getTaskList(@RequestParam("taskType") String taskType, @RequestParam("status") int status, @RequestParam("limit") int limit) {
        // fixme 从这里开始进行理解status的逻辑
        if (StringUtils.isNotBlank(taskType)) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        List<TaskEntity> taskEntityList = taskService.getTaskList(taskType, status, limit);
        List<TaskRes> taskRes = taskEntityList.stream().map(TaskConverter::toDTO).collect(Collectors.toList());
        return new R(taskRes);
    }

    @GetMapping("/hold_task")
    @Override
    public R holdTask(@RequestParam("taskType") String taskType, @RequestParam("status") int status, @RequestParam("limit") int limit) {
        if (StringUtils.isNotBlank(taskType)) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        List<TaskEntity> taskEntityList = taskService.holdTask(taskType, status, limit);
        List<TaskRes> taskRes = taskEntityList.stream().map(TaskConverter::toDTO).collect(Collectors.toList());
        return new R(taskRes);
    }

    @PostMapping("/set_task")
    @Override
    public R setTask(@RequestBody TaskSetReq taskSetReq) {
        if (StringUtils.isNotBlank(taskSetReq.getTaskId())) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        taskService.setTask(taskSetReq);
        return new R(ExceptionStatus.SUCCESS);
    }

    @GetMapping("/user_task_list")
    @Override
    public R getUserTaskList(@RequestParam("userId") String userId, @RequestParam("statusList") int statusList) {
        if (StringUtils.isNotBlank(userId)) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        List<TaskEntity> taskEntityList = taskService.getTaskByUserIdAndStatus(userId, statusList);
        List<TaskRes> taskRes = taskEntityList.stream().map(TaskConverter::toDTO).collect(Collectors.toList());
        return new R(taskRes);
    }

    @GetMapping("/cfg/list")
    public R getTaskTypeCfgList() {
        return new R(taskCfgService.getTaskTypeCfgList());
    }

    @GetMapping("/cfg/configuration")
    public R SetTaskCFG(@RequestBody TaskCfgReq taskCfgReq) {
        if (StringUtils.isNotBlank(taskCfgReq.getTaskType())) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        taskCfgService.save(taskCfgReq);
        return new R(ExceptionStatus.SUCCESS);
    }

}

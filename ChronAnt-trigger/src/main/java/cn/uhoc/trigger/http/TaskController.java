package cn.uhoc.trigger.http;

import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.vo.TaskStatus;
import cn.uhoc.domain.scheduler.service.ITaskCfgService;
import cn.uhoc.domain.scheduler.service.ITaskService;
import cn.uhoc.trigger.api.ITaskController;
import cn.uhoc.trigger.api.dto.*;
import cn.uhoc.type.enums.ExceptionStatus;
import cn.uhoc.type.model.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    public R createTask(@RequestBody TaskCreateReqDTO taskCreateReqDTO) {
        if (StringUtils.isNotBlank(taskCreateReqDTO.getTaskType())) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        String taskId = taskService.createTask(taskCreateReqDTO);
        return new R(TaskCreateResDTO.builder().taskId(taskId));
    }

    @GetMapping("/get_task")
    @Override
    public R<TaskResDTO> getTask(@RequestParam("taskId") String taskId) {
        if (StringUtils.isNotBlank(taskId)) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        TaskResDTO taskResDTO = taskService.getTask(taskId).toDTO();
        return new R(taskResDTO);
    }

    @GetMapping("/get_task_list")
    @Override
    public R<List<TaskResDTO>> getTaskList(@RequestParam("taskType") String taskType, @RequestParam("status") int status, @RequestParam("limit") int limit) {
        // fixme 从这里开始进行理解status的逻辑
        if (StringUtils.isNotBlank(taskType) || !TaskStatus.IsValidStatus(status)) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        List<TaskEntity> taskEntityList = taskService.getTaskList(taskType, status, limit);
        List<TaskResDTO> taskResDTOs = taskEntityList.stream().map(TaskEntity::toDTO).collect(Collectors.toList());
        return new R(taskResDTOs);
    }

    @GetMapping("/hold_task")
    @Override
    public R holdTask(@RequestParam("taskType") String taskType, @RequestParam("status") int status, @RequestParam("limit") int limit) {
        if (StringUtils.isNotBlank(taskType) || !TaskStatus.IsValidStatus(status)) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        List<TaskEntity> taskEntityList = taskService.holdTask(taskType, status, limit);
        List<TaskResDTO> taskResDTOs = taskEntityList.stream().map(TaskEntity::toDTO).collect(Collectors.toList());
        return new R(taskResDTOs);
    }

    @PostMapping("/set_task")
    @Override
    public R setTask(@RequestBody TaskSetReqDTO taskSetReqDTO) {
        if (StringUtils.isNotBlank(taskSetReqDTO.getTaskId())) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        taskService.setTask(taskSetReqDTO);
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
        List<TaskResDTO> taskResDTOs = taskEntityList.stream().map(TaskEntity::toDTO).collect(Collectors.toList());
        return new R(taskResDTOs);
    }

    @GetMapping("/cfg/list")
    public R getTaskTypeCfgList() {
        return new R(taskCfgService.getTaskTypeCfgList());
    }

    @GetMapping("/cfg/configuration")
    public R SetTaskCFG(@RequestBody TaskCfgDTO taskCfgDTO) {
        if (StringUtils.isNotBlank(taskCfgDTO.getTaskType())) {
            log.error("input invalid");
            return new R(ExceptionStatus.ERR_INPUT_INVALID);
        }
        taskCfgService.save(taskCfgDTO);
        return new R(ExceptionStatus.SUCCESS);
    }

}

package cn.uhoc.trigger.http;

import cn.uhoc.domain.scheduler.service.ITaskService;
import cn.uhoc.trigger.api.ITaskController;
import cn.uhoc.trigger.api.dto.TaskCreateReqDTO;
import cn.uhoc.trigger.api.dto.TaskCreateResDTO;
import cn.uhoc.trigger.api.dto.TaskResDTO;
import cn.uhoc.type.enums.ExceptionCode;
import cn.uhoc.type.model.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


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

    @PostMapping("/create_task")
    @Override
    public R createTask(@RequestBody TaskCreateReqDTO taskCreateReqDTO) {
        if (StringUtils.isNotBlank(taskCreateReqDTO.getTaskType())) {
            log.error("input invalid");
            return new R(ExceptionCode.ERR_INPUT_INVALID);
        }
        String taskId = taskService.createTask(taskCreateReqDTO);
        return new R(TaskCreateResDTO.builder().taskId(taskId));
    }

    @GetMapping("/get_task")
    @Override
    public R<TaskResDTO> getTask(@RequestParam("taskId") String taskId) {
        if (StringUtils.isNotBlank(taskId)){
            log.error("input invalid");
            return new R(ExceptionCode.ERR_INPUT_INVALID);
        }
        TaskResDTO taskResDTO = taskService.getTask(taskId);
        return new R(taskResDTO);
    }


}

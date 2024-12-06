package cn.uhoc.trigger.api;

import cn.uhoc.trigger.api.dto.TaskCreateReqDTO;
import cn.uhoc.trigger.api.dto.TaskCreateResDTO;
import cn.uhoc.trigger.api.dto.TaskResDTO;
import cn.uhoc.trigger.api.dto.TaskSetReqDTO;
import cn.uhoc.type.model.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-02 18:00
 **/
public interface ITaskController {

    /**
     * 创建任务
     */
    R<TaskCreateResDTO> createTask(TaskCreateReqDTO taskCreateReqDTO);

    /**
     * 获取任务列表
     */
    R<List<TaskResDTO>> getTaskList(String taskType, int status, int limit);

    /**
     * 获取单个任务
     */
    R<TaskResDTO> getTask(String task_id);

    /**
     * 占据任务
     */
    R<TaskResDTO> holdTask(String taskType, int status, int limit);

    /**
     * 更改任务信息
     */
    R setTask(TaskSetReqDTO asyncTaskSetRequest);

    /**
     * 获取指定用户的任务列表
     */
    R getUserTaskList(String user_id, int statusList);


}


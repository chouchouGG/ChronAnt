package cn.uhoc.trigger.api;

import cn.uhoc.trigger.api.dto.TaskCreateReq;
import cn.uhoc.trigger.api.dto.TaskCreateRes;
import cn.uhoc.trigger.api.dto.TaskRes;
import cn.uhoc.trigger.api.dto.TaskSetReq;
import cn.uhoc.type.model.R;

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
    R<TaskCreateRes> createTask(TaskCreateReq taskCreateReq);

    /**
     * 获取任务列表
     */
    R<List<TaskRes>> getTaskList(String taskType, int status, int limit);

    /**
     * 获取单个任务
     */
    R<TaskRes> getTask(String taskId);

    /**
     * 占据任务
     */
    R<TaskRes> holdTask(String taskType, int status, int limit);

    /**
     * 更改任务信息
     */
    R setTask(TaskSetReq taskSetReq);

    /**
     * 获取指定用户的任务列表
     */
    R getUserTaskList(String userId, int statusList);


}


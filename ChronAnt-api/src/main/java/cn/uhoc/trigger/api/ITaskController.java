package cn.uhoc.trigger.api;

import cn.uhoc.trigger.api.dto.TaskCreateReqDTO;
import cn.uhoc.trigger.api.dto.TaskCreateResDTO;
import cn.uhoc.trigger.api.dto.TaskResDTO;
import cn.uhoc.type.model.R;

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

//    /**
//     * 获取任务列表
//     */
//    R<List<>> getTaskList(String taskType, int status, int limit);

//    /**
//     * 更改任务信息
//     */
//    <T> ReturnStatus<T> setTask(AsyncTaskSetRequest asyncTaskSetRequest);
//
    /**
     * 获取任务
     */
    R<TaskResDTO> getTask(String task_id);
//
//    /**
//     * 获取指定用户的任务列表
//     */
//    <T> ReturnStatus<T> getTaskByUserIdAndStatus(String user_id, int statusList);
//
//
//    /**
//     * 占据任务
//     */
//    <T> ReturnStatus<T> holdTask(String taskType, int status, int limit);
}


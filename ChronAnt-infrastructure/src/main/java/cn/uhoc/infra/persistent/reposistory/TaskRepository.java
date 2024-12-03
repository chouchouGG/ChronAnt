package cn.uhoc.infra.persistent.reposistory;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskPosEntity;
import cn.uhoc.domain.scheduler.repository.ITaskRepository;
import cn.uhoc.infra.convertor.ITaskConvertor;
import cn.uhoc.infra.persistent.dao.ITaskCfgDao;
import cn.uhoc.infra.persistent.dao.ITaskDao;
import cn.uhoc.infra.persistent.dao.ITaskPosDao;
import cn.uhoc.infra.persistent.po.Task;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-02 16:17
 **/
public class TaskRepository implements ITaskRepository {

    @Resource
    private ITaskDao taskDao;

    @Resource
    private ITaskCfgDao taskCfgDao;

    @Resource
    private ITaskPosDao taskPosDao;


    @Resource
    private ITaskConvertor taskConvertor;

    @Override
    public TaskPosEntity getTaskPositionByType(String taskType) {
        return taskPosDao.getTaskPositionByType(taskType);
    }

    @Override
    public TaskCfgEntity getTaskConfigByType(String taskType) {
        return taskCfgDao.getTaskConfigByType(taskType);
    }

    @Override
    public void insertTask(String tableName, TaskEntity taskEntity) {
        taskDao.insertTask(tableName, taskEntity);
    }

    @Override
    public List<TaskEntity> getTaskList(String tableName, String taskType, int status, int limit) {
        List<Task> taskList = taskDao.getTaskList(tableName, taskType, status, limit);
        if (taskList == null || taskList.isEmpty()) {
            return Collections.emptyList();
        }
        return taskList.stream()
                .map(taskConvertor::toEntity) // 将每个 Task 转换为 TaskEntity
                .collect(Collectors.toList()); // 收集结果为 List
    }

    @Override
    public TaskEntity getTaskById(String taskId, String tableName) {
        Task task = taskDao.getTaskById(taskId, tableName);
    }
}

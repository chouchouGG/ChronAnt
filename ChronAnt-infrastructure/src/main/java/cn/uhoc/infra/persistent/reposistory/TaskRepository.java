package cn.uhoc.infra.persistent.reposistory;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskPosEntity;
import cn.uhoc.domain.scheduler.repository.ITaskRepository;
import cn.uhoc.infra.persistent.dao.ITaskCfgDao;
import cn.uhoc.infra.persistent.dao.ITaskDao;
import cn.uhoc.infra.persistent.dao.ITaskPosDao;
import cn.uhoc.infra.persistent.po.Task;
import cn.uhoc.infra.persistent.po.TaskCfg;
import cn.uhoc.trigger.api.dto.TaskCfgDTO;
import cn.uhoc.type.enums.ExceptionStatus;
import cn.uhoc.type.exception.E;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-02 16:17
 **/
@Slf4j
public class TaskRepository implements ITaskRepository {

    @Resource
    private ITaskDao taskDao;

    @Resource
    private ITaskCfgDao taskCfgDao;

    @Resource
    private ITaskPosDao taskPosDao;

    @Override
    public TaskPosEntity getTaskPositionByType(String taskType) {
        // 参数检查
        if (StringUtils.isBlank(taskType)) {
            log.error("任务类型参数为空");
            throw new IllegalArgumentException("任务类型不能为空");
        }
        TaskPosEntity taskPos = taskPosDao.getTaskPositionByType(taskType);
        if (null == taskPos) {
            log.warn("未找到任务类型对应的任务位置，任务类型: {}", taskType);
            throw new E(ExceptionStatus.ERR_GET_TASK_POS);
        }
        return taskPos;
    }

    @Override
    public TaskCfgEntity getTaskConfigByType(String taskType) {
        // 参数检查
        if (StringUtils.isBlank(taskType)) {
            log.error("任务类型参数为空");
            throw new IllegalArgumentException("任务类型不能为空");
        }
        TaskCfgEntity taskTypeCfg = taskCfgDao.getTaskConfigByType(taskType);
        if (null == taskTypeCfg)  {
            log.warn("未找到任务类型对应的任务配置，任务类型: {}", taskType);
            throw new E(ExceptionStatus.ERR_GET_TASK_CFG);
        }
        return taskTypeCfg;
    }



    @Override
    public void insertTask(String tableName, TaskEntity taskEntity) {
        taskDao.insertTask(tableName, taskEntity);
    }


    @Override
    public TaskEntity getTaskById(String taskId, String tableName) {
        Task task = taskDao.getTaskById(taskId, tableName);
        if (null == task) {
            log.warn("未从表中获取到对应的任务, 表名: {}, 任务id: {}", tableName, taskId);
            throw new E(ExceptionStatus.ERR_GET_TASK_INFO);
        }
        return Task.toEntity(task);
    }

    @Override
    public List<TaskEntity> getTaskByUseridAndStatus(String userId, List<Integer> statusList, String tableName) {
        List<Task> taskList = taskDao.getTaskByUseridAndStatus(userId, statusList, tableName);
        if (null == taskList || taskList.isEmpty()) {
            log.warn("未从表中获取到对应的任务, user_id: {}, status_list: {}, table_name: {}", userId, statusList, tableName);
            throw new E(ExceptionStatus.ERR_GET_TASK_INFO);
        }
        return taskList.stream().map(Task::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<TaskEntity> getTaskList(String tableName, String taskType, int status, int limit) {
        List<Task> taskList = taskDao.getTaskList(tableName, taskType, status, limit);
        return taskList.stream().map(Task::toEntity).collect(Collectors.toList());
    }

    @Override
    public void updateStatusBatch(String tableName, List<String> taskIds, int status) {
        taskDao.updateStatusBatch(tableName, taskIds, status);
    }

    @Override
    public void updateTask(TaskEntity taskEntity, List<Integer> list, String tableName) {
        taskDao.updateTask(taskEntity, list, tableName);
    }

    @Override
    public List<TaskCfgEntity> getTaskTypeCfgList() {
        List<TaskCfg> taskTypeCfgList = taskCfgDao.getTaskTypeCfgList();
        if (CollectionUtils.isEmpty(taskTypeCfgList)) {
            log.warn("未从表中获取到任务配置");
            throw new E(ExceptionStatus.ERR_GET_TASK_CFG);
        }
        return taskTypeCfgList.stream().map(TaskCfg::toEntity).collect(Collectors.toList());
    }

    @Override
    public void save(TaskCfgEntity taskCfgEntity) {
        TaskCfg taskCfg = TaskCfg.fromEntity(taskCfgEntity);
        int e = taskCfgDao.save(taskCfg);
        if (0 == e) {
            log.warn("新增任务配置失败");
            throw new E(ExceptionStatus.ERR_SET_TASK_CFG);
        }
    }
}

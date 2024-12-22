package cn.uhoc.domain.manager.service.impl;

import cn.uhoc.domain.manager.model.entity.TaskCfgEntity;
import cn.uhoc.domain.manager.model.entity.TaskEntity;
import cn.uhoc.domain.manager.model.entity.TaskPosEntity;
import cn.uhoc.domain.manager.model.vo.TaskConstants;
import cn.uhoc.domain.manager.model.vo.TaskStatus;
import cn.uhoc.domain.manager.repository.ITaskRepository;
import cn.uhoc.domain.manager.service.ITaskService;
import cn.uhoc.trigger.api.dto.TaskCreateReq;
import cn.uhoc.trigger.api.dto.TaskSetReq;
import cn.uhoc.type.common.SnowFlake;
import cn.uhoc.type.enums.ExceptionStatus;
import cn.uhoc.type.exception.E;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-01 18:59
 **/
@Slf4j
@Service
public class TaskServiceImpl implements ITaskService {

    @Resource
    private ITaskRepository taskRepository;

    @Override
    public String createTask(TaskCreateReq taskCreateReq) {
        String taskType = taskCreateReq.getTaskType();

        // 获取任务在数据库表中的位置偏移
        TaskPosEntity taskPos = taskRepository.getTaskPositionByType(taskType);
        // 获取任务类型的配置参数
        TaskCfgEntity taskTypeCfg = taskRepository.getTaskConfigByType(taskType);

        // 构建任务对象并写入数据库
        String taskId = null;
        try {
            TaskEntity taskEntity = buildTaskModel(taskCreateReq, taskPos, taskTypeCfg);
            taskId = taskEntity.getTaskId();
            // todo 后期去掉任务表按类型分表
            String tableName = getTableName(taskType, taskPos.getScheduleEndPos());
            taskRepository.insertTask(tableName, taskEntity);
        } catch (Exception e) {
            throw new E(ExceptionStatus.ERR_CREATE_TASK);
        }

        // 返回给调用方任务Id，用于后续轮询检查任务执行状态
        return taskId == null ? "" : taskId;
    }

    @Override
    public List<TaskEntity> getTaskList(String taskType, int status, int limit) {
        int aLimit = adjustLimit(limit);
        TaskPosEntity taskPos = taskRepository.getTaskPositionByType(taskType);
        String tableName = getTableName(taskType, taskPos.getScheduleBeginPos());
        return taskRepository.getTaskList(tableName, taskType, status, aLimit);
    }

    @Override
    public TaskEntity getTask(String taskId) {
        String tableName = getTableNameById(taskId);
        return taskRepository.getTaskById(taskId, tableName);
    }

    @Override
    public List<TaskEntity> holdTask(String taskType, int status, int limit) {
        int aLimit = adjustLimit(limit);
        TaskPosEntity taskPos = taskRepository.getTaskPositionByType(taskType);
        String tableName = getTableName(taskType, taskPos.getScheduleBeginPos());
        List<TaskEntity> taskEntityList = taskRepository.getTaskList(tableName, taskType, status, aLimit);
        // 提前处理空列表的情况
        if (taskEntityList == null || taskEntityList.isEmpty()) {
            log.info("No tasks found for table: {}, taskType: {}, status: {}", tableName, taskType, status);
            return null;
        }
        List<TaskEntity> filterList = taskEntityList
                .stream()
                .parallel()
                // TODO 理解这个过滤条件的含义？
                .filter(task -> task.getCrtRetryNum() == 0 || task.getMaxRetryInterval() != 0
                        && task.getOrderTime() <= System.currentTimeMillis())
                .collect(Collectors.toList());
        List<String> taskIds = filterList.stream().map(TaskEntity::getTaskId).collect(Collectors.toList());
        if (!taskIds.isEmpty()) {
            taskRepository.updateStatusBatch(tableName, taskIds, TaskStatus.EXECUTING.getCode());
        }
        return filterList;
    }

    @Override
    public void setTask(TaskSetReq taskSetReq) {
        String tableName = getTableNameById(taskSetReq.getTaskId());
        TaskEntity taskEntity = TaskEntity.builder()
                .taskStage(taskSetReq.getTaskStage())
                .status(taskSetReq.getStatus())
                .crtRetryNum(taskSetReq.getCrtRetryNum())
                .maxRetryNum(taskSetReq.getMaxRetryNum())
                .orderTime(taskSetReq.getOrderTime())
                .priority(taskSetReq.getPriority())
                .maxRetryInterval(taskSetReq.getMaxRetryInterval())
                .scheduleLog(taskSetReq.getScheduleLog())
                .taskContext(taskSetReq.getTaskContext())
                .build();
        List<Integer> excludeStatusList = Arrays.asList(
                TaskStatus.SUCCESS.getCode(),
                TaskStatus.FAIL.getCode()
        );
        try {
            taskRepository.updateTask(taskEntity, excludeStatusList, tableName);
        } catch (Exception e) {
            throw new E(ExceptionStatus.ERR_SET_TASK);
        }
    }

    @Override
    public List<TaskEntity> getTaskByUserIdAndStatus(String userId, int status) {
        List<TaskEntity> taskEntityList;
        // TODO 表明tableName临时使用，后续完善为一个任务表，而不是一个类型的任务一个表
        String tableName = getTableName("LarkTask", 1);
        List<Integer> statusList = TaskStatus.getStatusList(status);
        try {
            taskEntityList = taskRepository.getTaskByUseridAndStatus(userId, statusList, tableName); // TODO 去掉tableName参数
        } catch (Exception e) {
            throw new E(ExceptionStatus.ERR_GET_TASK_LIST);
        }
        return taskEntityList;
    }

    // 这个方法暂时没用到
    private boolean isUnUpdate(int x) {
        return x == TaskConstants.DEFAULT_TASK_STATUS;
    }

    private String getTableNameById(String taskId) {
        String[] ss = taskId.split("_");
        return getTableName(ss[1], Integer.parseInt(ss[2]));
    }

    /**
     * 构建任务实体对象
     */
    private TaskEntity buildTaskModel(TaskCreateReq taskCreateReq, TaskPosEntity taskPos, TaskCfgEntity taskTypeCfg) {
        String taskId = getTaskId(taskCreateReq.getTaskType(), taskPos);
        return TaskEntity.builder()
                .userId(taskCreateReq.getUserId())
                .taskId(taskId)
                .taskType(taskCreateReq.getTaskType())
                .status(TaskStatus.PENDING.getCode())
                .crtRetryNum(0)
                .maxRetryNum(taskTypeCfg.getMaxRetryNum())
                .maxRetryInterval(taskTypeCfg.getRetryInterval())
                .taskStage(taskCreateReq.getTaskStage())
                .scheduleLog(taskCreateReq.getScheduleLog())
                .taskContext(taskCreateReq.getTaskContext())
                .build();
    }

    /**
     * 构建新建的任务待插入的数据库表名
     */
    private String getTableName(String taskType, int pos) {
        return "task_" + taskType.toLowerCase() + "_" + pos;
    }

    /**
     * 构建任务id
     *
     * @param taskType 任务类型
     * @param taskPos  任务位置偏移
     * @return 任务id
     */
    private String getTaskId(String taskType, TaskPosEntity taskPos) {
        return SnowFlake.nextId() + "_" + taskType + "_" + taskPos.getScheduleEndPos();
    }

    /**
     * 根据最大和默认限制调整 limit 值
     *
     * @param limit 传入的限制值
     * @return 调整后的限制值
     */
    private int adjustLimit(int limit) {
        if (limit > TaskConstants.MAX_TASK_LIST_LIMIT) {
            limit = TaskConstants.MAX_TASK_LIST_LIMIT;
        }
        if (limit == 0) {
            limit = TaskConstants.DEFAULT_TASK_LIST_LIMIT;
        }
        return limit;
    }
}

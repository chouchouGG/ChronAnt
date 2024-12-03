package cn.uhoc.domain.scheduler.service.impl;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskPosEntity;
import cn.uhoc.domain.scheduler.model.vo.TaskStatus;
import cn.uhoc.domain.scheduler.repository.ITaskRepository;
import cn.uhoc.domain.scheduler.service.ITaskService;
import cn.uhoc.trigger.api.dto.TaskCreateReqDTO;
import cn.uhoc.trigger.api.dto.TaskResDTO;
import cn.uhoc.type.common.Utils;
import cn.uhoc.type.enums.ExceptionCode;
import cn.uhoc.type.exception.E;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    public String createTask(TaskCreateReqDTO taskCreateReqDTO) {
        String taskType = taskCreateReqDTO.getTaskType();

        // 获取任务在数据库表中的位置偏移
        TaskPosEntity taskPos = null;
        try {
            taskPos = taskRepository.getTaskPositionByType(taskType);
        } catch (Exception e) {
            throw new E(ExceptionCode.ERR_GET_TASK_POS);
        }

        // 获取任务类型的配置参数
        TaskCfgEntity taskTypeCfg;
        try {
            taskTypeCfg = taskRepository.getTaskConfigByType(taskType);
        } catch (Exception e) {
            throw new E(ExceptionCode.ERR_GET_TASK_CFG) ;
        }

        // 构建任务对象并写入数据库
        String taskId = null;
        try {
            TaskEntity taskEntity = buildTaskModel(taskCreateReqDTO, taskPos, taskTypeCfg);
            taskId = taskEntity.getTaskId();
            String tableName = getTableName(taskType, taskPos.getScheduleEndPos());
            taskRepository.insertTask(tableName, taskEntity);
        } catch (Exception e) {
            throw new E(ExceptionCode.ERR_CREATE_TASK) ;
        }

        // 返回给调用方任务Id，用于后续轮询检查任务执行状态
        return taskId == null ? "" : taskId;
    }

    @Override
    public TaskResDTO getTask(String taskId) {
        TaskEntity taskEntity;
        String tableName = getTableNameById(taskId);
        try {
            taskEntity = taskRepository.getTaskById(taskId, tableName);
        } catch (Exception e) {
            throw new E(ExceptionCode.ERR_GET_TASK_INFO);
        }
        return TaskResDTO.builder()
                .userId(taskEntity.getUserId())
                .taskId(taskEntity.getTaskId())
                .taskType(taskEntity.getTaskType())
                .taskStage(taskEntity.getTaskStage())
                .status(taskEntity.getStatus())
                .crtRetryNum(taskEntity.getCrtRetryNum())
                .maxRetryNum(taskEntity.getMaxRetryNum())
                .maxRetryInterval(taskEntity.getMaxRetryInterval())
                .scheduleLog(taskEntity.getScheduleLog())
                .taskContext(taskEntity.getTaskContext())
                .build();
    }

    private String getTableNameById(String taskId) {
        String[] ss = taskId.split("_");
        return getTableName(ss[1], Integer.parseInt(ss[2]));
    }

    /**
     * 构建任务实体对象
     * @param userId 用户id
     * @param taskType 任务类型
     * @param taskPos 任务位置
     * @param taskTypeCfg 任务配置
     * @return 任务实体
     */
    private TaskEntity buildTaskModel(TaskCreateReqDTO taskCreateReqDTO, TaskPosEntity taskPos, TaskCfgEntity taskTypeCfg) {
        String taskId = getTaskId(taskCreateReqDTO.getTaskType(), taskPos);
        return TaskEntity.builder()
                .userId(taskCreateReqDTO.getUserId())
                .taskId(taskId)
                .taskType(taskCreateReqDTO.getTaskType())
                .status(TaskStatus.PENDING.getStatus())
                .crtRetryNum(0)
                .maxRetryNum(taskTypeCfg.getMaxRetryNum())
                .maxRetryInterval(taskTypeCfg.getRetryInterval())
                .taskStage(taskCreateReqDTO.getTaskStage())
                .scheduleLog(taskCreateReqDTO.getScheduleLog())
                .taskContext(taskCreateReqDTO.getTaskContext())
                .build();
    }

    /**
     * 构建新建的任务待插入的数据库表名
     * @param taskType 任务类型
     * @param pos 任务位置
     * @return 表名
     */
    private String getTableName(String taskType, int pos) {
        return "task_" + taskType.toLowerCase() + "_" + pos;
    }

    /**
     * 构建任务id
     * @param taskType 任务类型
     * @param taskPos 任务位置偏移
     * @return 任务id
     */
    private String getTaskId(String taskType, TaskPosEntity taskPos) {
        return Utils.getTaskId() + "_" + taskType + "_" + taskPos.getScheduleEndPos();
    }


}

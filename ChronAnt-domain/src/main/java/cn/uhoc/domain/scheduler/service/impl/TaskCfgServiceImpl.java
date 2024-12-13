package cn.uhoc.domain.scheduler.service.impl;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.repository.ITaskRepository;
import cn.uhoc.domain.scheduler.service.ITaskCfgService;
import cn.uhoc.trigger.api.dto.TaskCfgReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-06 14:08
 **/
@Slf4j
@Service
public class TaskCfgServiceImpl implements ITaskCfgService {

    @Resource
    private ITaskRepository taskRepository;

    @Override
    public List<TaskCfgEntity> getTaskTypeCfgList() {
        return taskRepository.getTaskTypeCfgList();
    }

    @Override
    public void save(TaskCfgReq taskCfgReq) {
        TaskCfgEntity taskCfgEntity = TaskCfgEntity.fromDTO(taskCfgReq);
        taskRepository.save(taskCfgEntity);
    }
}

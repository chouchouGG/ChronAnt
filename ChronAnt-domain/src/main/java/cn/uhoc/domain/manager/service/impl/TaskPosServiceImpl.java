package cn.uhoc.domain.manager.service.impl;

import cn.uhoc.domain.manager.model.entity.TaskPosEntity;
import cn.uhoc.domain.manager.repository.ITaskRepository;
import cn.uhoc.domain.manager.service.ITaskPosService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-29 12:45
 **/
@Service
public class TaskPosServiceImpl implements ITaskPosService {

    @Resource
    private ITaskRepository taskRepository;

    @Override
    public List<TaskPosEntity> getTaskPosList() {
        return taskRepository.getTaskPosList();
    }
}

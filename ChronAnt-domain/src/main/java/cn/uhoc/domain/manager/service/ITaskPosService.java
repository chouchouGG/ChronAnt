package cn.uhoc.domain.manager.service;

import cn.uhoc.domain.manager.model.entity.TaskCfgEntity;
import cn.uhoc.domain.manager.model.entity.TaskPosEntity;

import java.util.List;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-29 12:45
 **/
public interface ITaskPosService {

    List<TaskPosEntity> getTaskPosList();
}

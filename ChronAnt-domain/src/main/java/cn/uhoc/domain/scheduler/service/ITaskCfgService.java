package cn.uhoc.domain.scheduler.service;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.trigger.api.dto.TaskCfgReq;

import java.util.List;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-06 14:10
 **/
public interface ITaskCfgService {

    /**
     * 查询所有任务配置列表
     */
    List<TaskCfgEntity> getTaskTypeCfgList();

    /**
     * 新增任务配置项
     * @param taskCfgReq 任务类型配置DTO对象
     */
    void save(TaskCfgReq taskCfgReq);
}

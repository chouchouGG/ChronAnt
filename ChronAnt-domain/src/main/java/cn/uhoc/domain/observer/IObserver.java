package cn.uhoc.domain.observer;

import cn.uhoc.domain.executor.model.entity.TaskBaseEntity;
import cn.uhoc.domain.executor.model.entity.TaskSetStageEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;

import java.util.List;

public interface IObserver {

    void onBoot();

    void onObtain(List<TaskEntity> taskEntityList, List<TaskBaseEntity> taskBaseEntityList);

    void onExecute(TaskBaseEntity taskBaseEntity);

    void onFinish(TaskBaseEntity taskBaseEntity, TaskSetStageEntity taskSetStageEntity, Class<?> aClass);

    void onStop(TaskBaseEntity taskBaseEntity);

    void onError(TaskBaseEntity taskBaseEntity, TaskCfgEntity taskCfgEntity, List<TaskBaseEntity> taskBaseEntityList, Class<?> aClass, Exception e);

}

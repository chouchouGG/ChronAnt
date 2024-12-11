package cn.uhoc.domain.observer;

import cn.uhoc.domain.executor.model.entity.TaskBaseEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;

import java.util.List;

public interface IObserver {

    void onBoot();

    void onObtain(List<TaskEntity> taskEntityList);

    void onExecute(TaskBaseEntity taskBaseEntity);

    void onFinish(TaskBaseEntity taskBaseEntity);

    void onStop(TaskBaseEntity taskBaseEntity);

    void onError(TaskBaseEntity taskBaseEntity, Exception e);

}

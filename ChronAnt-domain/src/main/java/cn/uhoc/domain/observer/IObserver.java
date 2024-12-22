package cn.uhoc.domain.observer;

import cn.uhoc.domain.executor.entity.TaskBase;
import cn.uhoc.domain.manager.model.entity.TaskEntity;

import java.util.List;

public interface IObserver {

    void onBoot();

    void onObtain(List<TaskEntity> taskEntityList);

    void onExecute(TaskBase taskBase);

    void onFinish(TaskBase taskBase);

    void onStop(TaskBase taskBase);

    void onError(TaskBase taskBase, Exception e);

}

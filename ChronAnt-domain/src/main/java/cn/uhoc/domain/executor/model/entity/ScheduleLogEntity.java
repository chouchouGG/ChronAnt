package cn.uhoc.domain.executor.model.entity;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ScheduleLogEntity {

    // 上一次的数据
    ScheduleDataEntity lastData;

    // 历史数据记录
    List<ScheduleDataEntity> historyData;

    public ScheduleLogEntity() {
        lastData = new ScheduleDataEntity();
        historyData = new ArrayList<>();
    }

}

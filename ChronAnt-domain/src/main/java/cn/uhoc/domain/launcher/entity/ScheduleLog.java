package cn.uhoc.domain.launcher.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ScheduleLog {

    // 上一次的日志数据
    ScheduleData lastData;

    // 历史日志数据记录
    List<ScheduleData> historyData;

    public ScheduleLog() {
        lastData = new ScheduleData();
        historyData = new ArrayList<>();
    }
}

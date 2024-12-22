package cn.uhoc.domain.executor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleData<T> {

    String traceId;

    String errMsg;

    T result;

//    String costTime; // 除了traceId和errMsg，其余的信息都应该由各个观察者添加
}

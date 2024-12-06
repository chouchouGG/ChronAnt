package cn.uhoc.type.model;

import cn.uhoc.type.enums.ExceptionStatus;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class R<T> {

    private int code;
    private String info;
    private T data;

    public R(ExceptionStatus responseCode) {
        this.code = responseCode.getCode();
        this.info = responseCode.getInfo();
    }

    public R(T data) {
        this(ExceptionStatus.SUCCESS);
        this.data = data;
    }
}
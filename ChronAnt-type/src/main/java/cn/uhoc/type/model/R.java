package cn.uhoc.type.model;

import cn.uhoc.type.enums.ExceptionCode;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class R<T> {

    private int code;
    private String info;
    private T data;

    public R(ExceptionCode responseCode) {
        this.code = responseCode.getCode();
        this.info = responseCode.getInfo();
    }

    public R(T data) {
        this(ExceptionCode.SUCCESS);
        this.data = data;
    }
}
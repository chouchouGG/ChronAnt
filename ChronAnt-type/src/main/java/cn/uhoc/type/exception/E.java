package cn.uhoc.type.exception;

import cn.uhoc.type.enums.ExceptionStatus;
import lombok.Data;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-01 15:48
 **/
@Data
public class E extends RuntimeException{

    /**
     * 异常码
     */
    private String code;

    /** 异常信息 */
    private String info;

    public E(int code) {
        this.code = String.valueOf(code);
    }

    public E(int code, Throwable cause) {
        this.code = String.valueOf(code);
        super.initCause(cause);
    }

    public E(ExceptionStatus r) {
        this(r.getCode(), r.getInfo());
    }

    public E(ExceptionStatus r, Throwable cause) {
        this(r.getCode(), r.getInfo(), cause);
    }

    public E(int code, String info) {
        this.code = String.valueOf(code);
        this.info = info;
    }

    public E(int code, String info, Throwable cause) {
        this.code = String.valueOf(code);
        this.info = info;
        super.initCause(cause);
    }
}

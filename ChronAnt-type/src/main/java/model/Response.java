package model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {

    private String code;
    private String info;
    private T data;


    @Getter
    @AllArgsConstructor
    public enum ResponseCode {
        ;

        private final String code;
        private final String info;
    }
}
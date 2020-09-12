package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;


@Data
public class ExceptionResult {
    private int status;
    private String msg;
    private Long timestamp;

    public ExceptionResult(ExceptionEnum e) {
        this.status=e.getCode();
        this.msg=e.getMsg();
        this.timestamp=System.currentTimeMillis();
    }
}

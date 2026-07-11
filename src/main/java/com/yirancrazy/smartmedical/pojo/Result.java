package com.yirancrazy.smartmedical.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: TODO
 * @Version: 1.0
 * @DateTime: 2024/7/25 21:41
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result(T data) {
        this.code = 200;
        this.message = "success";
        this.data = data;
    }

    public Result(T data, boolean success, String message) {
        if (success) {
            this.code = 200;
            this.message = "success";
        } else {
            this.code = 500;
            this.message = message;
        }
        this.data = data;
    }

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }



    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message);
    }

    public static <T> Result<T> info(int code,String message,T data) {
        return new Result<>(code,message, data);
    }
}


package com.health.healthplatform.result;

import lombok.Data;

/**
 * 统一返回结果类
 * @param <T> 数据类型
 */
@Data
public class R<T> {
    private Integer code; // 200成功，其它数字为失败
    private String message; // 返回信息
    private T data; // 返回数据

    /**
     * 无数据成功返回
     */
    public static <T> R<T> success() {
        return success(null);
    }

    /**
     * 带数据成功返回
     */
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    /**
     * 带消息成功返回
     */
    public static <T> R<T> success(String message) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage(message);
        return r;
    }

    /**
     * 带数据和消息成功返回
     */
    public static <T> R<T> success(T data, String message) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /**
     * 失败返回
     */
    public static <T> R<T> error(String message) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMessage(message);
        return r;
    }

    /**
     * 自定义状态码和消息的错误返回
     */
    public static <T> R<T> error(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /**
     * 带数据的错误返回
     */
    public static <T> R<T> error(String message, T data) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == 200;
    }
}
package com.zebrarfid.demo.result.label;

/*
 * 描述：这是用于 标签模板设计界面的请求和响应模板
 *
 */
import lombok.Data;

@Data
public class BaseResponse<T> {
    private int code;
    private String message;
    private T data;

    // 成功响应
    public static <T> BaseResponse<T> success(String message, T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    // 失败响应
    public static <T> BaseResponse<T> error(int code, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}

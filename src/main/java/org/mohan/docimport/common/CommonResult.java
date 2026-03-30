package org.mohan.docimport.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用接口返回对象，统一封装状态码、消息和返回数据。
 *
 * @param <T> 返回数据类型
 */
@Data
@Accessors(chain = true)
public class CommonResult<T> {

    private Integer code;
    private String msg;
    private T data;

    /**
     * 构造成功响应。
     *
     * @param data 返回数据
     * @param <T> 返回数据类型
     * @return 成功结果
     */
    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    /**
     * 构造失败响应。
     *
     * @param code 错误码
     * @param msg 错误信息
     * @param <T> 返回数据类型
     * @return 失败结果
     */
    public static <T> CommonResult<T> error(Integer code, String msg) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}

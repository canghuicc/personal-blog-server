package com.blog.web.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    //状态码
    private Integer code;

    //返回消息
    private String message;

    //返回数据
    private T data;

    /**
     * 创建一个表示成功的结果对象。
     *
     * @param <T> 结果对象的类型。
     * @return 返回一个包含成功状态码、消息和空数据的结果对象。
     */
    public static <T> Result<T> success() {
        // 构造一个表示成功的Result对象，其中数据部分为null
        return new Result<>(200, "success", null);
    }

    /**
     * 创建一个表示成功的结果对象。
     *
     * @param <T> 结果对象的类型。
     * @param data 成功时返回的数据。
     * @return 返回一个包含成功状态码、消息和数据的Result对象。
     */
    public static <T> Result<T> success(T data) {
        // 构造一个成功结果对象，包含状态码200、消息"success"和提供的数据
        return new Result<>(200, "success", data);
    }

    /**
     * 创建一个表示成功的结果对象。
     *
     * @param message 成功时的提示消息。
     * @param data 成功时返回的数据。
     * @param <T> 返回数据的类型。
     * @return 返回一个包含成功状态码、消息和数据的Result对象。
     */
    public static <T> Result<T> success(String message,T data) {
        // 构造一个成功结果对象
        return new Result<>(200, message, data);
    }

    /**
     * 生成一个表示成功的Result对象。
     *
     * @param message 成功时的提示消息。
     * @param <T> Result对象中数据的类型。
     * @return 返回一个包含成功状态码、消息和空数据的Result对象。
     */
    public static <T> Result<T> success(String message) {
        // 创建并返回一个包含成功状态码、指定消息和null数据的Result对象
        return new Result<>(200, message, null);
    }

    /**
     * 生成一个表示错误的结果对象。
     *
     * <p>该方法不接受任何参数，用于生成一个含有错误信息的结果对象。错误结果对象包含以下属性：
     * 状态码为500，错误信息为"error"，数据部分为null。</p>
     *
     * @param <T> 结果对象的类型。
     * @return 返回一个初始化为错误状态的结果对象实例。
     */
    public static <T> Result<T> error(){
        return new Result<>(500,"error",null);
    }

    /**
     * 创建一个表示错误的结果对象。
     *
     * @param code 错误代码，用以标识具体的错误类型。
     * @param <T> 结果对象的类型。
     * @return 返回一个初始化了错误代码、错误信息为"error"、数据为null的Result对象。
     */
    public static <T> Result<T> error(Integer code){
        return new Result<>(code,"error",null);
    }

    /**
     * 创建一个表示错误的结果对象。
     *
     * @param code 错误代码，用于标识具体的错误类型。
     * @param message 错误信息，用于描述错误的详细信息。
     * @param <T> 结果对象的类型。
     * @return 返回一个包含错误代码和错误信息的Result对象，数据字段为null。
     */
    public static <T> Result<T> error(Integer code,String message){
        return new Result<>(code,message,null);
    }

    /**
     * 生成一个表示错误的结果对象。
     *
     * @param message 错误信息，用于描述错误的详细信息。
     * @param <T> 结果对象的类型。
     * @return 返回一个包含错误信息和状态码的结果对象Result，数据部分为null。
     */
    public static <T> Result<T> error(String message){
        return new Result<>(500,message,null);
    }

}


package com.bytes.box.commons.base.response;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RestResponse<T> implements Serializable {

    private String code;

    private String message;

    private T data;

    private String path;

    private String service;

    public static <T> RestResponse success(T data, String path, String service) {

        //TODO 怎么获取service
        return builder()
                .code(RestCode.OK.getCode()).message(RestCode.OK.getMessage())
                .data(data).path(path).service(service).build();
    }

    public static <T> RestResponse success(T data) {
        return success(data, null, null);
    }

    public static <T> RestResponse success(T data, String service) {
        return success(data, null, service);
    }

    public static <T> RestResponse error(RestCode restCode, String path, String service) {
        return builder()
                .code(restCode.getCode()).message(restCode.getMessage())
                .path(path).service(service).build();
    }

    public static RestResponse error(RestCode restCode, String message, String path, String service) {
        return error(restCode.getCode(), message, path, service);
    }

    public static RestResponse error(String code, String message, String path, String service) {
        return builder()
                .code(code).message(message)
                .path(path).service(service).build();
    }

    public static <T> RestResponse error(RestCode restCode) {
        return of(restCode, null);
    }


    public static <T> RestResponse of(RestCode restCode, T data) {
        return builder()
                .code(restCode.getCode()).message(restCode.getMessage()).data(data).build();
    }

    public String toJson() {
        return JSONObject.toJSONString(this);
    }

    public boolean isSuccess() {
        return StringUtils.equalsAnyIgnoreCase(this.code, RestCode.OK.getCode());
    }

}

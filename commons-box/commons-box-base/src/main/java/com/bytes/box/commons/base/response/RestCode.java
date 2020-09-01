package com.bytes.box.commons.base.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum RestCode implements DefineRestCode {

    OK("200", "操作成功"),

    ERROR("500", "服务内部异常"),

    BAD_REQUEST("400", "请求参数错误"),

    NOT_FOUND("404", "没有对应请求"),

    METHOD_NOT_ALLOWED("405", "不支持的请求方法"),

    UNSUPPORTED_MEDIA_TYPE("415", "不支持 Media Type"),

    REQ_PARAM_ENCRYPT_ERROR("4001", "请求参数解密错误"),

    RESP_PARAM_ENCRYPT_ERROR("4002", "返回参数加密错误"),

    BOX_CONFIG_SETTING_EMPTY("900", "请求环境设置不能为空"),
    ;

    private final String code;

    private final String message;

    public DefaultException fetchException() {
        return new DefaultException(this);
    }

    public static Map<String, String> cache = Arrays.stream(RestCode.values())
            .collect(Collectors.toMap(RestCode::getCode, RestCode::getMessage));

    public static String getMessage(String code) {
        return cache.getOrDefault(code, null);
    }

    @Override
    public Pair<String, String> getPair() {
        return Pair.of(this.code, this.message);
    }
}

package com.bytes.box.commons.base.encrypt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

/**
 * 请求体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestHeaderContext {

    private String source;

    private String profile;

    private Boolean reqIgnore;

    private Boolean respIgnore;

    @Builder.Default
    private LocalDateTime ldt = LocalDateTime.now();

    public static RequestHeaderContext getRequestHeaderContext(String header) {
        return StringUtils.isBlank(header) ?
                RequestHeaderContext.builder().build() : JSON.toJavaObject(JSONObject.parseObject(header), RequestHeaderContext.class);
    }

    public String toJson() {
        return JSONObject.toJSONString(this);
    }

}
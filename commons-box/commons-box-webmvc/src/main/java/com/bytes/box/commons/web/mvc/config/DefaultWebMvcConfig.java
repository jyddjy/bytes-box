package com.bytes.box.commons.web.mvc.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.Jdk8DateCodec;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.bytes.box.commons.base.utils.FastJsonUtils;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
public class DefaultWebMvcConfig extends WebMvcConfigurationSupport {

    void init() {

        SerializeConfig.getGlobalInstance().put(LocalDateTime.class, (serializer, object, fieldName, fieldType, features) -> {
            if (object == null) {
                serializer.out.writeNull();
                return;
            }
            long value = ((LocalDateTime) object).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            serializer.out.writeLong(value);
        });

        SerializeConfig.getGlobalInstance().put(LocalDate.class, (serializer, object, fieldName, fieldType, features) -> {
            if (object == null) {
                serializer.out.writeNull();
                return;
            }
            long value = ((LocalDate) object).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            serializer.out.writeLong(value);
        });

        ParserConfig.getGlobalInstance().putDeserializer(LocalDateTime.class, Jdk8DateCodec.instance);
        // https://baijiahao.baidu.com/s?id=1671603044044877345&wfr=spider&for=pc
        ParserConfig.getGlobalInstance().setSafeMode(true);

    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        init();
        super.extendMessageConverters(converters);
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(FastJsonUtils.SERIALIZER_FEATURES);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        fastConverter.setSupportedMediaTypes(ImmutableList.of(MediaType.APPLICATION_JSON_UTF8,
                new MediaType("application", "*+json")));
        converters.add(0, fastConverter);

        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setSupportedMediaTypes(ImmutableList.of(MediaType.TEXT_PLAIN));
        converters.add(1, stringHttpMessageConverter);

        super.configureMessageConverters(converters);

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // **代表所有路径
                .allowedOrigins("*") // allowOrigin指可以通过的ip，*代表所有，可以使用指定的ip，多个的话可以用逗号分隔，默认为*
                .allowedMethods("GET", "POST", "HEAD", "PUT", "DELETE") // 指请求方式 默认为*
                .allowCredentials(false) // 支持证书，默认为true
                .maxAge(3600) // 最大过期时间，默认为-1
                .allowedHeaders("*");
    }
}

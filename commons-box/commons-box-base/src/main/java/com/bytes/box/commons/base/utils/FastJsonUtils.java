package com.bytes.box.commons.base.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.Jdk8DateCodec;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class FastJsonUtils {
    public static final SerializerFeature[] SERIALIZER_FEATURES;
    private static final SerializerFeature[] PRETTY_SERIALIZER_FEATURES;
    public static final FastJsonConfig PRETTY_FORMAT_CONFIG;
    public static final FastJsonConfig PRETTY_LOG_CONFIG;

    public FastJsonUtils() {
    }

    private static FastJsonConfig buildPrettyConfig(boolean needPettyFormat) {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        String datetimePattern = "yyyy-MM-dd HH:mm:ss:SSS";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datetimePattern);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        fastJsonConfig.setCharset(Charsets.UTF_8);
        fastJsonConfig.setDateFormat(datetimePattern);
        fastJsonConfig.setSerializeFilters(new SerializeFilter[0]);
        ArrayList<SerializerFeature> serializerFeatures = Lists.newArrayList(PRETTY_SERIALIZER_FEATURES);
        if (needPettyFormat) {
            serializerFeatures.add(SerializerFeature.PrettyFormat);
        }

        fastJsonConfig.setSerializerFeatures((SerializerFeature[]) serializerFeatures.stream().toArray((x$0) -> {
            return new SerializerFeature[x$0];
        }));
        SerializeConfig config = new SerializeConfig();
        config.put(LocalDateTime.class, (serializer, object, fieldName, fieldType, features) -> {
            if (object == null) {
                serializer.out.writeNull();
            } else {
                String value = ((LocalDateTime) object).format(dateTimeFormatter);
                serializer.out.writeString(value);
            }
        });
        config.put(LocalDate.class, (serializer, object, fieldName, fieldType, features) -> {
            if (object == null) {
                serializer.out.writeNull();
            } else {
                String value = ((LocalDate) object).format(dateFormatter);
                serializer.out.writeString(value);
            }
        });
        fastJsonConfig.setSerializeConfig(config);
        return fastJsonConfig;
    }

    public static String toJsonPettyLogString(Object object) {
        String res = JSONObject.toJSONString(object, PRETTY_LOG_CONFIG.getSerializeConfig(), PRETTY_LOG_CONFIG.getSerializeFilters(), PRETTY_LOG_CONFIG.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, PRETTY_LOG_CONFIG.getSerializerFeatures());
        return res;
    }

    public static String toJsonPettyFormatString(Object object) {
        return JSONObject.toJSONString(object, PRETTY_FORMAT_CONFIG.getSerializeConfig(), (SerializeFilter[]) null, PRETTY_FORMAT_CONFIG.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, PRETTY_FORMAT_CONFIG.getSerializerFeatures());
    }

    static {
        SERIALIZER_FEATURES = new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteEnumUsingName, SerializerFeature.SkipTransientField, SerializerFeature.DisableCircularReferenceDetect};
        PRETTY_SERIALIZER_FEATURES = new SerializerFeature[]{SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteEnumUsingName, SerializerFeature.SkipTransientField, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.SortField, SerializerFeature.MapSortField};
        PRETTY_FORMAT_CONFIG = buildPrettyConfig(true);
        PRETTY_LOG_CONFIG = buildPrettyConfig(false);
    }

    /**
     * 初始设置信息
     */
    public static void init() {

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


}

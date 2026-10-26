package com.campus.headlines.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一的 Jackson ObjectMapper 工厂，供 spring-mvc.xml 的 JSON 转换器引用。
 *
 * <p>解决三个默认行为带来的问题：</p>
 * <ol>
 *   <li>不注册 JavaTimeModule 时，{@code LocalDateTime} 会被序列化成
 *       {@code [2026,10,26,19,28,41]} 这种数组，前端没法直接用；</li>
 *   <li>即使注册了，默认输出 ISO 格式 {@code 2026-10-26T19:28:41}，带 T 且不含时区语义，
 *       这里统一成 {@code yyyy-MM-dd HH:mm:ss}；</li>
 *   <li>前端多传字段时默认会抛 UnrecognizedPropertyException，
 *       对“前端先行”的开发节奏不友好，这里关掉严格校验。</li>
 * </ol>
 *
 * <p>为什么用 static 方法而不是 {@code @Configuration} 类：本项目是 XML 配置驱动，
 * 用 {@code factory-method} 即可复用这个 ObjectMapper，不必为了一个 Bean 引入 JavaConfig。</p>
 */
public final class JacksonConfig {

    /** 统一的时间格式 */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 统一的日期格式 */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private JacksonConfig() {
    }

    /** 创建一个配置好的 ObjectMapper */
    public static ObjectMapper build() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

        SimpleModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(timeModule);
        // 时间一律输出字符串，不要时间戳数字
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 前端多传字段不报错
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // null 字段照常输出，前端可以据此判断“有没有这个值”
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        return mapper;
    }
}

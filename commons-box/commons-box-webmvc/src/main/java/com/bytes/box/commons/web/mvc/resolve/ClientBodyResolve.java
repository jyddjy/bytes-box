package com.bytes.box.commons.web.mvc.resolve;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.alibaba.fastjson.JSONObject;
import com.bytes.box.commons.base.annocation.EncryptIgnore;
import com.bytes.box.commons.base.encrypt.ApplicationEncryptContext;
import com.bytes.box.commons.base.encrypt.RequestHeaderContext;
import com.bytes.box.commons.base.encrypt.RsaCacheContext;
import com.bytes.box.commons.base.properties.EncryptProperties;
import com.bytes.box.commons.base.response.RestCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Import(value = {
        RsaCacheContext.class,
        ApplicationEncryptContext.class,
        ClientBodyResolve.RequestClientBodyResolve.class,
        ClientBodyResolve.ResponseClientBodyResolve.class}
)
public class ClientBodyResolve {

    public static final String CLIENT_BODY_HEADER_KEY = "encrypt-ctx";

    /**
     * request body handler
     */
    @ControllerAdvice
    @Order(0)
    static class RequestClientBodyResolve implements RequestBodyAdvice {

        private final ApplicationEncryptContext applicationEncryptContext;

        public RequestClientBodyResolve(ApplicationEncryptContext applicationEncryptContext) {
            this.applicationEncryptContext = applicationEncryptContext;
        }

        @Override
        public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
            return !methodParameter.getDeclaringClass().isAnnotationPresent(EncryptIgnore.class) &&
                    !methodParameter.getMethod().isAnnotationPresent(EncryptIgnore.class);
        }

        @Override
        public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> selectedConverterType) throws IOException {

            String body = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);
            if (StringUtils.isBlank(body)) {
                return HttpBodyMessage.builder().body(body).httpHeaders(inputMessage.getHeaders()).build();
            }

            EncryptProperties encryptProperties = applicationEncryptContext.getEncryptProperties();
            RsaCacheContext rsaCacheContext = applicationEncryptContext.getRsaCacheContext();

            RequestHeaderContext requestBoxContext = RequestHeaderContext.getRequestHeaderContext(inputMessage.getHeaders().getFirst(CLIENT_BODY_HEADER_KEY));

            log.info("[request] requestContext={} & serverIgnore={}", requestBoxContext, encryptProperties.getIgnore());
            if ((Objects.isNull(requestBoxContext) || BooleanUtils.isTrue(requestBoxContext.getReqIgnore()))
                    && BooleanUtils.isTrue(encryptProperties.getIgnore())) {
                return HttpBodyMessage.builder().body(body).httpHeaders(inputMessage.getHeaders()).build();
            }

            RSA rsa = rsaCacheContext.fetchRsa(requestBoxContext.getSource(), requestBoxContext.getProfile());
            try {
                String encryptBody = rsa.decryptStr(body, KeyType.PublicKey);
                return HttpBodyMessage.builder().body(encryptBody).httpHeaders(inputMessage.getHeaders()).build();
            } catch (Exception e) {
                throw RestCode.REQ_PARAM_ENCRYPT_ERROR.fetchException();
            }
        }

        @Override
        public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
            return body;
        }

        @Override
        public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
            return body;
        }

        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        static class HttpBodyMessage implements HttpInputMessage {

            private HttpHeaders httpHeaders;

            private String body;

            @Override
            public InputStream getBody() throws IOException {
                return IOUtils.toInputStream(body, "UTF-8");
            }

            @Override
            public HttpHeaders getHeaders() {
                return this.httpHeaders;
            }
        }

    }

    /**
     * response body handler
     */
    @ControllerAdvice
    @Order(0)
    static class ResponseClientBodyResolve implements ResponseBodyAdvice<Object> {

        private final ApplicationEncryptContext applicationEncryptContext;

        public ResponseClientBodyResolve(ApplicationEncryptContext applicationEncryptContext) {
            this.applicationEncryptContext = applicationEncryptContext;
        }

        @Override
        public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
            //基本类型忽略掉
            return !returnType.getDeclaringClass().isAnnotationPresent(EncryptIgnore.class)
                    && !returnType.getMethod().isAnnotationPresent(EncryptIgnore.class);
        }

        @Override
        public Object beforeBodyWrite(Object object, MethodParameter returnType, MediaType selectedContentType,
                                      Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                      ServerHttpRequest request, ServerHttpResponse response) {

            //基本类型可能有问题
            if (Objects.isNull(object)) {
                return null;
            }

            EncryptProperties encryptProperties = applicationEncryptContext.getEncryptProperties();
            RsaCacheContext rsaCacheContext = applicationEncryptContext.getRsaCacheContext();

            RequestHeaderContext requestBoxContext = RequestHeaderContext.getRequestHeaderContext(request.getHeaders().getFirst(CLIENT_BODY_HEADER_KEY));
            log.info("[response] requestContext={} & serverIgnore={}", requestBoxContext, encryptProperties.getIgnore());
            if ((Objects.isNull(requestBoxContext) || BooleanUtils.isTrue(requestBoxContext.getRespIgnore()))
                    && BooleanUtils.isTrue(encryptProperties.getIgnore())) {
                return object;
            }

            RSA rsa = rsaCacheContext.fetchRsa(requestBoxContext.getSource(), requestBoxContext.getProfile());
            try {
                final String responseBody = JSONObject.toJSONString(object);
                return rsa.encryptBase64(responseBody, KeyType.PublicKey);
            } catch (Exception e) {
                throw RestCode.RESP_PARAM_ENCRYPT_ERROR.fetchException();
            }
        }
    }

}

package com.bytes.box.commons.web.flux.resolve;


import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.bytes.box.commons.base.Keys;
import com.bytes.box.commons.base.encrypt.ApplicationEncryptContext;
import com.bytes.box.commons.base.encrypt.RequestHeaderContext;
import com.bytes.box.commons.base.encrypt.RsaCacheContext;
import com.bytes.box.commons.base.properties.EncryptProperties;
import com.bytes.box.commons.base.response.DefaultException;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.bytes.box.commons.base.response.RestCode.REQ_PARAM_ENCRYPT_ERROR;
import static com.bytes.box.commons.base.response.RestCode.RESP_PARAM_ENCRYPT_ERROR;

/**
 * TODO 理论上不使用这种处理
 */
@Slf4j
@Import(value = {
        RsaCacheContext.class,
        ApplicationEncryptContext.class,
        WebFluxClientBodyResolve.RequestClientBodyResolve.class,
        WebFluxClientBodyResolve.ResponseClientBodyResolve.class
})
public class WebFluxClientBodyResolve {

    private static DataBuffer resolveStringToBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }

    private static String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        return bodyRef.get();
    }

    @Getter
    @Order(Integer.MIN_VALUE)
    static class RequestClientBodyResolve implements WebFilter {

        private final ApplicationEncryptContext applicationEncryptContext;

        public RequestClientBodyResolve(ApplicationEncryptContext applicationEncryptContext) {
            this.applicationEncryptContext = applicationEncryptContext;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain chain) {
            ServerHttpRequest serverHttpRequest = serverWebExchange.getRequest();
            final String body = resolveBodyFromRequest(serverHttpRequest);
            if (StringUtils.isBlank(body)) {
                return chain.filter(serverWebExchange);
            }

            EncryptProperties encryptProperties = applicationEncryptContext.getEncryptProperties();
            RsaCacheContext rsaCacheContext = applicationEncryptContext.getRsaCacheContext();
            RequestHeaderContext requestBoxContext = RequestHeaderContext.getRequestHeaderContext(
                    serverHttpRequest.getHeaders().getFirst(Keys.CLIENT_BODY_HEADER_KEY));
            log.info("[request] requestContext={} & serverIgnore={}", requestBoxContext, encryptProperties.getIgnore());
            if ((Objects.isNull(requestBoxContext) || BooleanUtils.isTrue(requestBoxContext.getReqIgnore()))
                    && BooleanUtils.isTrue(encryptProperties.getIgnore())) {
                return chain.filter(serverWebExchange);
            }

            RSA rsa = rsaCacheContext.fetchRsa(requestBoxContext.getSource(), requestBoxContext.getProfile());
            try {
                String encryptBody = rsa.decryptStr(body, KeyType.PublicKey);

                //创建新得request信息
                ServerHttpRequest request = serverHttpRequest.mutate().uri(serverHttpRequest.getURI()).build();
                DataBuffer bodyDataBuffer = resolveStringToBuffer(encryptBody);
                Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);
                ServerHttpRequestDecorator newRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return bodyFlux;
                    }
                };
                return chain.filter(serverWebExchange.mutate().request(newRequest).build());
            } catch (Exception e) {
                throw new DefaultException(REQ_PARAM_ENCRYPT_ERROR);
            }
        }
    }

    @Getter
    @Order
    static class ResponseClientBodyResolve implements WebFilter {

        private final ApplicationEncryptContext applicationEncryptContext;

        public ResponseClientBodyResolve(ApplicationEncryptContext applicationEncryptContext) {
            this.applicationEncryptContext = applicationEncryptContext;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain chain) {

            final ServerHttpRequest serverHttpRequest = serverWebExchange.getRequest();
            final ServerHttpResponse serverHttpResponse = serverWebExchange.getResponse();
            String respException = serverHttpResponse.getHeaders().getFirst(WebFluxExceptionResolve.RESP_EXCEPTION);
            if (StringUtils.isNotBlank(respException)) {
                return chain.filter(serverWebExchange);
            }

            EncryptProperties encryptProperties = applicationEncryptContext.getEncryptProperties();
            RsaCacheContext rsaCacheContext = applicationEncryptContext.getRsaCacheContext();
            RequestHeaderContext requestBoxContext = RequestHeaderContext.getRequestHeaderContext(
                    serverHttpRequest.getHeaders().getFirst(Keys.CLIENT_BODY_HEADER_KEY));

            log.info("[response] requestContext={} & serverIgnore={}", requestBoxContext, encryptProperties.getIgnore());
            if ((Objects.isNull(requestBoxContext) || BooleanUtils.isTrue(requestBoxContext.getReqIgnore()))
                    && BooleanUtils.isTrue(encryptProperties.getIgnore())) {
                return chain.filter(serverWebExchange);
            }

            RSA rsa = rsaCacheContext.fetchRsa(requestBoxContext.getSource(), requestBoxContext.getProfile());
            try {

                //创建新得request信息
                ServerHttpResponseDecorator newResponse = new ServerHttpResponseDecorator(serverHttpResponse) {
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        //TODO
                        DataBufferUtils.join(body).doOnNext(dataBuffer -> {
                            String responseBody = dataBuffer.toString(StandardCharsets.UTF_8);
                            String encryptBase64 = rsa.encryptBase64(responseBody, KeyType.PublicKey);
                        });
                        return super.writeWith(body);
                    }
                };
                return chain.filter(serverWebExchange.mutate().response(newResponse).build());
            } catch (Exception e) {
                throw new DefaultException(RESP_PARAM_ENCRYPT_ERROR);
            }
        }
    }
}

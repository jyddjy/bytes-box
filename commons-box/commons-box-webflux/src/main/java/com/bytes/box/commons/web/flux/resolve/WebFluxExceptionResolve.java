package com.bytes.box.commons.web.flux.resolve;

import com.bytes.box.commons.base.response.DefaultException;
import com.bytes.box.commons.base.response.RestCode;
import com.bytes.box.commons.base.response.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * webflux exception handler
 */
@Slf4j
@Order(Integer.MIN_VALUE)
@ConditionalOnMissingBean(WebExceptionHandler.class)
public class WebFluxExceptionResolve implements WebExceptionHandler, EnvironmentAware {

    public static final String RESP_EXCEPTION = "RESP-EXCEPTION";

    private Environment environment;

    public String getApplicationName() {
        return environment.getProperty("spring.application.name");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("", ex);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(RESP_EXCEPTION, RESP_EXCEPTION);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        if ((ex instanceof ResponseStatusException)) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            final String code = String.valueOf(rse.getStatus().value());
            return this.response(exchange, code, RestCode.getMessage(code));
        } else if (ex instanceof DefaultException) {
            DefaultException ge = (DefaultException) ex;
            return this.response(exchange, ge.getRestCode().getCode(), ge.getRestCode().getMessage());
        }
        return Mono.empty();
    }

    public Mono<Void> response(ServerWebExchange serverWebExchange, String code, String message) {
        String path = serverWebExchange.getRequest().getPath().value();
        RestResponse response = RestResponse.error(code, message, path, getApplicationName());
        return response(serverWebExchange, response);
    }

    public Mono<Void> response(ServerWebExchange serverWebExchange, RestResponse response) {
        DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap(response.toJson().getBytes());
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

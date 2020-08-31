package com.bytes.box.commons.web.mvc.hystrix;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Callable;

/**
 * 透传 org.springframework.web.context.request.RequestAttributes
 * @Date v1.0 2019/9/20 6:31 PM
 * @see BfsRequestAttributesHystrixConcurrencyStrategy
 */
public class RequestAttributesCallable<V> implements Callable<V> {

    private final Callable<V> delegate;

    private final RequestAttributes requestAttributes;

    public RequestAttributesCallable(RequestAttributes requestAttributes, Callable<V> delegate) {
        this.requestAttributes = requestAttributes;
        this.delegate = delegate;
    }

    @Override
    public V call() throws Exception {
        RequestContextHolder.setRequestAttributes(requestAttributes);
        try {
            return this.delegate.call();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}

package com.bytes.box.commons.base.resolve;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.bytes.box.commons.base.Keys;
import com.bytes.box.commons.base.encrypt.ApplicationEncryptContext;
import com.bytes.box.commons.base.encrypt.RequestHeaderContext;
import com.bytes.box.commons.base.encrypt.RsaCacheContext;
import com.bytes.box.commons.base.hystrix.BfsRequestAttributesHystrixConcurrencyStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import feign.Client;
import feign.Request;
import feign.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.util.StreamUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;


/**
 * 代理默认得client设置，可以重写默认得client，设置对应得属性信息
 */
@Slf4j
@Getter
@Import(value = {
        RsaCacheContext.class,
        BfsRequestAttributesHystrixConcurrencyStrategy.class,
        ApplicationEncryptContext.class}
)
public class FeignClientBodyResolve {

    @Bean
    @ConditionalOnMissingBean(Client.class)
    @Order(0)
    public Client client(ApplicationEncryptContext applicationEncryptContext) {
        return new BfsFeignClient(null, null, applicationEncryptContext);
    }

    @Getter
    static final class BfsFeignClient extends Client.Default {

        private final ApplicationEncryptContext applicationEncryptContext;

        /**
         * Null parameters imply platform defaults.
         * @param sslContextFactory
         * @param hostnameVerifier
         * @param applicationEncryptContext
         */
        public BfsFeignClient(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier, ApplicationEncryptContext applicationEncryptContext) {
            super(sslContextFactory, hostnameVerifier);
            this.applicationEncryptContext = applicationEncryptContext;
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            return this.resolveResponse(super.execute(this.resolveRequest(request), options));
        }

        private Response resolveResponse(Response response) throws IOException {

            final String body = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);

            final String applicationName = applicationEncryptContext.getApplicationName();
            final String profile = applicationEncryptContext.getActiveProfile();

            RSA rsa = applicationEncryptContext.getRsaCacheContext().fetchRsa(applicationName, profile);
            String decryptStr = rsa.decryptStr(body, KeyType.PrivateKey, StandardCharsets.UTF_8);

            //other message setting

            log.info(" feign response {}", decryptStr);
            return Response.builder().body(decryptStr, StandardCharsets.UTF_8)
                    .headers(response.headers())
                    .request(response.request())
                    .status(response.status())
                    .reason(response.reason())
                    .build();
        }

        private Request resolveRequest(Request request) {

            final String applicationName = applicationEncryptContext.getApplicationName();
            final String profile = applicationEncryptContext.getActiveProfile();

            //无关 trace 信息设置
            RSA rsa = applicationEncryptContext.getRsaCacheContext().fetchRsa(applicationName, profile);
            String encryptBase64 = rsa.encryptBase64(request.requestBody().asString(), KeyType.PrivateKey);
            RequestHeaderContext requestHeaderContext = RequestHeaderContext.builder()
                    .source(applicationName)
                    .profile(profile)
                    .respIgnore(false)
                    .reqIgnore(false)
                    .ldt(LocalDateTime.now())
                    .build();

            Map<String, Collection<String>> headers = Maps.newHashMap(request.headers());
            headers.put(Keys.CLIENT_BODY_HEADER_KEY, ImmutableList.of(requestHeaderContext.toJson()));

            Request newRequest = Request.create(request.httpMethod(), request.url(), headers,
                    Request.Body.bodyTemplate(encryptBase64, StandardCharsets.UTF_8)
            );
            log.info("feign request : application={},profile={},header={}", applicationName, profile, newRequest.headers());
            return newRequest;
        }
    }
}

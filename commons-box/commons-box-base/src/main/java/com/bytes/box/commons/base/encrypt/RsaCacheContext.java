package com.bytes.box.commons.base.encrypt;

import cn.hutool.crypto.asymmetric.RSA;
import com.bytes.box.commons.base.properties.EncryptProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties(EncryptProperties.class)
@Slf4j
@Getter
@ConditionalOnMissingBean(RsaCacheContext.class)
public class RsaCacheContext {

    private EncryptProperties encryptProperties;

    public RsaCacheContext(EncryptProperties encryptProperties) {
        this.encryptProperties = encryptProperties;
    }

    private final Cache<String, RsaContent> cache = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .expireAfterAccess(24, TimeUnit.HOURS)
            .maximumSize(100)
            .build(new RsaReLoader(encryptProperties));

    static class RsaReLoader implements CacheLoader<String, RsaContent> {

        private final EncryptProperties encryptProperties;

        public RsaReLoader(EncryptProperties encryptProperties) {
            this.encryptProperties = encryptProperties;
        }

        @Nullable
        @Override
        public RsaContent load(@NonNull String key) throws Exception {

            String[] keys = key.split(":");
            final String source = keys[0], profile = keys[1];
            EncryptProperties.ProfileProp profileProp = encryptProperties.fetchProfileProp(source, profile);

            if (Objects.isNull(profileProp)) {
                return RsaContent.builder().source(source).profile(profile).build();
            }

            return RsaContent.builder().profile(profile).source(source)
                    .rsa(new RSA(null, profileProp.getPublicKey()))
                    .build();
        }

    }

    public RSA fetchRsa(String source, String profile) {
        EncryptProperties.ProfileProp profileProp = encryptProperties.fetchProfileProp(source, profile);
        final String publicKey = profileProp.getPublicKey();

        RsaContent newRsaContent = RsaContent.builder().profile(profile).source(source).build();
        RsaContent rsaContent = cache.getIfPresent(newRsaContent.key());
        if (Objects.nonNull(rsaContent) && StringUtils.equalsIgnoreCase(publicKey, rsaContent.getPublicKey())) {
            return rsaContent.getRsa();
        }
        newRsaContent.setRsa(new RSA(encryptProperties.getPrivateKey(), publicKey));
        cache.put(newRsaContent.key(), newRsaContent);
        return newRsaContent.getRsa();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RsaContent {

        private String source;

        private String profile;

        private RSA rsa;

        public String key() {
            return String.format("%s:%s", source, profile);
        }

        public String getPublicKey() {
            return Objects.isNull(rsa) ? null : rsa.getPublicKeyBase64();
        }
    }
}

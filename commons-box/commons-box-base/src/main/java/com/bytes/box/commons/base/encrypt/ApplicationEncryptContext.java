package com.bytes.box.commons.base.encrypt;

import cn.hutool.crypto.asymmetric.RSA;
import com.bytes.box.commons.base.properties.EncryptProperties;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Getter
@EnableConfigurationProperties(EncryptProperties.class)
@ConditionalOnMissingBean(ApplicationEncryptContext.class)
public class ApplicationEncryptContext implements EnvironmentAware, InitializingBean {

    private Environment environment;

    private RSA selfRsa;

    private final EncryptProperties encryptProperties;

    private final RsaCacheContext rsaCacheContext;

    public ApplicationEncryptContext(EncryptProperties encryptProperties, RsaCacheContext rsaCacheContext) {
        this.encryptProperties = encryptProperties;
        this.rsaCacheContext = rsaCacheContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getApplicationName() {
        return environment.getProperty("spring.application.name");
    }

    public String getActiveProfile() {
        return environment.getActiveProfiles()[0];
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        List<EncryptProperties.ProfileProp> profileProps =
                encryptProperties.getPublicKeys().getOrDefault(getApplicationName(), ImmutableList.of());

        if (CollectionUtils.isEmpty(profileProps)) {
            return;
        }
        selfRsa = this.rsaCacheContext.fetchRsa(getApplicationName(), getActiveProfile());
    }
}

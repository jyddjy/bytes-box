package com.bytes.box.commons.base.properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ConfigurationProperties(prefix = "support.box.encrypt")
@RefreshScope
public class EncryptProperties {

    private String privateKey;

    private Boolean ignore;

    private Map<String, List<ProfileProp>> publicKeys = ImmutableMap.of();

    public ProfileProp fetchProfileProp(String source, String profile) {

        if (Objects.isNull(publicKeys)) {
            return ProfileProp.builder().build();
        }

        List<ProfileProp> profileProps = publicKeys.getOrDefault(source, ImmutableList.of());
        return profileProps.stream().filter(profileProp -> StringUtils.equalsIgnoreCase(profileProp.getProfile(), profile))
                .findFirst().orElse(ProfileProp.builder().build());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProfileProp {

        private String profile;

        private String publicKey;
    }
}

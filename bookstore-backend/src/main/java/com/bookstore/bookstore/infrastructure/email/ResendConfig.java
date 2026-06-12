package com.bookstore.bookstore.infrastructure.email;

import com.bookstore.bookstore.shared.util.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({OtpProperties.class, ResendProperties.class})
public class ResendConfig {

    @Bean
    public RestClient resendRestClient(RestClient.Builder builder, ResendProperties resendProperties) {
        String baseUrl = StringUtils.trimToNull(resendProperties.baseUrl());
        return baseUrl == null ? builder.build() : builder.baseUrl(baseUrl).build();
    }
}

package com.sellect.server.image.config;

import com.sellect.server.image.config.properties.TusProperties;
import me.desair.tus.server.TusFileUploadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TusConfiguration {

    @Bean
    public TusFileUploadService tusFileUploadService(TusProperties properties) {
        return new TusFileUploadService()
            .withStoragePath(properties.getStoragePath())
            .withUploadUri(properties.getUploadUri())
            .withUploadExpirationPeriod(properties.getUploadExpiration());
    }
}

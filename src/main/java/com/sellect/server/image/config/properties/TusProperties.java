package com.sellect.server.image.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("storage.tus")
public class TusProperties {

    private String storagePath;
    private String uploadUri;
    private Long uploadExpiration;

}

package io.retak.can.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties("config.security")
public class JwtConfigurationProperties {
    private String prefix;
    private String secret;
    private long validity;
}

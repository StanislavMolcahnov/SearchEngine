package main.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties("custom")
@Getter
@Setter
public class Config {
    private List<SiteData> sites;
    private String userAgent;
    private String referrer;

    @Getter
    @Setter
    public static class SiteData {

        private String url;
        private String name;
    }
}

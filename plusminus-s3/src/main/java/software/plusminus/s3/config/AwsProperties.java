package software.plusminus.s3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("aws")
public class AwsProperties {

    private String accessKey;
    private String secretKey;
    private String region;

}

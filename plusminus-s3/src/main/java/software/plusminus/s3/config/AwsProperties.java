package software.plusminus.s3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("aws")
public class AwsProperties {

    /**
     * Static AWS access key. Leave unset (the default) to resolve credentials through the default
     * AWS credentials provider chain, which is the recommended approach. Setting it here places
     * the credential in plaintext configuration and is discouraged for production.
     */
    private String accessKey;
    /**
     * Static AWS secret key. See {@link #accessKey} regarding plaintext credentials; prefer the
     * default AWS credentials provider chain instead of setting this.
     */
    private String secretKey;
    private String region;

}

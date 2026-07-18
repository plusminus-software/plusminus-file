package software.plusminus.s3.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.plusminus.file.service.FileService;
import software.plusminus.s3.service.S3FileService;

@Configuration
@EnableConfigurationProperties({AwsProperties.class, S3Properties.class})
public class S3Autoconfig {

    @ConditionalOnMissingBean(S3Client.class)
    @Bean
    public S3Client s3Client(AwsProperties properties) {
        S3ClientBuilder builder = S3Client.builder();
        if (StringUtils.hasText(properties.getRegion())) {
            builder.region(Region.of(properties.getRegion()));
        }
        if (StringUtils.hasText(properties.getAccessKey())
                && StringUtils.hasText(properties.getSecretKey())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
        }
        return builder.build();
    }

    @ConditionalOnMissingBean(FileService.class)
    @Bean
    public S3FileService s3FileService(S3Client s3Client, S3Properties s3Properties) {
        return new S3FileService(s3Client, s3Properties);
    }
}

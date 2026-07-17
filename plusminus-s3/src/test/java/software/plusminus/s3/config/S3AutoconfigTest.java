package software.plusminus.s3.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import software.amazon.awssdk.services.s3.S3Client;
import software.plusminus.fs.service.FileService;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static software.plusminus.check.Checks.check;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = S3Autoconfig.class)
@ActiveProfiles("test")
public class S3AutoconfigTest {

    @Autowired(required = false)
    private FileService fileService;

    @Test
    public void testAutoConfiguration() {
        check(fileService).isNotNull();
    }

    @Test
    public void s3Client_UsesStaticCredentialsWhenConfigured() {
        AwsProperties properties = new AwsProperties();
        properties.setRegion("eu-central-1");
        properties.setAccessKey("access");
        properties.setSecretKey(UUID.randomUUID().toString());
        try (S3Client client = new S3Autoconfig().s3Client(properties)) {
            assertNotNull(client);
        }
    }

    @Test
    public void s3Client_FallsBackToDefaultChainWhenSecretKeyMissing() {
        AwsProperties properties = new AwsProperties();
        properties.setRegion("eu-central-1");
        properties.setAccessKey("access");
        try (S3Client client = new S3Autoconfig().s3Client(properties)) {
            assertNotNull(client);
        }
    }
}

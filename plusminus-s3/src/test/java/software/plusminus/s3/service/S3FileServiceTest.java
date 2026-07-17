package software.plusminus.s3.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.plusminus.s3.config.S3Properties;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.plusminus.check.Checks.check;

@RunWith(MockitoJUnitRunner.class)
public class S3FileServiceTest {

    private static final byte[] DATA = "data".getBytes(StandardCharsets.UTF_8);

    @Mock
    private S3Client s3Client;

    @Test
    public void upload_SetsPublicReadAclWhenConfigured() {
        S3FileService service = service(true);

        URL result = service.upload("file.txt", new ByteArrayInputStream(DATA), DATA.length);

        PutObjectRequest request = capturePutRequest();
        check(request.acl()).is(ObjectCannedACL.PUBLIC_READ);
        check(result.toString()).is("https://bucket.s3.eu-central-1.amazonaws.com/file.txt");
    }

    @Test
    public void upload_LeavesAclEmptyByDefault() {
        S3FileService service = service(false);

        service.upload("file.txt", new ByteArrayInputStream(DATA), DATA.length);

        PutObjectRequest request = capturePutRequest();
        check(request.acl()).isNull();
        check(request.contentType()).is("text/plain");
        check(request.contentLength()).is(DATA.length);
    }

    private S3FileService service(boolean publicRead) {
        S3Properties properties = new S3Properties();
        properties.setBucketName("bucket");
        properties.setPublicRead(publicRead);
        S3Utilities utilities = S3Utilities.builder().region(Region.EU_CENTRAL_1).build();
        when(s3Client.utilities()).thenReturn(utilities);
        return new S3FileService(s3Client, properties);
    }

    private PutObjectRequest capturePutRequest() {
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        return captor.getValue();
    }
}

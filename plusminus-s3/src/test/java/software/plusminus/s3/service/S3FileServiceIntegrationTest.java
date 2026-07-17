package software.plusminus.s3.service;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.plusminus.s3.config.S3Autoconfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static software.plusminus.check.Checks.check;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = S3Autoconfig.class)
@ContextConfiguration(initializers = S3FileServiceIntegrationTest.MinioInitializer.class)
@ActiveProfiles("test")
@SuppressWarnings("PMD.CloseResource")
public class S3FileServiceIntegrationTest {

    private static final byte[] DATA = "some data".getBytes(Charset.defaultCharset());
    private static final String FOLDER = "my/path";
    private static final String BUCKET = "plusminus-test";
    private static final String REGION = "eu-central-1";

    private static final MinIOContainer MINIO = new MinIOContainer(
            DockerImageName.parse("minio/minio:RELEASE.2023-09-04T19-57-37Z"));

    @Autowired
    private S3FileService s3FileService;
    @Autowired
    private S3Client s3Client;

    @BeforeClass
    public static void beforeClass() {
        MINIO.start();
    }

    @AfterClass
    public static void afterClass() {
        MINIO.stop();
    }

    @After
    public void after() {
        listFiles().forEach(key -> s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(BUCKET)
                .key(key)
                .build()));
    }

    @Test
    public void download_ReadsFileFromS3Bucket() {
        String filepath = FOLDER + '/' + UUID.randomUUID();
        putFile(filepath);

        InputStream downloaded = s3FileService.download(filepath);

        check(downloaded).is(DATA);
    }

    @Test
    public void upload_CreatesFileInS3Bucket() {
        InputStream inputStream = new ByteArrayInputStream(DATA);
        String filepath = FOLDER + '/' + UUID.randomUUID();

        s3FileService.upload(filepath, inputStream, DATA.length);

        InputStream actual = getFile(filepath);
        check(actual).is(DATA);
    }

    @Test
    public void upload_SetsContentTypeForKnownExtension() {
        InputStream inputStream = new ByteArrayInputStream(DATA);
        String filepath = FOLDER + '/' + UUID.randomUUID() + ".txt";

        s3FileService.upload(filepath, inputStream, DATA.length);

        ResponseInputStream<GetObjectResponse> actual = getFile(filepath);
        check(actual.response().contentType()).is("text/plain");
        check(actual).is(DATA);
    }

    @Test
    public void upload_ReturnsCorrectFilepath() {
        InputStream inputStream = new ByteArrayInputStream(DATA);
        String filepath = FOLDER + '/' + UUID.randomUUID();

        URL url = s3FileService.upload(filepath, inputStream, DATA.length);

        check(url.toString()).is(MINIO.getS3URL() + '/' + BUCKET + '/' + filepath);
    }

    @Test
    public void upload_UpdatesFileInS3Bucket() {
        String filepath = FOLDER + '/' + UUID.randomUUID();
        putFile(filepath);
        byte[] updatedData = "some updated data".getBytes(Charset.defaultCharset());

        s3FileService.upload(filepath, new ByteArrayInputStream(updatedData), updatedData.length);

        InputStream actual = getFile(filepath);
        check(actual).is(updatedData);
    }

    @Test
    public void remove_RemovesFileFromS3Bucket() {
        String filepath = FOLDER + '/' + UUID.randomUUID();
        putFile(filepath);

        s3FileService.remove(filepath);

        check(listFiles()).isEmpty();
    }

    @Test
    public void getAllFilePaths_ReturnsFullFilepaths() {
        String filepath1 = FOLDER + '/' + UUID.randomUUID();
        String filepath2 = FOLDER + '/' + UUID.randomUUID();
        String filepath3 = FOLDER + '/' + UUID.randomUUID();
        putFile(filepath1);
        putFile(filepath2);
        putFile(filepath3);

        List<String> filepaths = s3FileService.getAllFilePaths(FOLDER);

        check(filepaths).containsExactly(filepath1, filepath2, filepath3);
    }

    private void putFile(String filepath) {
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(BUCKET)
                .key(filepath)
                .build(), RequestBody.fromBytes(DATA));
    }

    private ResponseInputStream<GetObjectResponse> getFile(String filepath) {
        return s3Client.getObject(GetObjectRequest.builder()
                .bucket(BUCKET)
                .key(filepath)
                .build());
    }

    private List<String> listFiles() {
        return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                        .bucket(BUCKET)
                        .prefix(FOLDER)
                        .build())
                .contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    static class MinioInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            S3Client s3Client = S3Client.builder()
                    .endpointOverride(URI.create(MINIO.getS3URL()))
                    .region(Region.of(REGION))
                    .forcePathStyle(true)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(MINIO.getUserName(), MINIO.getPassword())))
                    .build();
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(BUCKET).build());
            } catch (NoSuchBucketException e) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
            }
            applicationContext.getBeanFactory().registerSingleton("s3Client", s3Client);
        }
    }
}

package software.plusminus.s3.service;

import lombok.AllArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.plusminus.file.exception.FileNotFoundException;
import software.plusminus.file.service.FileService;
import software.plusminus.s3.config.S3Properties;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class S3FileService implements FileService {

    private S3Client s3Client;
    private S3Properties s3Properties;

    @Override
    public InputStream download(String filepath) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(filepath)
                .build();
        try {
            return s3Client.getObject(request);
        } catch (NoSuchKeyException e) {
            throw new FileNotFoundException("File not found: " + filepath, e);
        }
    }

    @Override
    public URL upload(String filepath, InputStream data, long contentLength) {
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(filepath)
                .contentLength(contentLength);
        String contentType = URLConnection.guessContentTypeFromName(filepath);
        if (contentType != null) {
            request.contentType(contentType);
        }
        if (s3Properties.isPublicRead()) {
            request.acl(ObjectCannedACL.PUBLIC_READ);
        }
        s3Client.putObject(request.build(), RequestBody.fromInputStream(data, contentLength));
        return s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(filepath)
                .build());
    }

    @Override
    public void remove(String filepath) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(filepath)
                .build();
        s3Client.deleteObject(request);
    }

    @Override
    public List<String> getAllFilePaths(String folderPath) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Properties.getBucketName())
                .prefix(toFolderPrefix(folderPath))
                .build();
        return s3Client.listObjectsV2Paginator(request).contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    /**
     * Normalizes a folder path into an S3 key prefix with a trailing delimiter so that only keys
     * inside that folder match. Without this, prefix "my/path" would also match a sibling folder
     * such as "my/path2". An empty/null folder path lists the whole bucket.
     */
    private String toFolderPrefix(String folderPath) {
        if (folderPath == null || folderPath.isEmpty() || folderPath.endsWith("/")) {
            return folderPath == null ? "" : folderPath;
        }
        return folderPath + '/';
    }
}

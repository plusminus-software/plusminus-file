# plusminus-file
Easy work with files in a Java application.

## Modules
 - **plusminus-file** — base module: the `FileService` abstraction, file controller,
   and autoconfiguration.
 - **plusminus-s3** — S3 implementation of `FileService`, built on AWS SDK for Java v2.
   Depends on `plusminus-file`.

## Usage
Add the implementation you want — the base module comes transitively:

```xml
<dependency>
    <groupId>software.plusminus</groupId>
    <artifactId>plusminus-s3</artifactId>
    <version>...</version>
</dependency>
```

How the `/files` controller behaves depends on the consuming application:

 - **Servlet web application** (has `spring-boot-starter-web`): the controller is registered
   **by default**. Set `plusminus.file.controller.enabled=false` if you only want `FileService`.
 - **Non-web application**: no controller and no web server — the web dependency is `optional`
   and is not pulled in transitively. `FileService` works as a plain storage service.

### Endpoints
 - `GET /files/{path}` — streams the file stored under `path` (nested paths are supported).
 - `POST /files/{folder}` — multipart upload (`file` part); stores the file as
   `folder/originalFilename` and returns its URL.

## Configuration
| Property | Default | Description |
|---|---|---|
| `plusminus.file.controller.enabled` | `true` | Registers the `/files` controller; set to `false` to switch it off. |
| `s3.bucketName` | — | Bucket used by `S3FileService` (required). |
| `s3.publicRead` | `false` | If `true`, uploads get the `public-read` canned ACL. Requires ACLs to be enabled on the bucket. |
| `aws.region` | — | Region; if empty, the AWS default region provider chain is used. |
| `aws.accessKey` / `aws.secretKey` | — | Static credentials; if empty, the AWS default credentials provider chain (IAM roles, env vars, profiles) is used — recommended for production. |

A custom `S3Client` bean can be defined to take full control of the client configuration.

## Security
The `/files` endpoints ship without authentication or upload-size limits — the consuming
application is responsible for securing them (e.g. Spring Security rules) and for
configuring `spring.servlet.multipart.max-file-size`.

## Test running
The `plusminus-s3` integration tests spin up a local, S3-compatible
[MinIO](https://min.io/) server via [Testcontainers](https://testcontainers.com/),
so no AWS account is needed — just a running Docker daemon on the machine (or CI runner).

## Building

Requires JDK 8. Build with the Maven wrapper:

```bash
./mvnw clean install
```

The build enforces Checkstyle, PMD, SpotBugs and JaCoCo coverage checks.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

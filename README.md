# plusminus-file
Easy work with files in a Java application.

## Modules
 - **plusminus-fs** — Java filesystem abstraction (`FileService`).
 - **plusminus-s3** — S3 implementation of the abstraction, built on AWS SDK for Java v2.
 - **plusminus-file** — File controller and autoconfiguration on top of the filesystem abstraction.

## Usage
Add `plusminus-file` together with an implementation of the abstraction
(the implementation dependency is `optional`, so pick one explicitly):

```xml
<dependency>
    <groupId>software.plusminus</groupId>
    <artifactId>plusminus-file</artifactId>
    <version>...</version>
</dependency>
<dependency>
    <groupId>software.plusminus</groupId>
    <artifactId>plusminus-s3</artifactId>
    <version>...</version>
</dependency>
```

### Endpoints
 - `GET /files/{path}` — streams the file stored under `path` (nested paths are supported).
 - `POST /files/{folder}` — multipart upload (`file` part); stores the file as
   `folder/originalFilename` and returns its URL.

The controller is enabled by default; set `plusminus.file.controller.enabled=false` to switch it off
(e.g. to expose your own endpoints on top of `FileService`).

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

package software.plusminus.file.controller;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import software.plusminus.file.service.FileService;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.http.HttpServletRequest;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "plusminus.file.controller.enabled", matchIfMissing = true)
@RestController
@RequestMapping("/files")
@AllArgsConstructor
public class FileController {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private FileService service;

    @GetMapping("/**")
    public ResponseEntity<Resource> download(HttpServletRequest request) {
        String filepath = sanitizeFilepath(extractFilepath(request));
        String contentType = URLConnection.guessContentTypeFromName(filepath);
        MediaType mediaType = contentType == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);
        String filename = filepath.substring(filepath.lastIndexOf('/') + 1);
        ContentDisposition disposition = ContentDisposition.builder("attachment")
                .filename(filename)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(service.download(filepath)));
    }

    @PostMapping("/**")
    public URL upload(HttpServletRequest request,
                      @RequestParam("file") MultipartFile file) throws IOException {
        String folder = extractFilepath(request);
        String filename = sanitizeFilename(file.getOriginalFilename());
        String filepath = folder.isEmpty()
                ? filename
                : folder + '/' + filename;
        return service.upload(filepath, file.getInputStream(), file.getSize());
    }

    private String extractFilepath(HttpServletRequest request) {
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        return PATH_MATCHER.extractPathWithinPattern(pattern, path);
    }

    private String sanitizeFilepath(String filepath) {
        if (filepath.indexOf('\\') >= 0 || filepath.startsWith("/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        for (String segment : filepath.split("/")) {
            if ("..".equals(segment) || ".".equals(segment)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }
        }
        return filepath;
    }

    private String sanitizeFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file name is required");
        }
        String baseName = originalFilename.replace('\\', '/');
        int lastSlash = baseName.lastIndexOf('/');
        if (lastSlash >= 0) {
            baseName = baseName.substring(lastSlash + 1);
        }
        baseName = baseName.trim();
        if (baseName.isEmpty() || ".".equals(baseName) || "..".equals(baseName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid uploaded file name");
        }
        return baseName;
    }
}

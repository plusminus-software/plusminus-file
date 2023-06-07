package software.plusminus.file.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import software.plusminus.fs.service.FileService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Controller
@ConditionalOnMissingBean(FileApi.class)
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService service;

    @GetMapping("/{path}")
    @ResponseBody
    public InputStream download(@PathVariable String path) {
        return service.download(path);
    }

    @PostMapping("/{path}")
    @ResponseBody
    public URL upload(@PathVariable String path,
                      MultipartFile file) throws IOException {
        return service.upload(path + '/' + file.getOriginalFilename(),
                file.getInputStream());
    }
}

package software.plusminus.file.service;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public interface FileService {

    InputStream download(String filepath);

    URL upload(String filepath, InputStream data, long contentLength);

    void remove(String filepath);

    List<String> getAllFilePaths(String folderPath);

}

package software.plusminus.file.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import software.plusminus.fs.service.FileService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class FileControllerTest {

    @Mock
    private FileService service;
    private MockMvc mvc;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new FileController(service)).build();
    }

    @Test
    public void download_StreamsFileWithContentType() throws Exception {
        byte[] data = "data".getBytes(StandardCharsets.UTF_8);
        when(service.download("folder/photo.png")).thenReturn(new ByteArrayInputStream(data));
        mvc.perform(get("/files/folder/photo.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(data));
    }

    @Test
    public void download_FallsBackToOctetStreamForUnknownType() throws Exception {
        byte[] data = "data".getBytes(StandardCharsets.UTF_8);
        when(service.download("folder/file.unknown")).thenReturn(new ByteArrayInputStream(data));
        mvc.perform(get("/files/folder/file.unknown"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(data));
    }

    @Test
    public void upload_DelegatesToFileServiceWithFullPath() throws Exception {
        URL url = new URL("https://example.com/folder/photo.png");
        MockMultipartFile file = new MockMultipartFile("file", "photo.png",
                "image/png", "data".getBytes(StandardCharsets.UTF_8));
        when(service.upload(eq("folder/photo.png"), any(InputStream.class), eq(4L)))
                .thenReturn(url);
        mvc.perform(multipart("/files/folder").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string('"' + url.toString() + '"'));
    }

    @Test
    public void upload_UsesFilenameOnlyWhenNoFolderGiven() throws Exception {
        URL url = new URL("https://example.com/photo.png");
        MockMultipartFile file = new MockMultipartFile("file", "photo.png",
                "image/png", "data".getBytes(StandardCharsets.UTF_8));
        when(service.upload(eq("photo.png"), any(InputStream.class), eq(4L)))
                .thenReturn(url);
        mvc.perform(multipart("/files").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string('"' + url.toString() + '"'));
    }
}

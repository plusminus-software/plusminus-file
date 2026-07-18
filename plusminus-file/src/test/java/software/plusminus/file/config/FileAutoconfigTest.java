package software.plusminus.file.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import software.plusminus.file.controller.FileController;
import software.plusminus.file.service.FileService;

import static org.mockito.Mockito.mock;
import static software.plusminus.check.Checks.check;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {FileAutoconfig.class, FileAutoconfigTest.TestConfig.class})
@ActiveProfiles("test")
public class FileAutoconfigTest {

    @Autowired
    private FileController fileController;

    @Test
    public void testAutoConfiguration() {
        check(fileController).isNotNull();
    }

    @Configuration
    static class TestConfig {

        @Bean
        public FileService fileService() {
            return mock(FileService.class);
        }
    }

}

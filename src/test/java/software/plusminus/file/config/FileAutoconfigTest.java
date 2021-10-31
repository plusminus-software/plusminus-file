package software.plusminus.file.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import software.plusminus.file.controller.FileController;

import static software.plusminus.check.Checks.check;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = FileAutoconfig.class)
@ActiveProfiles("test")
public class FileAutoconfigTest {
    
    @Autowired
    private FileController fileController;
    
    @Test
    public void testAutoConfiguration() {
        check(fileController).isNotNull();
    }
    
}
package software.plusminus.file.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static software.plusminus.check.Checks.check;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = FileAutoconfig.class, properties = "plusminus.file.controller.enabled=false")
@ActiveProfiles("test")
public class FileAutoconfigDisabledTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void fileControllerIsSwitchedOff() {
        check(context.containsBean("fileController")).isFalse();
    }

}

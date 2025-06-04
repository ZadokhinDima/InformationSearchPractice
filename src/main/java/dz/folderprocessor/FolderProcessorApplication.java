package dz.folderprocessor;

import dz.folderprocessor.config.PathProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PathProps.class)
public class FolderProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FolderProcessorApplication.class, args);
    }

}

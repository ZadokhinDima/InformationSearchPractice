package dz.folderprocessor.reader;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class TxtReader implements FileReader {

    @Override
    public String readFile(Path filePath) throws IOException {
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    @Override
    public String getFileExtension() {
        return ".txt";
    }
}

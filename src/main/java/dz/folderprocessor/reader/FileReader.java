package dz.folderprocessor.reader;

import java.io.IOException;
import java.nio.file.Path;

public interface FileReader {

    /**
     * Reads the content of a file and returns it as a String.
     *
     * @param filePath the path to the file to be read
     * @return the content of the file as a String
     */
    String readFile(Path filePath) throws IOException;

    /**
     * Returns the file extension for the reader.
     *
     * @return the file extension handled by this reader
     */
    String getFileExtension();
}

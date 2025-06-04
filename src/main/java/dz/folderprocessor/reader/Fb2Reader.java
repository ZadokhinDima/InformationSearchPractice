package dz.folderprocessor.reader;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToTextContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class Fb2Reader implements FileReader {

    private final AutoDetectParser parser = new AutoDetectParser();

    @Override
    public String readFile(Path filePath) throws IOException {
        Metadata md = new Metadata();
        md.set(Metadata.CONTENT_TYPE, "application/x-fictionbook+xml");

        var textHandler = new ToTextContentHandler();      // no length limit
        try (TikaInputStream in = TikaInputStream.get(filePath)) {
            parser.parse(in, textHandler, md);
            return textHandler.toString();
        } catch (SAXException | TikaException e) {
            throw new IOException("Failed to parse FB2 file: " + filePath, e);
        }
    }

    @Override
    public String getFileExtension() {
        return ".fb2";
    }
}
package dz.folderprocessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessor {

    private final Vocabulary vocabulary;

    public void processFile(Path filePath) throws IOException {
        log.info("Start processing file: {}", filePath.getFileName());
        long start = System.currentTimeMillis();
        String text = readText(filePath);
        var words = getWords(text);
        vocabulary.addAll(words);
        long duration = System.currentTimeMillis() - start;
        log.info("Processed file: {}, words: {}, duration: {} ms",
                filePath.getFileName(), words.size(), duration);
    }

    private Set<String> getWords(String text) throws IOException {
        var result = new HashSet<String>();
        try (
                Analyzer analyzer = new StandardAnalyzer();
                TokenStream ts = analyzer.tokenStream("content", text)
        ) {
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                result.add(term.toString());
            }
        }
        return result;
    }

    private String readText(Path filePath) throws IOException {
        if (Files.isDirectory(filePath)) {
            throw new IOException("Cannot read text from a directory: " + filePath);
        }
        if (!Files.exists(filePath)) {
            throw new IOException("File does not exist: " + filePath);
        }
        if (Files.isRegularFile(filePath) && filePath.toString().endsWith(".txt")) {
            return readTextFromTxt(filePath);
        }
        if (Files.isRegularFile(filePath) && filePath.toString().endsWith(".fb2")) {
            return readTextFromFb2(filePath);
        }
        throw new IOException("Unsupported file type or not a regular file: " + filePath);
    }

    private String readTextFromTxt(Path filePath) throws IOException {
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    private String readTextFromFb2(Path filePath) throws IOException {
        throw new UnsupportedOperationException("FB2 file reading not implemented yet");
    }
}

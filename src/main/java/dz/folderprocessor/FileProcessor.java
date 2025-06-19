package dz.folderprocessor;

import dz.folderprocessor.events.TermReadEvent;
import dz.folderprocessor.events.BigramReadEvent;
import dz.folderprocessor.reader.FileReader;
import dz.folderprocessor.data.DocumentRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessor {

    private final DocumentRegistry documentRegistry;
    private final ApplicationEventPublisher eventPublisher;
    private final List<FileReader> readers;

    public void processFile(Path filePath) throws IOException {
        log.info("Start processing file: {}", filePath.getFileName());

        long start = System.currentTimeMillis();
        String text = readFile(filePath);
        processFile(filePath, text);

        log.info("Finished processing file: {}, duration: {} ms",
                filePath.getFileName(), System.currentTimeMillis() - start);
    }

    private void processFile(Path path, String text) throws IOException {
        var docId = documentRegistry.registerDocument(path.toString());

        try (
                Analyzer analyzer = new StandardAnalyzer();
                TokenStream ts = analyzer.tokenStream("content", text)
        ) {
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            OffsetAttribute offset = ts.addAttribute(OffsetAttribute.class);
            ts.reset();
            
            String previousTerm = null;
            int previousWordPosition = 0;
            int wordPosition = 0;
            
            while (ts.incrementToken()) {
                String currentTerm = term.toString();
                
                eventPublisher.publishEvent(new TermReadEvent(this, currentTerm, path.toString(), docId, wordPosition));
                
                if (previousTerm != null) {
                    String bigram = previousTerm + " " + currentTerm;
                    eventPublisher.publishEvent(new BigramReadEvent(this, bigram, path.toString(), docId, previousWordPosition));
                }
                
                previousTerm = currentTerm;
                previousWordPosition = wordPosition;
                wordPosition++;
            }
        }
    }

    private String readFile(Path filePath) throws IOException {
        return readers.stream().filter(reader -> filePath.toString().endsWith(reader.getFileExtension()))
                .findFirst()
                .orElseThrow(() -> new IOException("No suitable reader found for file: " + filePath))
                .readFile(filePath);
    }
}

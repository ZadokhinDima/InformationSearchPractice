package dz.folderprocessor.scheduled;

import dz.folderprocessor.config.PathProps;
import dz.folderprocessor.data.Dictionary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class VocabularySaver {
    private final PathProps pathProps;
    private final Dictionary dictionary;

    private int previousHash;

    @Scheduled(fixedRate = 60000)
    public void saveVocabularyToDisk() {
        var words = dictionary.getVocabulary();
        var currentHash = words.hashCode();

        if (currentHash == previousHash) {
            log.info("Vocabulary has not changed, skipping save.");
            return;
        }

        previousHash = currentHash;
        try {
            var filePath = pathProps.vocabulary().resolve("vocabulary.txt");
            Files.write(filePath, words);
            log.info("Vocabulary saved to {}", filePath);
        } catch (Exception e) {
            log.error("Failed to save vocabulary", e);
        }

    }
}

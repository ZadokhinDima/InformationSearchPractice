package dz.folderprocessor;

import dz.folderprocessor.data.WordPairIndex;
import dz.folderprocessor.data.DocumentRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "path.scan=test-input",
    "path.vocabulary=test-processed"
})
class BigramIndexTest {

    @Autowired
    private FileProcessor fileProcessor;
    
    @Autowired
    private WordPairIndex wordPairIndex;
    
    @Autowired
    private DocumentRegistry documentRegistry;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(tempDir.resolve("test-input"));
        Files.createDirectories(tempDir.resolve("test-processed"));
    }
    
    @Test
    void testBigramIndexWithMultipleFiles() throws IOException, InterruptedException {
        Path file1 = tempDir.resolve("test-input/file1.txt");
        Path file2 = tempDir.resolve("test-input/file2.txt");
        Path file3 = tempDir.resolve("test-input/file3.txt");
        
        Files.write(file1, "java programming language is great".getBytes());
        Files.write(file2, "programming language tutorial for beginners".getBytes());
        Files.write(file3, "java programming tutorial and examples".getBytes());
        
        fileProcessor.processFile(file1);
        fileProcessor.processFile(file2);
        fileProcessor.processFile(file3);
        
        Thread.sleep(1000);
        
        Map<Integer, List<Integer>> javaProgrammingPositions = wordPairIndex.getDocumentsWithPositions("java programming");
        assertFalse(javaProgrammingPositions.isEmpty(), "Bigram 'java programming' should be found in index");
        assertEquals(2, javaProgrammingPositions.size(), "Bigram 'java programming' should appear in 2 documents");
        
        Map<Integer, List<Integer>> programmingLanguagePositions = wordPairIndex.getDocumentsWithPositions("programming language");
        assertEquals(2, programmingLanguagePositions.size(), "Bigram 'programming language' should appear in 2 documents");
        
        Map<Integer, List<Integer>> languageTutorialPositions = wordPairIndex.getDocumentsWithPositions("language tutorial");
        assertEquals(1, languageTutorialPositions.size(), "Bigram 'language tutorial' should appear in 1 document");
        
        for (Map.Entry<Integer, List<Integer>> entry : javaProgrammingPositions.entrySet()) {
            assertFalse(entry.getValue().isEmpty(), "Each document should have position information for java programming");
        }
        
        System.out.println("=== Bigram Index Test Results ===");
        System.out.println("Bigram 'java programming' positions: " + javaProgrammingPositions);
        System.out.println("Bigram 'programming language' positions: " + programmingLanguagePositions);
        System.out.println("Bigram 'language tutorial' positions: " + languageTutorialPositions);
    }
    
    @Test
    void testBigramPositionAccuracy() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/bigram-test.txt");
        
        Files.write(testFile, "first word second word third word second word".getBytes());
        
        fileProcessor.processFile(testFile);
        
        Thread.sleep(500);
        
        Map<Integer, List<Integer>> secondWordPositions = wordPairIndex.getDocumentsWithPositions("second word");
        assertFalse(secondWordPositions.isEmpty(), "Bigram 'second word' should be found");
        
        List<Integer> positions = secondWordPositions.values().iterator().next();
        assertEquals(2, positions.size(), "Bigram 'second word' should appear twice");
        
        Map<Integer, List<Integer>> wordSecondPositions = wordPairIndex.getDocumentsWithPositions("word second");
        assertEquals(1, wordSecondPositions.size(), "Bigram 'word second' should appear once");
        
        System.out.println("=== Bigram Position Accuracy Test Results ===");
        System.out.println("Bigram 'second word' positions: " + secondWordPositions);
        System.out.println("Bigram 'word second' positions: " + wordSecondPositions);
    }
    
    @Test
    void testBigramPhraseSearch() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/phrase-test.txt");
        
        Files.write(testFile, "machine learning algorithms are powerful tools for data analysis".getBytes());
        
        fileProcessor.processFile(testFile);
        
        Thread.sleep(500);
        
        assertTrue(wordPairIndex.containsWordPair("machine learning"), "Should contain 'machine learning' word pair");
        assertTrue(wordPairIndex.containsWordPair("learning algorithms"), "Should contain 'learning algorithms' word pair");
        assertTrue(wordPairIndex.containsWordPair("data analysis"), "Should contain 'data analysis' word pair");
        assertFalse(wordPairIndex.containsWordPair("machine algorithms"), "Should not contain 'machine algorithms' word pair");
        
        Map<Integer, List<Integer>> machineLearningPositions = wordPairIndex.getDocumentsWithPositions("machine learning");
        assertFalse(machineLearningPositions.isEmpty(), "Machine learning bigram should have positions");
        
        System.out.println("=== Bigram Phrase Search Test Results ===");
        System.out.println("Bigram 'machine learning' positions: " + machineLearningPositions);
    }
}
package dz.folderprocessor;

import dz.folderprocessor.query.PhrasalQueryProcessor;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "path.scan=test-input",
    "path.vocabulary=test-processed"
})
class PhrasalQueryTest {

    @Autowired
    private FileProcessor fileProcessor;
    
    @Autowired
    private PhrasalQueryProcessor phrasalQueryProcessor;
    
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
    void testPhrasalQueryWithExactMatch() throws IOException, InterruptedException {
        Path file1 = tempDir.resolve("test-input/file1.txt");
        Path file2 = tempDir.resolve("test-input/file2.txt");
        Path file3 = tempDir.resolve("test-input/file3.txt");
        
        Files.write(file1, "machine learning algorithms are powerful".getBytes());
        Files.write(file2, "learning machine algorithms and data".getBytes());
        Files.write(file3, "machine learning is the future".getBytes());
        
        fileProcessor.processFile(file1);
        fileProcessor.processFile(file2);
        fileProcessor.processFile(file3);
        
        Thread.sleep(1000);
        
        List<String> results = phrasalQueryProcessor.processPhrasalQuery("machine learning algorithms");
        
        assertFalse(results.isEmpty(), "Should find documents with exact phrase");
        assertTrue(results.contains(file1.toString()), "File1 should contain the exact phrase");
        assertFalse(results.contains(file2.toString()), "File2 should not match (wrong order)");
        
        System.out.println("=== Phrasal Query Exact Match Results ===");
        System.out.println("Query: 'machine learning algorithms'");
        System.out.println("Results: " + results);
    }
    
    @Test
    void testPhrasalQueryWithPartialMatch() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/partial.txt");
        
        Files.write(testFile, "java programming language tutorial for beginners".getBytes());
        
        fileProcessor.processFile(testFile);
        
        Thread.sleep(500);
        
        List<String> results1 = phrasalQueryProcessor.processPhrasalQuery("java programming language");
        List<String> results2 = phrasalQueryProcessor.processPhrasalQuery("programming language tutorial");
        List<String> results3 = phrasalQueryProcessor.processPhrasalQuery("language tutorial for");
        
        assertTrue(results1.contains(testFile.toString()), "Should find 'java programming language'");
        assertTrue(results2.contains(testFile.toString()), "Should find 'programming language tutorial'");
        assertTrue(results3.contains(testFile.toString()), "Should find 'language tutorial for'");
        
        System.out.println("=== Phrasal Query Partial Match Results ===");
        System.out.println("Query 1: 'java programming language' -> " + results1.size() + " results");
        System.out.println("Query 2: 'programming language tutorial' -> " + results2.size() + " results");
        System.out.println("Query 3: 'language tutorial for' -> " + results3.size() + " results");
    }
    
    @Test
    void testPhrasalQueryNoMatch() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/nomatch.txt");
        
        Files.write(testFile, "artificial intelligence and neural networks".getBytes());
        
        fileProcessor.processFile(testFile);
        
        Thread.sleep(500);
        
        List<String> results = phrasalQueryProcessor.processPhrasalQuery("machine learning algorithms");
        
        assertTrue(results.isEmpty(), "Should not find non-existent phrase");
        
        System.out.println("=== Phrasal Query No Match Results ===");
        System.out.println("Query: 'machine learning algorithms'");
        System.out.println("Results: " + results + " (should be empty)");
    }
    
    @Test
    void testPhrasalQueryWithSingleWord() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/single.txt");
        
        Files.write(testFile, "programming is fun".getBytes());
        
        fileProcessor.processFile(testFile);
        
        Thread.sleep(500);
        
        List<String> results = phrasalQueryProcessor.processPhrasalQuery("programming");
        
        assertTrue(results.isEmpty(), "Single word queries should return empty (need at least 2 words for bigrams)");
        
        System.out.println("=== Phrasal Query Single Word Results ===");
        System.out.println("Query: 'programming'");
        System.out.println("Results: " + results + " (should be empty for single words)");
    }
    
    @Test
    void testPhrasalQueryWithMultipleFiles() throws IOException, InterruptedException {
        Path file1 = tempDir.resolve("test-input/multi1.txt");
        Path file2 = tempDir.resolve("test-input/multi2.txt");
        Path file3 = tempDir.resolve("test-input/multi3.txt");
        
        Files.write(file1, "data science is growing field".getBytes());
        Files.write(file2, "data science applications in business".getBytes());
        Files.write(file3, "science data mining techniques".getBytes());
        
        fileProcessor.processFile(file1);
        fileProcessor.processFile(file2);
        fileProcessor.processFile(file3);
        
        Thread.sleep(1000);
        
        List<String> results = phrasalQueryProcessor.processPhrasalQuery("data science");
        
        assertEquals(2, results.size(), "Should find 2 files with 'data science' phrase");
        assertTrue(results.contains(file1.toString()), "File1 should contain 'data science'");
        assertTrue(results.contains(file2.toString()), "File2 should contain 'data science'");
        assertFalse(results.contains(file3.toString()), "File3 should not match (wrong order)");
        
        System.out.println("=== Phrasal Query Multiple Files Results ===");
        System.out.println("Query: 'data science'");
        System.out.println("Results: " + results);
    }
}
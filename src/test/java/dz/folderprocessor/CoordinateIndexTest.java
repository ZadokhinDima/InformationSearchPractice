package dz.folderprocessor;

import dz.folderprocessor.data.InverseIndex;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "path.scan=test-input",
    "path.vocabulary=test-processed"
})
class CoordinateIndexTest {

    @Autowired
    private FileProcessor fileProcessor;
    
    @Autowired
    private InverseIndex inverseIndex;
    
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
    void testCoordinateIndexWithMultipleFiles() throws IOException, InterruptedException {
        Path file1 = tempDir.resolve("test-input/file1.txt");
        Path file2 = tempDir.resolve("test-input/file2.txt");
        Path file3 = tempDir.resolve("test-input/file3.txt");
        
        Files.write(file1, "hello world java programming".getBytes());
        Files.write(file2, "java is great programming language".getBytes());
        Files.write(file3, "hello programming world of java".getBytes());
        
        fileProcessor.processFile(file1);
        fileProcessor.processFile(file2);
        fileProcessor.processFile(file3);
        
        Thread.sleep(1000);
        
        Map<Integer, List<Integer>> javaPositions = inverseIndex.getDocumentsWithPositions("java");
        assertFalse(javaPositions.isEmpty(), "Java term should be found in index");
        assertEquals(3, javaPositions.size(), "Java should appear in 3 documents");
        
        Map<Integer, List<Integer>> helloPositions = inverseIndex.getDocumentsWithPositions("hello");
        assertEquals(2, helloPositions.size(), "Hello should appear in 2 documents");
        
        Map<Integer, List<Integer>> programmingPositions = inverseIndex.getDocumentsWithPositions("programming");
        assertEquals(3, programmingPositions.size(), "Programming should appear in 3 documents");
        
        for (Map.Entry<Integer, List<Integer>> entry : javaPositions.entrySet()) {
            assertFalse(entry.getValue().isEmpty(), "Each document should have position information for java");
        }
        
        System.out.println("=== Coordinate Index Test Results ===");
        System.out.println("Term 'java' positions: " + javaPositions);
        System.out.println("Term 'hello' positions: " + helloPositions);
        System.out.println("Term 'programming' positions: " + programmingPositions);
    }
    
    @Test
    void testPositionAccuracy() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/position-test.txt");
        
        Files.write(testFile, "first second third second fourth".getBytes());
        
        fileProcessor.processFile(testFile);
        
        Thread.sleep(500);
        
        Map<Integer, List<Integer>> secondPositions = inverseIndex.getDocumentsWithPositions("second");
        assertFalse(secondPositions.isEmpty(), "Second term should be found");
        
        List<Integer> positions = secondPositions.values().iterator().next();
        assertEquals(2, positions.size(), "Term 'second' should appear twice");

        assertThat(positions).containsExactlyInAnyOrder(1, 3);
        
        System.out.println("=== Position Accuracy Test Results ===");
        System.out.println("Term 'second' positions: " + secondPositions);
    }
}
package dz.folderprocessor;

import dz.folderprocessor.data.SuffixIndex;
import dz.folderprocessor.events.TermReadEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "path.scan=test-input",
    "path.vocabulary=test-processed"
})
class SuffixIndexTest {

    @Autowired
    private SuffixIndex suffixIndex;

    @BeforeEach
    void setUp() {
        publishTermEvents();
    }

    private void publishTermEvents() {
        suffixIndex.handleTermRead(new TermReadEvent(this, "programming", "test1.txt", 1, 0));
        suffixIndex.handleTermRead(new TermReadEvent(this, "testing", "test1.txt", 1, 1));
        suffixIndex.handleTermRead(new TermReadEvent(this, "debugging", "test2.txt", 2, 0));
        suffixIndex.handleTermRead(new TermReadEvent(this, "running", "test2.txt", 2, 1));
        suffixIndex.handleTermRead(new TermReadEvent(this, "coding", "test3.txt", 3, 0));
        suffixIndex.handleTermRead(new TermReadEvent(this, "learning", "test3.txt", 3, 1));
        suffixIndex.handleTermRead(new TermReadEvent(this, "hello", "test4.txt", 4, 0));
        suffixIndex.handleTermRead(new TermReadEvent(this, "world", "test4.txt", 4, 1));
    }

    @Test
    void testSuffixSearchWithIng() {
        List<String> results = suffixIndex.endsWith("ing");
        
        assertEquals(6, results.size(), "Should find 6 terms ending with 'ing'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        assertTrue(results.contains("testing"), "Should contain 'testing'");
        assertTrue(results.contains("debugging"), "Should contain 'debugging'");
        assertTrue(results.contains("running"), "Should contain 'running'");
        assertTrue(results.contains("coding"), "Should contain 'coding'");
        assertTrue(results.contains("learning"), "Should contain 'learning'");
        
        System.out.println("=== Suffix 'ing' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testSuffixSearchWithSpecificEnding() {
        List<String> results = suffixIndex.endsWith("lo");
        
        assertEquals(1, results.size(), "Should find 1 term ending with 'lo'");
        assertTrue(results.contains("hello"), "Should contain 'hello'");
        
        System.out.println("=== Suffix 'lo' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testSuffixSearchWithLongerSuffix() {
        List<String> results = suffixIndex.endsWith("gramming");
        
        assertEquals(1, results.size(), "Should find 1 term ending with 'gramming'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        
        System.out.println("=== Suffix 'gramming' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testSuffixSearchWithCommonEnding() {
        List<String> results = suffixIndex.endsWith("ning");
        
        assertEquals(3, results.size(), "Should find 3 terms ending with 'ning'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        assertTrue(results.contains("running"), "Should contain 'running'");
        assertTrue(results.contains("learning"), "Should contain 'learning'");
        
        System.out.println("=== Suffix 'ning' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testSuffixSearchNoMatches() {
        List<String> results = suffixIndex.endsWith("xyz");
        
        assertTrue(results.isEmpty(), "Should find no terms ending with 'xyz'");
        
        System.out.println("=== Suffix 'xyz' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testSuffixSearchSingleCharacter() {
        List<String> results = suffixIndex.endsWith("g");
        
        assertEquals(6, results.size(), "Should find 6 terms ending with 'g'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        assertTrue(results.contains("testing"), "Should contain 'testing'");
        assertTrue(results.contains("debugging"), "Should contain 'debugging'");
        assertTrue(results.contains("running"), "Should contain 'running'");
        assertTrue(results.contains("coding"), "Should contain 'coding'");
        assertTrue(results.contains("learning"), "Should contain 'learning'");
        
        System.out.println("=== Suffix 'g' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testSuffixSearchCaseSensitivity() {
        suffixIndex.handleTermRead(new TermReadEvent(this, "Testing", "test5.txt", 5, 0));
        
        List<String> results = suffixIndex.endsWith("ing");
        
        assertTrue(results.size() >= 6, "Should find at least 6 terms ending with 'ing'");
        assertTrue(results.contains("testing"), "Should contain lowercase 'testing'");
        
        System.out.println("=== Case Sensitivity Test Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testSuffixSearchEmptyString() {
        List<String> results = suffixIndex.endsWith("");
        
        assertEquals(8, results.size(), "Should find all terms when searching for empty suffix");
        
        System.out.println("=== Empty Suffix Results ===");
        System.out.println("Found terms: " + results);
    }
}
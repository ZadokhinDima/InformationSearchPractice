package dz.folderprocessor;

import dz.folderprocessor.data.PermutationIndex;
import dz.folderprocessor.events.TermReadEvent;
import dz.folderprocessor.query.WildcardQueryProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "path.scan=test-input",
    "path.vocabulary=test-processed"
})
class PermutationIndexTest {

    @Autowired
    private PermutationIndex permutationIndex;
    
    @Autowired
    private WildcardQueryProcessor wildcardQueryProcessor;

    @BeforeEach
    void setUp() {
        publishTermEvents();
    }

    private void publishTermEvents() {
        permutationIndex.handleTermRead(new TermReadEvent(this, "hello", "test1.txt", 1, 0));
        permutationIndex.handleTermRead(new TermReadEvent(this, "world", "test1.txt", 1, 1));
        permutationIndex.handleTermRead(new TermReadEvent(this, "programming", "test2.txt", 2, 0));
        permutationIndex.handleTermRead(new TermReadEvent(this, "language", "test2.txt", 2, 1));
        permutationIndex.handleTermRead(new TermReadEvent(this, "testing", "test3.txt", 3, 0));
        permutationIndex.handleTermRead(new TermReadEvent(this, "debugging", "test3.txt", 3, 1));
    }

    @Test
    void testWildcardSearchAtEnd() {
        Set<String> results = permutationIndex.searchByPattern("*ing");
        
        assertEquals(3, results.size(), "Should find 3 terms ending with 'ing'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        assertTrue(results.contains("testing"), "Should contain 'testing'");
        assertTrue(results.contains("debugging"), "Should contain 'debugging'");
        
        System.out.println("=== Wildcard '*ing' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testWildcardSearchAtBeginning() {
        Set<String> results = permutationIndex.searchByPattern("hel*");
        
        assertEquals(1, results.size(), "Should find 1 term starting with 'hel'");
        assertTrue(results.contains("hello"), "Should contain 'hello'");
        
        System.out.println("=== Wildcard 'hel*' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testWildcardSearchInMiddle() {
        Set<String> results = permutationIndex.searchByPattern("prog*ing");
        
        assertEquals(1, results.size(), "Should find 1 term matching 'prog*ing'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        
        System.out.println("=== Wildcard 'prog*ing' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testWildcardSearchWithShortPattern() {
        Set<String> results = permutationIndex.searchByPattern("w*d");
        
        assertEquals(1, results.size(), "Should find 1 term matching 'w*d'");
        assertTrue(results.contains("world"), "Should contain 'world'");
        
        System.out.println("=== Wildcard 'w*d' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testWildcardSearchNoMatches() {
        Set<String> results = permutationIndex.searchByPattern("xyz*");
        
        assertTrue(results.isEmpty(), "Should find no terms starting with 'xyz'");
        
        System.out.println("=== Wildcard 'xyz*' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testSubstringSearch() {
        Set<String> results = permutationIndex.getAllTermsContaining("amm");
        
        assertEquals(1, results.size(), "Should find 1 term containing 'amm'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        
        System.out.println("=== Substring 'amm' Results ===");
        System.out.println("Found terms: " + results);
    }

    @Test
    void testEmptyWildcardPattern() {
        Set<String> results = permutationIndex.searchByPattern("hello");
        
        assertTrue(results.isEmpty(), "Should return empty set for pattern without wildcard");
    }

    @Test
    void testMultipleWildcardPattern() {
        Set<String> results = permutationIndex.searchByPattern("h*l*o");
        
        assertTrue(results.isEmpty(), "Should return empty set for multiple wildcards");
    }

    // WildcardQueryProcessor Tests
    
    @Test
    void testWildcardQueryProcessorSingleWildcard() {
        Set<String> results = wildcardQueryProcessor.queryPermutationIndex("*ing");
        
        assertEquals(3, results.size(), "Should find 3 terms ending with 'ing'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        assertTrue(results.contains("testing"), "Should contain 'testing'");
        assertTrue(results.contains("debugging"), "Should contain 'debugging'");
        
        System.out.println("=== WildcardQueryProcessor '*ing' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryProcessorMultipleWildcards() {
        // Add more test terms for complex patterns
        permutationIndex.handleTermRead(new TermReadEvent(this, "hello", "test4.txt", 4, 0));
        permutationIndex.handleTermRead(new TermReadEvent(this, "helping", "test4.txt", 4, 1));
        
        Set<String> results = wildcardQueryProcessor.queryPermutationIndex("hel*ing");
        
        assertEquals(1, results.size(), "Should find 1 term matching 'hel*ing'");
        assertTrue(results.contains("helping"), "Should contain 'helping'");
        
        System.out.println("=== WildcardQueryProcessor 'hel*ing' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryProcessorComplexMultipleWildcards() {
        // Add test terms with complex patterns
        permutationIndex.handleTermRead(new TermReadEvent(this, "programming", "test5.txt", 5, 0));
        permutationIndex.handleTermRead(new TermReadEvent(this, "processing", "test5.txt", 5, 1));
        
        Set<String> results = wildcardQueryProcessor.queryPermutationIndex("pro*ram*ing");
        
        assertEquals(1, results.size(), "Should find 1 term matching 'pro*ram*ing'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        
        System.out.println("=== WildcardQueryProcessor 'pro*ram*ing' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryProcessorNoWildcardThrowsException() {
        assertThrows(IllegalArgumentException.class, 
            () -> wildcardQueryProcessor.queryPermutationIndex("hello"),
            "Should throw exception for pattern without wildcard");
    }
    
    @Test
    void testWildcardQueryProcessorEmptyResults() {
        Set<String> results = wildcardQueryProcessor.queryPermutationIndex("xyz*abc");
        
        assertTrue(results.isEmpty(), "Should return empty set for non-matching pattern");
        
        System.out.println("=== WildcardQueryProcessor 'xyz*abc' Results ===");
        System.out.println("Found terms: " + results);
    }
}
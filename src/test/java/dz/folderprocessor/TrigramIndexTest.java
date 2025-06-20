package dz.folderprocessor;

import dz.folderprocessor.data.TrigramIndex;
import dz.folderprocessor.query.WildcardQueryProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "path.scan=test-input",
    "path.vocabulary=test-processed"
})
class TrigramIndexTest {

    @Autowired
    private FileProcessor fileProcessor;
    
    @Autowired
    private TrigramIndex trigramIndex;
    
    @Autowired
    private WildcardQueryProcessor wildcardQueryProcessor;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(tempDir.resolve("test-input"));
        Files.createDirectories(tempDir.resolve("test-processed"));
    }

    @Test
    void testSingleCharacterTerm() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/single-char.txt");
        Files.write(testFile, "a".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        assertTrue(trigramIndex.containsTrigram("  a"), "Should contain '  a' trigram");
        assertTrue(trigramIndex.containsTrigram(" a "), "Should contain ' a ' trigram");
        assertTrue(trigramIndex.containsTrigram("a  "), "Should contain 'a  ' trigram");

        Set<String> terms = trigramIndex.contains("  a");
        assertTrue(terms.contains("a"), "Should contain term 'a'");
    }

    @Test
    void testTwoCharacterTerm() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/two-char.txt");
        Files.write(testFile, "it".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        assertTrue(trigramIndex.containsTrigram("  i"), "Should contain '  i' trigram");
        assertTrue(trigramIndex.containsTrigram(" it"), "Should contain ' it' trigram");
        assertTrue(trigramIndex.containsTrigram(" it "), "Should contain ' it ' trigram");
        assertTrue(trigramIndex.containsTrigram("it "), "Should contain 'it ' trigram");
        assertTrue(trigramIndex.containsTrigram("t  "), "Should contain 't  ' trigram");

        Set<String> terms = trigramIndex.contains(" it");
        assertTrue(terms.contains("it"), "Should contain term 'it'");
    }

    @Test
    void testThreeCharacterTerm() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/three-char.txt");
        Files.write(testFile, "cat".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        assertTrue(trigramIndex.containsTrigram("  c"), "Should contain '  c' trigram");
        assertTrue(trigramIndex.containsTrigram(" ca"), "Should contain ' ca' trigram");
        assertTrue(trigramIndex.containsTrigram("cat"), "Should contain 'cat' trigram");
        assertTrue(trigramIndex.containsTrigram("at "), "Should contain 'at ' trigram");
        assertTrue(trigramIndex.containsTrigram("t  "), "Should contain 't  ' trigram");

        Set<String> terms = trigramIndex.contains("cat");
        assertTrue(terms.contains("cat"), "Should contain term 'cat'");
    }

    @Test
    void testLongTerm() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/long-term.txt");
        Files.write(testFile, "programming".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        // Test beginning trigrams
        assertTrue(trigramIndex.containsTrigram("  p"), "Should contain '  p' trigram");
        assertTrue(trigramIndex.containsTrigram(" pr"), "Should contain ' pr' trigram");
        
        // Test internal trigrams
        assertTrue(trigramIndex.containsTrigram("pro"), "Should contain 'pro' trigram");
        assertTrue(trigramIndex.containsTrigram("rog"), "Should contain 'rog' trigram");
        assertTrue(trigramIndex.containsTrigram("ogr"), "Should contain 'ogr' trigram");
        assertTrue(trigramIndex.containsTrigram("gra"), "Should contain 'gra' trigram");
        assertTrue(trigramIndex.containsTrigram("ram"), "Should contain 'ram' trigram");
        assertTrue(trigramIndex.containsTrigram("amm"), "Should contain 'amm' trigram");
        assertTrue(trigramIndex.containsTrigram("mmi"), "Should contain 'mmi' trigram");
        assertTrue(trigramIndex.containsTrigram("min"), "Should contain 'min' trigram");
        assertTrue(trigramIndex.containsTrigram("ing"), "Should contain 'ing' trigram");
        
        // Test ending trigrams
        assertTrue(trigramIndex.containsTrigram("ng "), "Should contain 'ng ' trigram");
        assertTrue(trigramIndex.containsTrigram("g  "), "Should contain 'g  ' trigram");

        Set<String> terms = trigramIndex.contains("ram");
        assertTrue(terms.contains("programming"), "Should contain term 'programming'");
    }

    @Test
    void testMultipleTermsWithCommonTrigrams() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/multiple-terms.txt");
        Files.write(testFile, "program programming programmer".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> termsWithPro = trigramIndex.contains("pro");
        assertTrue(termsWithPro.size() >= 3, "Should contain at least 3 terms with 'pro' trigram");
        assertTrue(termsWithPro.contains("program"), "Should contain 'program'");
        assertTrue(termsWithPro.contains("programming"), "Should contain 'programming'");
        assertTrue(termsWithPro.contains("programmer"), "Should contain 'programmer'");

        Set<String> termsWithGra = trigramIndex.contains("gra");
        assertTrue(termsWithGra.size() >= 3, "Should contain at least 3 terms with 'gra' trigram");

        Set<String> termsWithIng = trigramIndex.contains("ing");
        assertTrue(termsWithIng.size() >= 1, "Should contain at least 1 term with 'ing' trigram");
        assertTrue(termsWithIng.contains("programming"), "Should contain 'programming'");
    }

    @Test
    void testWildcardSearchScenario() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/wildcard-test.txt");
        Files.write(testFile, "java javascript jar jump".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        // Simulate wildcard search "ja*"
        Set<String> termsStartingWithJa = trigramIndex.contains(" ja");
        assertTrue(termsStartingWithJa.size() >= 3, "Should find at least 3 terms starting with 'ja'");
        assertTrue(termsStartingWithJa.contains("java"), "Should contain 'java'");
        assertTrue(termsStartingWithJa.contains("javascript"), "Should contain 'javascript'");
        assertTrue(termsStartingWithJa.contains("jar"), "Should contain 'jar'");

        // Simulate wildcard search "*ava*"
        Set<String> termsContainingAva = trigramIndex.contains("ava");
        assertTrue(termsContainingAva.size() >= 2, "Should find at least 2 terms containing 'ava'");
        assertTrue(termsContainingAva.contains("java"), "Should contain 'java'");
        assertTrue(termsContainingAva.contains("javascript"), "Should contain 'javascript'");
    }

    @Test
    void testEmptyTrigramQuery() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/empty-query.txt");
        Files.write(testFile, "test".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> terms = trigramIndex.contains("xyz");
        assertTrue(terms.isEmpty(), "Should return empty set for non-existent trigram");
        
        assertFalse(trigramIndex.containsTrigram("xyz"), "Should not contain non-existent trigram");
    }

    @Test
    void testTrigramGeneration() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/trigram-gen.txt");
        Files.write(testFile, "test".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        // For "test" we should have: "  t", " te", "tes", "est", "st ", "t  "
        assertTrue(trigramIndex.containsTrigram("  t"), "Should contain '  t'");
        assertTrue(trigramIndex.containsTrigram(" te"), "Should contain ' te'");
        assertTrue(trigramIndex.containsTrigram("tes"), "Should contain 'tes'");
        assertTrue(trigramIndex.containsTrigram("est"), "Should contain 'est'");
        assertTrue(trigramIndex.containsTrigram("st "), "Should contain 'st '");
        assertTrue(trigramIndex.containsTrigram("t  "), "Should contain 't  '");

        // Verify that "test" is contained in all its trigrams
        assertTrue(trigramIndex.contains("  t").contains("test"));
        assertTrue(trigramIndex.contains(" te").contains("test"));
        assertTrue(trigramIndex.contains("tes").contains("test"));
        assertTrue(trigramIndex.contains("est").contains("test"));
        assertTrue(trigramIndex.contains("st ").contains("test"));
        assertTrue(trigramIndex.contains("t  ").contains("test"));
    }

    // WildcardQueryProcessor Trigram Index Tests
    
    @Test
    void testWildcardQueryTrigramIndexPrefixSearch() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/wildcard-prefix.txt");
        Files.write(testFile, "programming program programmer".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> results = wildcardQueryProcessor.queryTrigramIndex("prog*");
        
        assertTrue(results.size() >= 3, "Should find at least 3 terms starting with 'prog'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        assertTrue(results.contains("program"), "Should contain 'program'");
        assertTrue(results.contains("programmer"), "Should contain 'programmer'");
        
        System.out.println("=== Trigram Wildcard 'prog*' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryTrigramIndexSuffixSearch() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/wildcard-suffix.txt");
        Files.write(testFile, "programming testing debugging".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> results = wildcardQueryProcessor.queryTrigramIndex("*ing");
        
        assertTrue(results.size() >= 3, "Should find at least 3 terms ending with 'ing'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        assertTrue(results.contains("testing"), "Should contain 'testing'");
        assertTrue(results.contains("debugging"), "Should contain 'debugging'");
        
        System.out.println("=== Trigram Wildcard '*ing' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryTrigramIndexMiddleWildcard() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/wildcard-middle.txt");
        Files.write(testFile, "javascript java language".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> results = wildcardQueryProcessor.queryTrigramIndex("java*");
        
        assertTrue(results.size() >= 2, "Should find at least 2 terms starting with 'java'");
        assertTrue(results.contains("javascript"), "Should contain 'javascript'");
        assertTrue(results.contains("java"), "Should contain 'java'");
        
        System.out.println("=== Trigram Wildcard 'java*' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryTrigramIndexComplexPattern() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/wildcard-complex.txt");
        Files.write(testFile, "programming processing program".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> results = wildcardQueryProcessor.queryTrigramIndex("pro*ram*");
        
        assertEquals(2, results.size(), "Should find 1 term matching 'pro*ram*'");
        assertTrue(results.contains("programming"), "Should contain 'programming'");
        assertTrue(results.contains("program"), "Should contain 'program'");
        
        System.out.println("=== Trigram Wildcard 'pro*ram*' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryTrigramIndexMultipleWildcards() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/wildcard-multiple.txt");
        Files.write(testFile, "programming processing procedural".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> results = wildcardQueryProcessor.queryTrigramIndex("pro*e*ing");
        
        assertEquals(1, results.size(), "Should find 1 term matching 'pro*e*ing'");
        assertTrue(results.contains("processing"), "Should contain 'processing'");
        
        System.out.println("=== Trigram Wildcard 'pro*e*ing' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryTrigramIndexNoWildcardThrowsException() {
        assertThrows(IllegalArgumentException.class, 
            () -> wildcardQueryProcessor.queryTrigramIndex("programming"),
            "Should throw exception for pattern without wildcard");
    }
    
    @Test
    void testWildcardQueryTrigramIndexNoMatches() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/wildcard-no-match.txt");
        Files.write(testFile, "java javascript programming".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> results = wildcardQueryProcessor.queryTrigramIndex("xyz*abc");
        
        assertTrue(results.isEmpty(), "Should return empty set for non-matching pattern");
        
        System.out.println("=== Trigram Wildcard 'xyz*abc' Results ===");
        System.out.println("Found terms: " + results);
    }
    
    @Test
    void testWildcardQueryTrigramIndexShortPatterns() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("test-input/wildcard-short.txt");
        Files.write(testFile, "go cat dog programming".getBytes());
        
        fileProcessor.processFile(testFile);
        Thread.sleep(500);

        Set<String> results = wildcardQueryProcessor.queryTrigramIndex("ca*");
        
        assertTrue(results.size() >= 1, "Should find at least 1 term starting with 'ca'");
        assertTrue(results.contains("cat"), "Should contain 'cat'");
        
        System.out.println("=== Trigram Wildcard 'ca*' Results ===");
        System.out.println("Found terms: " + results);
    }
}
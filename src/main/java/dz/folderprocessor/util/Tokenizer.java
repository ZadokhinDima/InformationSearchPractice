package dz.folderprocessor.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private static final Analyzer analyzer = new StandardAnalyzer();

    public static List<String> tokenizeInput(String token) {
        try (TokenStream ts = analyzer.tokenStream("content", token)) {
            List<String> result = new ArrayList<>();
            ts.reset();
            CharTermAttribute attr = ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
                result.add(attr.toString());
            }
            ts.end();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to analyze query term: " + token, e);
        }
    }

}

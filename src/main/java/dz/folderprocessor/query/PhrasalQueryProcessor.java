package dz.folderprocessor.query;

import dz.folderprocessor.data.*;
import dz.folderprocessor.util.SetUtil;
import dz.folderprocessor.util.Tokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class PhrasalQueryProcessor {

    private final InverseIndex inverseIndex;
    private final WordPairIndex wordPairIndex;
    private final DocumentRegistry documentRegistry;

    public List<String> containsPhraseCoordinateIndex(String phrase) {
        List<String> tokens = Tokenizer.tokenizeInput(phrase);

        if (tokens.size() < 2) {
            return Collections.emptyList();
        }

        return getCandidateDocuments(inverseIndex, tokens).stream().
                filter(docId -> hasSequentialTokens(docId, tokens, inverseIndex))
                .map(documentRegistry::getDocumentPath)
                .toList();
    }


    public List<String> containsPhrasePairIndex(String phrase) {
        List<String> tokens = Tokenizer.tokenizeInput(phrase);
        List<String> pairs = createPairs(tokens);
        
        if (pairs.isEmpty()) {
            return Collections.emptyList();
        }

        return getCandidateDocuments(wordPairIndex, pairs).stream()
                .filter(docId -> hasSequentialTokens(docId, pairs, wordPairIndex))
                .map(documentRegistry::getDocumentPath)
                .toList();
    }

    private List<String> createPairs(List<String> tokens) {
        if (tokens.size() < 2) {
            return Collections.emptyList();
        }
        
        List<String> bigrams = new ArrayList<>();
        for (int i = 0; i < tokens.size() - 1; i++) {
            bigrams.add(tokens.get(i) + " " + tokens.get(i + 1));
        }
        
        return bigrams;
    }

    private Set<Integer> getCandidateDocuments(WordSearchIndex index, List<String> tokens) {
        return SetUtil.intersection(tokens.stream().map(index::getDocuments));
    }

    private boolean hasSequentialTokens(Integer docId, List<String> tokens, PositionIndex index) {
        if (tokens.isEmpty()) {
            return true;
        }

        Set<Integer> startingPositions = index.getPositions(docId, tokens.getFirst());
        
        if (startingPositions == null || startingPositions.isEmpty()) {
            return false;
        }
        
        for (int i = 1; i < tokens.size(); i++) {
            Set<Integer> currentTokenPositions = index.getPositions(docId, tokens.get(i));
            
            if (currentTokenPositions == null || currentTokenPositions.isEmpty()) {
                return false;
            }
            

            final int offset = i;
            startingPositions.removeIf(pos -> !currentTokenPositions.contains(pos + offset));
            
            if (startingPositions.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
}
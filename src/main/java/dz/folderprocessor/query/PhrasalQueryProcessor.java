package dz.folderprocessor.query;

import dz.folderprocessor.data.BigramIndex;
import dz.folderprocessor.data.DocumentRegistry;
import dz.folderprocessor.util.Tokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class PhrasalQueryProcessor {

    private final BigramIndex bigramIndex;
    private final DocumentRegistry documentRegistry;

    public List<String> processPhrasalQuery(String phrase) {
        List<String> tokens = Tokenizer.tokenizeInput(phrase);
        List<String> bigrams = createBigrams(tokens);
        
        if (bigrams.isEmpty()) {
            return Collections.emptyList();
        }
        
        Set<Integer> candidateDocuments = getCandidateDocuments(bigrams);
        
        return candidateDocuments.stream()
                .filter(docId -> hasSequentialBigrams(docId, bigrams))
                .map(documentRegistry::getDocumentPath)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> createBigrams(List<String> tokens) {
        if (tokens.size() < 2) {
            return Collections.emptyList();
        }
        
        List<String> bigrams = new ArrayList<>();
        for (int i = 0; i < tokens.size() - 1; i++) {
            bigrams.add(tokens.get(i) + " " + tokens.get(i + 1));
        }
        
        return bigrams;
    }

    private Set<Integer> getCandidateDocuments(List<String> bigrams) {
        if (bigrams.isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<Integer> candidates = new HashSet<>(bigramIndex.getDocuments(bigrams.get(0)));
        
        for (int i = 1; i < bigrams.size(); i++) {
            candidates.retainAll(bigramIndex.getDocuments(bigrams.get(i)));
        }
        
        return candidates;
    }

    private boolean hasSequentialBigrams(Integer docId, List<String> bigrams) {
        if (bigrams.isEmpty()) {
            return true;
        }

        List<Integer> startingPositions = bigramIndex.getDocumentsWithPositions(bigrams.getFirst()).get(docId);
        
        if (startingPositions == null || startingPositions.isEmpty()) {
            return false;
        }
        
        Set<Integer> candidatePositions = new HashSet<>(startingPositions);
        
        for (int i = 1; i < bigrams.size(); i++) {
            Map<Integer, List<Integer>> currentBigramPositions = bigramIndex.getDocumentsWithPositions(bigrams.get(i));
            List<Integer> currentPositions = currentBigramPositions.get(docId);
            
            if (currentPositions == null || currentPositions.isEmpty()) {
                return false;
            }
            
            Set<Integer> currentPositionSet = new HashSet<>(currentPositions);
            final int offset = i;
            candidatePositions.removeIf(pos -> !currentPositionSet.contains(pos + offset));
            
            if (candidatePositions.isEmpty()) {
                return false;
            }
        }
        
        return !candidatePositions.isEmpty();
    }
}
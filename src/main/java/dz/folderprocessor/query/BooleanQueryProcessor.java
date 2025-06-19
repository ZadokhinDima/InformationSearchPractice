package dz.folderprocessor.query;

import dz.folderprocessor.data.DocumentRegistry;
import dz.folderprocessor.data.IncidentMatrix;
import dz.folderprocessor.util.Tokenizer;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class BooleanQueryProcessor {

    private final DocumentRegistry documentRegistry;
    private final IncidentMatrix incidentMatrix;

    private final Analyzer analyzer = new StandardAnalyzer();

    private static final Set<String> OPERATORS = Set.of("AND", "OR", "NOT");

    /**
     * Processes a query in Polish notation and returns a list of document pathes that match the query.
     * @param query - A query in Polish notation.
     *              term1 term2 AND term3 OR ~ (term1 AND term2) OR term3
     * @return A list of document IDs that match the query.
     */
    public List<String> processQuery(String query) {
        var maxFileId = documentRegistry.maxFileId();

        Deque<String> tokens = tokenize(query);
        Deque<BitSet> operationStack = new ArrayDeque<>();

        while (!tokens.isEmpty()) {
            String token = tokens.pop();

            if (OPERATORS.contains(token)) {
                switch (token) {
                    case "AND" -> {
                        BitSet b2 = operationStack.pop();
                        BitSet b1 = operationStack.pop();
                        b1.and(b2);
                        operationStack.push(b1);
                    }
                    case "OR" -> {
                        BitSet b2 = operationStack.pop();
                        BitSet b1 = operationStack.pop();
                        b1.or(b2);
                        operationStack.push(b1);
                    }
                    case "NOT" -> {
                        BitSet b = operationStack.pop();
                        BitSet not = new BitSet(maxFileId + 1);
                        not.set(0, maxFileId + 1);
                        not.andNot(b);
                        operationStack.push(not);
                    }
                }
            } else {
                List<String> normalized = Tokenizer.tokenizeInput(token);
                BitSet intersection = new BitSet();
                intersection.set(0, maxFileId + 1); // стартуємо з усіх одиниць

                for (String normTerm : normalized) {
                    BitSet b = incidentMatrix.getBitSetForTerm(normTerm);
                    intersection.and(b);
                }
                operationStack.push(intersection);
            }
        }

        BitSet resultSet = operationStack.pop();

        return resultSet.stream().mapToObj(documentRegistry::getDocumentPath).toList();
    }

    private Deque<String> tokenize(String query) {
        String[] parts = query.trim().split("\\s+");
        return new ArrayDeque<>(List.of(parts));
    }
}

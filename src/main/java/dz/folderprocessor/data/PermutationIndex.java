package dz.folderprocessor.data;

import dz.folderprocessor.events.TermReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PermutationIndex {

    private final TreeMap<String, Set<String>> permutationMap = new TreeMap<>();

    @EventListener
    public void handleTermRead(TermReadEvent event) {
        String term = event.getTerm().toLowerCase();
        
        generatePermutations(term);
    }

    private void generatePermutations(String term) {
        String termWithMarker = term + "$";
        
        for (int i = 0; i < termWithMarker.length(); i++) {
            String permutation = termWithMarker.substring(i) + termWithMarker.substring(0, i);
            permutationMap.computeIfAbsent(permutation, k -> new HashSet<>()).add(term);
        }
    }

    public Set<String> searchByPattern(String pattern) {
        if (!pattern.contains("*")) {
            return Set.of();
        }

        String[] parts = pattern.split("\\*", -1);
        if (parts.length != 2) {
            return Set.of();
        }

        String prefix = parts[0];
        String suffix = parts[1];
        
        String searchPattern = suffix + "$" + prefix;
        
        return findTermsStartingWith(searchPattern);
    }

    private Set<String> findTermsStartingWith(String prefix) {
        Set<String> result = new HashSet<>();
        
        String end = prefix + Character.MAX_VALUE;
        NavigableMap<String, Set<String>> subMap = permutationMap.subMap(prefix, true, end, false);
        
        for (Map.Entry<String, Set<String>> entry : subMap.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.addAll(entry.getValue());
            }
        }
        
        return result;
    }

    public Set<String> getAllTermsContaining(String substring) {
        Set<String> result = new HashSet<>();
        String pattern = substring + "$";
        
        for (Map.Entry<String, Set<String>> entry : permutationMap.entrySet()) {
            if (entry.getKey().contains(pattern)) {
                result.addAll(entry.getValue());
            }
        }
        
        return result;
    }
}
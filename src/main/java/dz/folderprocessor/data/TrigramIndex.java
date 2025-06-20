package dz.folderprocessor.data;

import dz.folderprocessor.events.TermReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TrigramIndex {

    private final Map<String, Set<String>> index;

    public TrigramIndex() {
        this.index = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleTermRead(TermReadEvent event) {
        String term = event.getTerm();
        Set<String> trigrams = generateTrigrams(term);
        
        for (String trigram : trigrams) {
            index.computeIfAbsent(trigram, k -> ConcurrentHashMap.newKeySet())
                    .add(term);
        }
    }

    private Set<String> generateTrigrams(String term) {
        Set<String> trigrams = new HashSet<>();
        
        if (term.length() == 1) {
            trigrams.add("  " + term);
            trigrams.add(" " + term + " ");
            trigrams.add(term + "  ");
        } else if (term.length() == 2) {
            trigrams.add("  " + term.charAt(0));
            trigrams.add(" " + term);
            trigrams.add(" " + term + " ");
            trigrams.add(term + " ");
            trigrams.add(term.charAt(1) + "  ");
        } else {
            // Add trigrams with spaces for beginning and end
            trigrams.add("  " + term.charAt(0));
            trigrams.add(" " + term.substring(0, 2));
            
            // Add all internal trigrams
            for (int i = 0; i <= term.length() - 3; i++) {
                trigrams.add(term.substring(i, i + 3));
            }
            
            // Add trigrams with spaces for end
            trigrams.add(term.substring(term.length() - 2) + " ");
            trigrams.add(term.charAt(term.length() - 1) + "  ");
        }
        
        return trigrams;
    }

    public Set<String> contains(String trigram) {
        return index.getOrDefault(trigram, Collections.emptySet());
    }

    public boolean containsTrigram(String trigram) {
        return index.containsKey(trigram);
    }
}
package dz.folderprocessor.data;

import dz.folderprocessor.events.TermReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.TreeSet;

@Component
public class SuffixIndex {

    private final TreeSet<String> reversedTerms = new TreeSet<>();

    @EventListener
    public void handleTermRead(TermReadEvent event) {
        String term = event.getTerm();

        // Reverse the term and add it to the set
        String reversedTerm = new StringBuilder(term).reverse().toString();
        reversedTerms.add(reversedTerm);
    }

    public List<String> endsWith(String suffix) {
        String reversedSuffix = new StringBuilder(suffix).reverse().toString();

        String end = reversedSuffix + Character.MAX_VALUE;
        return reversedTerms.subSet(reversedSuffix, true, end, true).stream()
                .map(s -> new StringBuilder(s).reverse().toString())
                .toList();
    }
}

package dz.folderprocessor.data;

import dz.folderprocessor.events.TermReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class PrefixIndex {

    private final NavigableSet<String> terms = new ConcurrentSkipListSet<>();

    @EventListener
    public void handleTermRead(TermReadEvent event) {
        String term = event.getTerm();

        // Add the term to the set
        terms.add(term);
    }

    public List<String> termsStartWith(String prefix) {
        String end = prefix + Character.MAX_VALUE;
        return terms.subSet(prefix, true, end, true).stream()
                .filter(term -> term.startsWith(prefix))
                .toList();
    }
}
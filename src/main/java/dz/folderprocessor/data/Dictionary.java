package dz.folderprocessor.data;

import dz.folderprocessor.events.TermReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class Dictionary {

    private final Map<String, AtomicLong> termFrequencies;

    public Dictionary() {
        termFrequencies = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleTermRead(TermReadEvent event) {
        String term = event.getTerm();
        termFrequencies.computeIfAbsent(term, k -> new AtomicLong(0)).incrementAndGet();
    }

    public Set<String> getVocabulary() {
        return Set.copyOf(termFrequencies.keySet());
    }
}

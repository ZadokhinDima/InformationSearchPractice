package dz.folderprocessor.data;

import dz.folderprocessor.events.TermReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InverseIndex {

    private final Map<String, Map<Integer, List<Integer>>> index;

    public InverseIndex() {
        this.index = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleTermRead(TermReadEvent event) {
        String term = event.getTerm();
        int fileId = event.getFileId();
        int position = event.getPosition();

        index.computeIfAbsent(term, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(fileId, k -> new ArrayList<>())
                .add(position);
    }

    public Map<Integer, List<Integer>> getDocumentsWithPositions(String term) {
        return index.getOrDefault(term, Collections.emptyMap());
    }

    public Set<Integer> getDocuments(String term) {
        return index.getOrDefault(term, Collections.emptyMap()).keySet();
    }
}

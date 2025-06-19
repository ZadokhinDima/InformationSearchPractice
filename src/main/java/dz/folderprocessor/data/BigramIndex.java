package dz.folderprocessor.data;

import dz.folderprocessor.events.BigramReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BigramIndex {

    private final Map<String, Map<Integer, List<Integer>>> index;

    public BigramIndex() {
        this.index = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleBigramRead(BigramReadEvent event) {
        String bigram = event.getBigram();
        int fileId = event.getFileId();
        int position = event.getPosition();

        index.computeIfAbsent(bigram, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(fileId, k -> new ArrayList<>())
                .add(position);
    }

    public Map<Integer, List<Integer>> getDocumentsWithPositions(String bigram) {
        return index.getOrDefault(bigram, Collections.emptyMap());
    }

    public Set<Integer> getDocuments(String bigram) {
        return index.getOrDefault(bigram, Collections.emptyMap()).keySet();
    }

    public boolean containsBigram(String bigram) {
        return index.containsKey(bigram);
    }
}
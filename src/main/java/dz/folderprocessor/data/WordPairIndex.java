package dz.folderprocessor.data;

import dz.folderprocessor.events.BigramReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WordPairIndex implements WordSearchIndex, PositionIndex {

    private final Map<String, Map<Integer, List<Integer>>> index;

    public WordPairIndex() {
        this.index = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleBigramRead(BigramReadEvent event) {
        String wordPair = event.getBigram();
        int fileId = event.getFileId();
        int position = event.getPosition();

        index.computeIfAbsent(wordPair, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(fileId, k -> new ArrayList<>())
                .add(position);
    }

    public Map<Integer, List<Integer>> getDocumentsWithPositions(String wordPair) {
        return index.getOrDefault(wordPair, Collections.emptyMap());
    }

    @Override
    public Set<Integer> getDocuments(String wordPair) {
        return index.getOrDefault(wordPair, Collections.emptyMap()).keySet();
    }

    public boolean containsWordPair(String wordPair) {
        return index.containsKey(wordPair);
    }

    @Override
    public Set<Integer> getPositions(Integer fileId, String term) {
        return new HashSet<>(index.get(term).get(fileId));
    }
}
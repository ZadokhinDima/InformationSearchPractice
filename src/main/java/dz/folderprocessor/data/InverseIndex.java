package dz.folderprocessor.data;

import dz.folderprocessor.events.TermReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InverseIndex {

    private final Map<String, Set<Integer>> index;

    public InverseIndex() {
        this.index = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleTermRead(TermReadEvent event) {
        String term = event.getTerm();
        int fileId = event.getFileId();

        index.computeIfAbsent(term,
                        k -> ConcurrentHashMap.newKeySet())
                .add(fileId);
    }
}

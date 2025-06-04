package dz.folderprocessor.data;

import dz.folderprocessor.events.TermReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IncidentMatrix {

    private final Map<String, BitSet> incidentMatrix;

    public IncidentMatrix() {
        this.incidentMatrix = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleTermRead(TermReadEvent event) {
        String term = event.getTerm();
        int fileId = event.getFileId();

        BitSet bitSet = incidentMatrix.computeIfAbsent(term, k -> new BitSet());

        bitSet.set(fileId);
    }

}

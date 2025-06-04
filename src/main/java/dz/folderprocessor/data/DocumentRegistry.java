package dz.folderprocessor.data;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DocumentRegistry {

    private final AtomicInteger idCounter;

    private final Map<String, Integer> pathToId;
    private final Map<Integer, String> idToPath;

    public DocumentRegistry() {
        this.idCounter = new AtomicInteger(0);
        this.pathToId = new ConcurrentHashMap<>();
        this.idToPath = new ConcurrentHashMap<>();
    }

    public int registerDocument(String path) {
        return pathToId.computeIfAbsent(path, p -> {
            int id = idCounter.getAndIncrement();
            idToPath.put(id, p);
            return id;
        });
    }

}

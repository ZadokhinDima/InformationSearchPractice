package dz.folderprocessor;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Vocabulary {

    private final Set<String> words;

    public Vocabulary() {
        this.words = ConcurrentHashMap.newKeySet();
    }

    public void addAll(Set<String> words) {
        this.words.addAll(words);
    }

    public Set<String> snapshot() {
        return Set.copyOf(words);
    }
}

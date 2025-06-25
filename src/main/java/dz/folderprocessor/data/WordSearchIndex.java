package dz.folderprocessor.data;

import java.util.Set;

public interface WordSearchIndex {
    Set<Integer> getDocuments(String term);
}

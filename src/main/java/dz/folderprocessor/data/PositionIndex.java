package dz.folderprocessor.data;

import java.util.Set;

public interface PositionIndex {
    Set<Integer> getPositions(Integer fileId, String term);
}

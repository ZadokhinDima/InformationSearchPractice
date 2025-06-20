package dz.folderprocessor.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class SetUtil {

    /**
     * Returns the intersection of two sets without modifying the original sets.
     * 
     * @param <T> the type of elements in the sets
     * @param set1 the first set
     * @param set2 the second set
     * @return a new set containing elements that are in both sets
     */
    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }


    /**
     * Returns the intersection of a collection of sets without modifying the original sets.
     * Returns an empty set if the collection is empty or if any set is empty.
     * 
     * @param <T> the type of elements in the sets
     * @param sets the collection of sets to intersect
     * @return a new set containing elements that are in all sets
     */
    public static <T> Set<T> intersection(Collection<Set<T>> sets) {
        if (sets.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<T> result = null;
        for (Set<T> set : sets) {
            if (set.isEmpty()) {
                return new HashSet<>(); // Any empty set makes intersection empty
            }
            
            if (result == null) {
                result = new HashSet<>(set);
            } else {
                result.retainAll(set);
                if (result.isEmpty()) {
                    return result; // Early exit if intersection becomes empty
                }
            }
        }
        
        return result;
    }

    /**
     * Returns the intersection of a stream of sets without modifying the original sets.
     * Uses stream reduce with pair intersection for functional approach.
     * Returns an empty set if the stream is empty.
     * 
     * @param <T> the type of elements in the sets
     * @param setsStream the stream of sets to intersect
     * @return a new set containing elements that are in all sets
     */
    public static <T> Set<T> intersection(Stream<Set<T>> setsStream) {
        return setsStream
                .reduce(SetUtil::intersection)
                .orElse(new HashSet<>());
    }
}
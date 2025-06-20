package dz.folderprocessor.query;

import dz.folderprocessor.data.PermutationIndex;
import dz.folderprocessor.data.PrefixIndex;
import dz.folderprocessor.data.SuffixIndex;
import dz.folderprocessor.data.TrigramIndex;
import dz.folderprocessor.util.SetUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WildcardQueryProcessor {

    private final PermutationIndex permutationIndex;
    private final TrigramIndex trigramIndex;
    private final PrefixIndex prefixIndex;
    private final SuffixIndex suffixIndex;

    /**
     * Processes wildcard queries with support for multiple wildcards.
     * Converts query to prefix*suffix format and applies additional filtering.
     * 
     * @param pattern The wildcard pattern to search for (e.g., "pre*mid*suf")
     * @return Set of terms matching the pattern
     */
    public Set<String> queryPermutationIndex(String pattern) {
        if (!pattern.contains("*")) {
            throw new IllegalArgumentException("Pattern must contain at least one wildcard (*)");
        }

        // Convert to prefix*suffix format
        String[] parts = pattern.split("\\*");
        String prefix = parts[0];
        String suffix = parts.length > 1 ? parts[parts.length - 1] : "";
        
        String simplifiedPattern = prefix + "*" + suffix;
        
        // Get candidates using permutation index
        Set<String> candidates = permutationIndex.searchByPattern(simplifiedPattern);
        
        return applyAdditionalFiltering(pattern, candidates);
    }

    /**
     * Processes wildcard queries using the trigram index.
     * Generates all relevant trigrams from the pattern and finds intersection of candidate sets.
     * 
     * @param pattern The wildcard pattern to search for (e.g., "pre*mid*suf")
     * @return Set of terms matching the pattern
     */
    public Set<String> queryTrigramIndex(String pattern) {
        if (!pattern.contains("*")) {
            throw new IllegalArgumentException("Pattern must contain at least one wildcard (*)");
        }

        // Generate all relevant trigrams from the pattern
        List<String> queryTrigrams = generateTrigramsFromPattern(pattern);
        
        if (queryTrigrams.isEmpty()) {
            return new HashSet<>();
        }

        // Get candidates by intersecting all trigram sets
        List<Set<String>> trigramSets = queryTrigrams.stream()
                .map(trigramIndex::contains)
                .toList();
        
        Set<String> candidates = SetUtil.intersection(trigramSets);

        return applyAdditionalFiltering(pattern, candidates);
    }

    /**
     * Processes wildcard queries using intersection of prefix and suffix indexes.
     * Finds terms that start with prefix and end with suffix, then applies additional filtering.
     * 
     * @param pattern The wildcard pattern to search for (e.g., "pre*mid*suf")
     * @return Set of terms matching the pattern
     */
    public Set<String> queryPrefixSuffixIndex(String pattern) {
        if (!pattern.contains("*")) {
            throw new IllegalArgumentException("Pattern must contain at least one wildcard (*)");
        }

        // Split pattern to get prefix and suffix
        String[] parts = pattern.split("\\*");
        String prefix = parts[0];
        String suffix = parts.length > 1 ? parts[parts.length - 1] : "";
        
        Set<String> candidates;
        
        if (!prefix.isEmpty() && !suffix.isEmpty()) {
            // Get intersection of prefix and suffix matches
            List<String> prefixMatches = prefixIndex.termsStartWith(prefix);
            List<String> suffixMatches = suffixIndex.endsWith(suffix);
            
            candidates = SetUtil.intersection(new HashSet<>(prefixMatches), new HashSet<>(suffixMatches));
        } else if (!prefix.isEmpty()) {
            // Only prefix search
            candidates = new HashSet<>(prefixIndex.termsStartWith(prefix));
        } else if (!suffix.isEmpty()) {
            // Only suffix search
            candidates = new HashSet<>(suffixIndex.endsWith(suffix));
        } else {
            // Pattern is just "*" - return empty set
            return new HashSet<>();
        }

        return applyAdditionalFiltering(pattern, candidates);
    }

    private Set<String> applyAdditionalFiltering(String pattern, Set<String> candidates) {
        // If original pattern has multiple wildcards, apply additional filtering
        if (pattern.split("\\*", -1).length > 2) {
            String regexPattern = convertWildcardToRegex(pattern);
            Pattern compiledPattern = Pattern.compile(regexPattern);
            
            return candidates.stream()
                    .filter(term -> compiledPattern.matcher(term).matches())
                    .collect(Collectors.toSet());
        }
        
        return candidates;
    }

    private List<String> generateTrigramsFromPattern(String pattern) {
        List<String> trigrams = new ArrayList<>();
        
        // Split by * to get parts
        String[] parts = pattern.split("\\*");
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            
            if (i == 0) {
                // First part - generate start trigrams
                trigrams.addAll(generateStartTrigrams(part));
            } else if (i == parts.length - 1) {
                // Last part - generate end trigrams
                trigrams.addAll(generateEndTrigrams(part));
            } else {
                // Middle part - generate internal trigrams
                trigrams.addAll(generateInternalTrigrams(part));
            }
        }
        
        return trigrams;
    }

    private Set<String> generateStartTrigrams(String part) {
        Set<String> trigrams = new HashSet<>();
        
        if (part.length() >= 2) {
            trigrams.add(" " + part.substring(0, 2));
        }
        if (!part.isEmpty()) {
            trigrams.add("  " + part.charAt(0));
        }
        
        // Add internal trigrams if part is long enough
        trigrams.addAll(generateInternalTrigrams(part));
        
        return trigrams;
    }

    private Set<String> generateEndTrigrams(String part) {
        Set<String> trigrams = new HashSet<>();
        
        if (part.length() >= 2) {
            trigrams.add(part.substring(part.length() - 2) + " ");
        }
        if (!part.isEmpty()) {
            trigrams.add(part.charAt(part.length() - 1) + "  ");
        }
        
        // Add internal trigrams if part is long enough
        trigrams.addAll(generateInternalTrigrams(part));
        
        return trigrams;
    }

    private Set<String> generateInternalTrigrams(String part) {
        Set<String> trigrams = new HashSet<>();
        
        if (part.length() >= 3) {
            for (int i = 0; i <= part.length() - 3; i++) {
                trigrams.add(part.substring(i, i + 3));
            }
        }
        
        return trigrams;
    }

    private String convertWildcardToRegex(String wildcardPattern) {
        // Escape special regex characters except *
        String escaped = wildcardPattern.replaceAll("([.\\[\\]{}()+?^$|\\\\])", "\\\\$1");
        // Convert * to .*
        return escaped.replace("*", ".*");
    }
}
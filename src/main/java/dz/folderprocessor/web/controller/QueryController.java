package dz.folderprocessor.web.controller;

import dz.folderprocessor.query.BooleanQueryProcessor;
import dz.folderprocessor.query.PhrasalQueryProcessor;
import dz.folderprocessor.query.WildcardQueryProcessor;
import dz.folderprocessor.web.dto.QueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RequestMapping("/query")
@RestController
@RequiredArgsConstructor
public class QueryController {

    private final BooleanQueryProcessor booleanQueryProcessor;
    private final PhrasalQueryProcessor phrasalQueryProcessor;
    private final WildcardQueryProcessor wildcardQueryProcessor;

    @PostMapping("/bool")
    public List<String> booleanQuery(@RequestBody QueryRequest request) {
        return booleanQueryProcessor.processQuery(request.getQuery());
    }

    @PostMapping("/phrase/coordinate")
    public List<String> phrasalQueryCoordinate(@RequestBody QueryRequest request) {
        return phrasalQueryProcessor.containsPhraseCoordinateIndex(request.getQuery());
    }

    @PostMapping("/phrase/pair")
    public List<String> phrasalQueryPair(@RequestBody QueryRequest request) {
        return phrasalQueryProcessor.containsPhrasePairIndex(request.getQuery());
    }

    @PostMapping("/wildcard/permutation")
    public Set<String> wildcardPermutationQuery(@RequestBody QueryRequest request) {
        return wildcardQueryProcessor.queryPermutationIndex(request.getQuery());
    }

    @PostMapping("/wildcard/trigram")
    public Set<String> wildcardTrigramQuery(@RequestBody QueryRequest request) {
        return wildcardQueryProcessor.queryTrigramIndex(request.getQuery());
    }

    @PostMapping("/wildcard/prefix-suffix")
    public Set<String> wildcardPrefixSuffixQuery(@RequestBody QueryRequest request) {
        return wildcardQueryProcessor.queryPrefixSuffixIndex(request.getQuery());
    }

}

package dz.folderprocessor.web.controller;

import dz.folderprocessor.query.BooleanQueryProcessor;
import dz.folderprocessor.query.PhrasalQueryProcessor;
import dz.folderprocessor.web.dto.QueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/query")
@RestController
@RequiredArgsConstructor
public class QueryController {

    private final BooleanQueryProcessor booleanQueryProcessor;
    private final PhrasalQueryProcessor phrasalQueryProcessor;

    @PostMapping("/bool")
    public List<String> booleanQuery(@RequestBody QueryRequest request) {
        return booleanQueryProcessor.processQuery(request.getQuery());
    }

    @PostMapping("/phrase")
    public List<String> phrasalQuery(@RequestBody QueryRequest request) {
        return phrasalQueryProcessor.processPhrasalQuery(request.getQuery());
    }

}

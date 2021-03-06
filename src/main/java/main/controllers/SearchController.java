package main.controllers;

import main.dto.SearchResultDto;
import main.exceptions.BadIndexException;
import main.exceptions.BadLemmaException;
import main.exceptions.BadPageException;
import main.exceptions.EmptyFrequencyException;
import main.services.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public synchronized ResponseEntity<SearchResultDto> getSearchResult(String query, int offset, int limit, String site) throws BadIndexException,
            BadLemmaException, EmptyFrequencyException, BadPageException {
        return searchService.startSearch(site, query);
    }
}

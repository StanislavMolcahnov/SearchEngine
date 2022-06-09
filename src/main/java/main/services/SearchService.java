
package main.services;

import main.dto.SearchPageDto;
import main.dto.SearchResultDto;
import main.exceptions.BadIndexException;
import main.exceptions.BadLemmaException;
import main.exceptions.BadPageException;
import main.exceptions.EmptyFrequencyException;
import main.model.Index;
import main.model.Lemma;
import main.model.Site;
import main.repositories.IndexRepository;
import main.repositories.LemmaRepository;
import main.repositories.PageRepository;
import main.repositories.SiteRepository;
import main.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SearchService {
    private SearchResultDto searchResultDto;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private int siteId;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;
    private final LemmatizerService lemmatizerService = new LemmatizerService();
    private Map<String, Integer> searchQueryLemmas = new TreeMap<>();
    private final Map<String, Integer> lemmasFrequency = new TreeMap<>();
    private List<Page> newPages = new ArrayList<>();
    private List<Page> oldPages = new ArrayList<>();
    private List<Page> siftedPages = new ArrayList<>();
    private final Map<Page, Float> linksWithRank = new TreeMap<>();
    private final Map<Page, Float> sortedLinks = new LinkedHashMap<>();
    private String searchQuery;

    @Autowired
    public SearchService(PageRepository pageRepository, IndexRepository indexRepository, LemmaRepository lemmaRepository, SiteRepository siteRepository) {
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        this.siteRepository = siteRepository;
    }

    public ResponseEntity<SearchResultDto> startSearch(String siteUrl, String searchQuery) throws BadIndexException, BadLemmaException, EmptyFrequencyException, BadPageException {
        searchResultDto = new SearchResultDto();
        this.searchQuery = searchQuery;
        lemmasFrequency.clear();
        siteId = 0;
        if (searchQuery.isBlank()) {
            searchResultDto.result = false;
            searchResultDto.error = "Задан пустой поисковый запрос";
            return ResponseEntity.ok(searchResultDto);
        }
        if (siteUrl != null) {
            siteId = siteRepository.findByurl(siteUrl).getId();
        }
        int countOfFoundLemmas;
        lemmatizerService.countOfLemmas(searchQuery);
        searchQueryLemmas = lemmatizerService.getCountOfLemmas();

        getLemmaFrequencyFromDB();

        searchQueryLemmas.clear();
        searchQueryLemmas.putAll(lemmasFrequency);

        countOfFoundLemmas = lemmasFrequency.size();
        pageSifting(countOfFoundLemmas);
        rankSort();
        return informationOutput();
    }

    private void getLemmaFrequencyFromDB() throws EmptyFrequencyException {
        double normalFrequency;
        try {
            normalFrequency = lemmaRepository.getMaxFrequency() * 0.85;
        } catch (Exception ex) {
            throw new EmptyFrequencyException("Отсутвствует максимальное значение frequency в БД");
        }
        for (String lemma : searchQueryLemmas.keySet()) {
            int lemmaFrequency = 0;
            try {
                if (siteId != 0) {
                    Optional<Lemma> optionalLemma = lemmaRepository.findByLemmaAndSiteId(lemma, siteId);
                    lemmaFrequency = optionalLemma.get().getFrequency();
                } else {
                    Iterable<Lemma> listLemmas = lemmaRepository.findByLemma(lemma);
                    for (Lemma singleLemma : listLemmas) {
                        lemmaFrequency += singleLemma.getFrequency();
                    }
                }
                if (lemmaFrequency != 0 && lemmaFrequency < normalFrequency) {
                    lemmasFrequency.put(lemma, lemmaFrequency);
                }
            } catch (Exception ex) {
            }
        }
    }

    private void pageSifting(int countOfFoundLemmas) throws BadIndexException, BadLemmaException, BadPageException {
        String minFrequencyLemma = "";
        oldPages.clear();

        for (int i = 0; i < countOfFoundLemmas; i++) {
            List<Integer> lemmasId = new ArrayList<>();
            minFrequencyLemma = minFrequencyLemma(minFrequencyLemma);
            getLemmasIdFromDB(minFrequencyLemma, lemmasId);

            Set<Integer> pagesId = new TreeSet<>();
            for (Integer idLemma : lemmasId) {
                Iterable<Index> indexes = indexRepository.findByLemmaId(idLemma);
                if (!indexes.iterator().hasNext()) {
                    throw new BadIndexException("Объект типа index не найден в БД");
                }
                indexes.forEach(index -> pagesId.add(index.getPageId()));
            }
            lemmasFrequency.remove(minFrequencyLemma);

            findPages(pagesId);
            if (i == 0) {
                oldPages.addAll(newPages);
            }

            for (Page page : newPages) {
                if (oldPages.contains(page)) {
                    siftedPages.add(page);
                }
            }
            oldPages = siftedPages;
            newPages = new ArrayList<>();
            siftedPages = new ArrayList<>();
        }
    }

    private void findPages(Set<Integer> pagesId) throws BadPageException {
        for (int pageId : pagesId) {
            if (siteId != 0) {
                Optional<Page> page = pageRepository.findByIdAndSiteId(pageId, siteId);
                page.ifPresent(presentPage -> newPages.add(presentPage));
            } else {
                Optional<Page> page = pageRepository.findById(pageId);
                if (page.isEmpty()) {
                    throw new BadPageException("Объект типа page не найден в БД");
                }
                page.ifPresent(presentPage -> newPages.add(presentPage));
            }
        }
    }

    private String minFrequencyLemma(String minFrequencyLemma) {
        int minFrequency = 2147483647;
        for (String lemma : lemmasFrequency.keySet()) {
            if (lemmasFrequency.get(lemma) < minFrequency) {
                minFrequency = lemmasFrequency.get(lemma);
                minFrequencyLemma = lemma;
            }
        }
        return minFrequencyLemma;
    }

    private void rankSort() throws BadLemmaException {
        sortedLinks.clear();
        float pageRank;
        Page maxRankPage = new Page();
        if (!oldPages.isEmpty()) {
            for (Page page : oldPages) {
                List<Integer> lemmasId = new ArrayList<>();
                pageRank = 0;
                for (String lemma : searchQueryLemmas.keySet()) {
                    getLemmasIdFromDB(lemma, lemmasId);
                    float lemmaRank = 0;
                    for (Integer idLemma : lemmasId) {
                        Optional<Index> index = indexRepository.findByLemmaIdAndPageId(idLemma, page.getId());
                        if (index.isPresent()) {
                            lemmaRank = index.get().getRank();
                        }
                    }
                    pageRank += lemmaRank;
                }
                linksWithRank.put(page, pageRank);
            }
            putInSortedLinks(maxRankPage);
        }
    }

    private void putInSortedLinks(Page maxRankPage) {
        float absoluteRel = 0;
        int countOfLinks = linksWithRank.size();
        for (int i = 0; i < countOfLinks; i++) {
            float maxRank = 0;
            for (Page page : linksWithRank.keySet()) {
                if (linksWithRank.get(page) > maxRank) {
                    maxRankPage = page;
                    maxRank = linksWithRank.get(page);
                }
            }
            if (i == 0) {
                absoluteRel = maxRank;
            }
            sortedLinks.put(maxRankPage, maxRank / absoluteRel);
            linksWithRank.remove(maxRankPage);
        }
    }

    private void getLemmasIdFromDB(String lemma, List<Integer> lemmasId) throws BadLemmaException {
        if (siteId != 0) {
            Optional<Lemma> optionalLemma = lemmaRepository.findByLemmaAndSiteId(lemma, siteId);
            if (optionalLemma.isPresent()) {
                int lemmaId = optionalLemma.get().getId();
                lemmasId.add(lemmaId);
            } else {
                throw new BadLemmaException("Объект типа lemma не найден в БД");
            }
        } else {
            Iterable<Lemma> listLemmas = lemmaRepository.findByLemma(lemma);
            if (!listLemmas.iterator().hasNext()) {
                throw new BadLemmaException("Объект типа lemma не найден в БД");
            }
            for (Lemma singleLemma : listLemmas) {
                lemmasId.add(singleLemma.getId());
            }
        }
    }

    private ResponseEntity<SearchResultDto> informationOutput() {
        if (sortedLinks.isEmpty()) {
            searchResultDto.result = false;
            searchResultDto.data = new ArrayList<>();
            searchResultDto.error = "Поиск не дал результатов";
        } else {
            fillSearchResultData();
            searchResultDto.result = true;
            searchResultDto.count = searchResultDto.data.size();
        }
        return ResponseEntity.ok(searchResultDto);
    }

    private void fillSearchResultData() {
        searchResultDto.data = new ArrayList<>();
        for (Page page : sortedLinks.keySet()) {
            SearchPageDto searchPageDto = new SearchPageDto();
            String content = page.getContent();
            int startTitle = content.indexOf("<title>") + 7;
            int endTitle = content.indexOf("</title>");
            String title = content.substring(startTitle, endTitle);

            int startBody = content.indexOf("<body>") + 6;
            int endBody = content.indexOf("</body>");
            String body = content.substring(startBody, endBody);

            StringBuilder snippet = snippetSearch(body).append("...");
            Optional<Site> site = Optional.empty();
            Optional<Page> optionalPage = pageRepository.findById(page.getId());
            if (optionalPage.isPresent()) {
                site = siteRepository.findById(optionalPage.get().getSiteId());
            }
            if (site.isPresent()) {
                searchPageDto.site = site.get().getUrl();
                searchPageDto.siteName = site.get().getName();
            }
            searchPageDto.uri = page.getPath();
            searchPageDto.title = title;
            searchPageDto.snippet = snippet.toString();
            searchPageDto.relevance = sortedLinks.get(page);
            searchResultDto.data.add(searchPageDto);
        }
    }

    private StringBuilder snippetSearch(String body) {
        StringBuilder snippet = new StringBuilder();
        int countSnippets = 0;

        List<String> query = getOriginalSearchWord(body);
        snippet.append("...");
        if (body.toLowerCase().contains(searchQuery.toLowerCase()) && searchQueryLemmas.size() > 1) {
            snippet.append(findSnippet(body, searchQuery));
            return snippet;
        }
        for (String searchWord : query) {
            if (countSnippets == 3) {
                break;
            }
            if (body.contains(searchWord)) {
                snippet.append(findSnippet(body, searchWord));
                countSnippets++;
            }
        }
        return snippet;
    }

    private String findSnippet(String body, String searchWord) {
        StringBuilder snippetPart = new StringBuilder();

        int startQuery = findWord(searchWord, body)[0];
        int endQuery = findWord(searchWord, body)[1];

        int startSnippet = body.lastIndexOf("\s", startQuery - 40) + 1;
        int endSnippet = body.indexOf("\s", endQuery + 40);

        if (endQuery > endSnippet) {
            endSnippet = endQuery;
        }
        if (startQuery < startSnippet) {
            startSnippet = startQuery;
        }
        snippetPart.append(body, startSnippet, startQuery)
                .append("<b>")
                .append(body, startQuery, endQuery)
                .append("</b>")
                .append(body, endQuery, endSnippet);

        return snippetPart.toString();
    }

    private List<String> getOriginalSearchWord(String body) {
        List<String> searchWordInBody = new ArrayList<>();

        lemmatizerService.countOfLemmas(body);
        Map<String, String> bodyLemmasPlusWords = lemmatizerService.getLemmaPlusWord();

        for (String lemmaSearchWord : searchQueryLemmas.keySet()) {
            if (bodyLemmasPlusWords.containsKey(lemmaSearchWord)) {
                searchWordInBody.add(bodyLemmasPlusWords.get(lemmaSearchWord));
            }
        }
        return searchWordInBody;
    }

    private int[] findWord(String searchWord, String body) {
        int[] startEnd = new int[]{0, 0};
        searchWord = searchWord.toLowerCase();
        body = body.toLowerCase();
        String wordInCenter = "[^ёЁА-Яа-яA-Za-z]" + searchWord + "[^ёЁА-Яа-яA-Za-z]";
        Pattern patternCenter = Pattern.compile(wordInCenter);
        Matcher matcherCenter = patternCenter.matcher(body);

        String wordAtBeginning = searchWord + "[^ёЁА-Яа-яA-Za-z]";
        Pattern patternBeginning = Pattern.compile(wordAtBeginning);
        Matcher matcherBeginning = patternBeginning.matcher(body);

        String wordAtEnd = "[^ёЁА-Яа-яA-Za-z]" + searchWord;
        Pattern patternEnd = Pattern.compile(wordAtEnd);
        Matcher matcherEnd = patternEnd.matcher(body);

        if (matcherCenter.find()) {
            startEnd[0] = matcherCenter.start();
            startEnd[1] = matcherCenter.end();
        } else if (matcherBeginning.find()) {
            startEnd[0] = matcherBeginning.start();
            startEnd[1] = matcherBeginning.end();
        } else if (matcherEnd.find()) {
            startEnd[0] = matcherEnd.start();
            startEnd[1] = matcherEnd.end();
        }
        return startEnd;
    }
}

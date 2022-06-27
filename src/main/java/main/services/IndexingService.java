package main.services;

import lombok.RequiredArgsConstructor;
import main.exceptions.BadFieldException;
import main.repositories.FieldRepository;
import main.repositories.IndexRepository;
import main.repositories.LemmaRepository;
import main.repositories.PageRepository;
import main.model.Index;
import main.model.Lemma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor(onConstructor =@__({@Autowired}))
public class IndexingService {
    private int siteId;
    private final PageRepository pageRepository;
    private final FieldRepository fieldRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public void startIndexingWebPage(String path, String title, String body, int code) {
        if (code == 200) {
            LemmatizerService lemmatizerService = new LemmatizerService();

            int pageId = pageRepository.findByPathAndSiteId(path, siteId).getId();
            String titleBody = title + " " + body;
            lemmatizerService.countOfLemmas(titleBody);
            TreeMap<String, Integer> allLemmas = lemmatizerService.getCountOfLemmas();
            for (String lemma : allLemmas.keySet()) {
                lemmaRepository.save(new Lemma(lemma, 1, siteId));
            }

            lemmatizerService.countOfLemmas(title);
            TreeMap<String, Integer> titleLemmas = lemmatizerService.getCountOfLemmas();

            lemmatizerService.countOfLemmas(body);
            TreeMap<String, Integer> bodyLemmas = lemmatizerService.getCountOfLemmas();

            saveToIndex(allLemmas, titleLemmas, bodyLemmas, pageId);
        }
    }

    private void saveToIndex(TreeMap<String, Integer> allLemmas, TreeMap<String, Integer> titleLemmas, TreeMap<String, Integer> bodyLemmas, int pageId) {
        for (String lemma : allLemmas.keySet()) {
            if (fieldRepository.findByname("title") == null || fieldRepository.findByname("body") == null) {
                try {
                    throw new BadFieldException("Объект типа field не найден в БД");
                } catch (BadFieldException e) {
                    throw new RuntimeException(e);
                }
            }
            float rank = 0;
            if (titleLemmas.containsKey(lemma)) {
                rank = titleLemmas.get(lemma) * fieldRepository.findByname("title").getWeight();
            }
            if (bodyLemmas.containsKey(lemma)) {
                rank = rank + bodyLemmas.get(lemma) * fieldRepository.findByname("body").getWeight();
            }
            Optional<Lemma> optionalLemma = lemmaRepository.findByLemmaAndSiteId(lemma, siteId);
            int lemmaId = 0;
            if (optionalLemma.isPresent()) {
                lemmaId = optionalLemma.get().getId();
            }
            indexRepository.save(new Index(pageId, lemmaId, rank));
        }
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }
}

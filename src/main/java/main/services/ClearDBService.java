package main.services;

import lombok.RequiredArgsConstructor;
import main.repositories.IndexRepository;
import main.repositories.LemmaRepository;
import main.repositories.PageRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClearDBService {
    private int numberOfCleansing;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public synchronized void clearDB() {
        if (numberOfCleansing == 0) {
            pageRepository.deleteAll();
            lemmaRepository.deleteAll();
            indexRepository.deleteAll();
        }
    }

    public void setNumberOfCleansing(int numberOfCleansing) {
        this.numberOfCleansing = numberOfCleansing;
    }
}

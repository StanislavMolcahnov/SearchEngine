package main.services;

import main.config.Config;
import main.dto.LinkNodeDto;
import main.model.Field;
import main.model.Site;
import main.model.StatusType;
import main.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ManagementService {
    private final SavePageService savePageService;
    private int numberOfSite = 0;
    private final ClearDBService clearDBService;
    private final List<ThreadDistributor> threads = new ArrayList<>();
    private final IsIndexingService isIndexingService;
    private final FieldRepository fieldRepository;
    private final Config config;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final UpdateUrlService updateUrlService;

    @Autowired
    public ManagementService(SavePageService savePageService, ClearDBService clearDBService, IsIndexingService isIndexingService, FieldRepository fieldRepository,
                             Config config, PageRepository pageRepository, SiteRepository siteRepository,
                             LemmaRepository lemmaRepository, IndexRepository indexRepository, UpdateUrlService updateUrlService) {
        this.savePageService = savePageService;
        this.clearDBService = clearDBService;
        this.isIndexingService = isIndexingService;
        this.fieldRepository = fieldRepository;
        this.config = config;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.updateUrlService = updateUrlService;
    }

    public StringBuilder startIndexing() {
        StringBuilder indexing = new StringBuilder();
        if (isIndexingService.indexingCheck()) {
            return indexing.append("{ \"result\" : ").append(false).append(",").append(" \"error\" : \"Индексация уже запущена\"").append(" }");
        } else {
            fieldToDB();
            siteRepository.deleteAll();
            for (Config.SiteData site : config.getSites()) {
                fillSiteAndSave(site);

                int siteId = siteRepository.findByurl(site.getUrl()).getId();
                LinkNodeDto linkNode = new LinkNodeDto(site.getUrl());
                ThreadDistributor threadDistributor = new ThreadDistributor(savePageService, config, pageRepository, fieldRepository,
                        lemmaRepository, indexRepository, siteRepository);
                threadDistributor.setLinkNode(linkNode);
                threadDistributor.setSiteId(siteId);
                threadDistributor.start();
                clearDBService.setNumberOfCleansing(numberOfSite);
                threadDistributor.setClearDBService(clearDBService);
                numberOfSite++;
                threads.add(threadDistributor);
            }
            numberOfSite = 0;
            return indexing.append("{ \"result\" : ").append(true).append(" }");
        }
    }

    private void fillSiteAndSave(Config.SiteData site) {
        Site siteToDB = new Site();
        siteToDB.setName(site.getName());
        siteToDB.setUrl(site.getUrl());
        siteToDB.setStatus(StatusType.INDEXING);
        siteToDB.setLastError("-");
        siteToDB.setStatusTime(System.currentTimeMillis());
        siteRepository.save(siteToDB);
    }

    public StringBuilder stopIndexing() {
        StringBuilder indexing = new StringBuilder();
        if (!isIndexingService.indexingCheck()) {
            return indexing.append("{ \"result\" : ").append(false).append(",")
                    .append(" \"error\" : \"Индексация не запущена\"").append(" }");
        } else {
            threads.forEach(ThreadDistributor::stopThread);
            Iterable<Site> sites = siteRepository.findAll();
            sites.forEach(site -> {
                if (site.getStatus().equals(StatusType.INDEXING)) {
                    site.setStatus(StatusType.FAILED);
                    site.setLastError("Индексация была остановлена");
                }
                site.setStatusTime(System.currentTimeMillis());
                siteRepository.save(site);
            });
            return indexing.append("{ \"result\" : ").append(true).append(" }");
        }
    }

    public StringBuilder updateUrl(String url) {
        StringBuilder indexPage = new StringBuilder();
        if (updateUrlService.update(url)) {
            return indexPage.append("{ \"result\" : ").append(true).append(" }");
        } else {
            return indexPage.append("{ \"result\" : ").append(false).append(",")
                    .append(" \"error\" : \"Данная страница находится за пределами сайтов, " +
                            "указанных в конфигурационном файле\"").append(" }");
        }

    }

    private void fieldToDB() {
        if (fieldRepository.findByname("title") == null) {
            fieldRepository.save(new Field("title", "title", 1.0f));
        }
        if (fieldRepository.findByname("body") == null) {
            fieldRepository.save(new Field("body", "body", 0.8f));
        }
    }
}


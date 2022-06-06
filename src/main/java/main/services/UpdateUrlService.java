package main.services;

import main.config.Config;
import main.model.Page;
import main.model.Site;
import main.repositories.IndexRepository;
import main.repositories.PageRepository;
import main.repositories.SiteRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUrlService {
    private String path;
    private int siteId = 0;
    private String rootUrl = "";
    private boolean pageWithin = false;
    private final SavePageService savePageService;
    private final SiteRepository siteRepository;
    private final Config config;
    private final IndexingService indexingService;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private Document site;

    public UpdateUrlService(SavePageService savePageService, SiteRepository siteRepository, Config config, IndexingService indexingService,
                            PageRepository pageRepository, IndexRepository indexRepository) {
        this.savePageService = savePageService;
        this.siteRepository = siteRepository;
        this.config = config;
        this.indexingService = indexingService;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
    }

    @Transactional
    public boolean update(String url) {
        checkUrl(url);
        if (pageWithin) {
            removePage(url);
            for (int cycleCount = 1; cycleCount < 3; cycleCount++) {
                try {
                    site = Jsoup.connect(url)
                            .userAgent(config.getUserAgent())
                            .referrer(config.getReferrer())
                            .ignoreHttpErrors(true)
                            .ignoreContentType(true)
                            .get();
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            try {
                savePageService.siteToDBPage(site, path, siteId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            indexingService.setSiteId(siteId);
            indexingService.startIndexingWebPage(path, savePageService.getTitle(),
                    savePageService.getBody(), savePageService.getStatusCode());
        } else {
            return false;
        }
        return true;
    }

    private void checkUrl(String url) {
        Iterable<Site> sites = siteRepository.findAll();
        for (Site site : sites) {
            if (url.startsWith(site.getUrl())) {
                pageWithin = true;
                siteId = site.getId();
                rootUrl = site.getUrl();
                break;
            }
        }
    }

    private void removePage(String url) {
        path = url.substring(rootUrl.length()) + "/";
        Page removePage = pageRepository.findByPathAndSiteId(path, siteId);
        if (removePage != null) {
            int pageId = removePage.getId();
            pageRepository.deleteById(pageId);
            indexRepository.deleteByPageId(pageId);
        }
    }
}

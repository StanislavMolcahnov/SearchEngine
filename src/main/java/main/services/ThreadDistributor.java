package main.services;

import lombok.RequiredArgsConstructor;
import main.config.Config;
import main.dto.LinkNodeDto;
import main.model.Site;
import main.model.StatusType;
import main.repositories.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class ThreadDistributor extends Thread {

    private final SavePageService savePageService;
    private ClearDBService clearDBService;
    private LinkNodeDto linkNode;
    private int siteId;
    private final Config config;
    private final PageRepository pageRepository;
    private final FieldRepository fieldRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final ForkJoinPool pool = new ForkJoinPool();

    @Override
    public void run() {
        clearDBService.clearDB();
        SiteBypassService siteBypassService = new SiteBypassService(savePageService, fieldRepository, lemmaRepository, indexRepository,
                config, pageRepository, siteRepository);
        siteBypassService.setNode(linkNode);
        siteBypassService.setSiteId(siteId);
        pool.invoke(siteBypassService);
        Optional<Site> site = siteRepository.findById(siteId);
        site.ifPresent(updatableSite -> {
            if (!updatableSite.getStatus().equals(StatusType.FAILED)) {
                updatableSite.setStatus(StatusType.INDEXED);
                updatableSite.setStatusTime(System.currentTimeMillis());
                updatableSite.setLastError("-");
            }
            siteRepository.save(updatableSite);
        });
    }

    public void setLinkNode(LinkNodeDto linkNode) {
        this.linkNode = linkNode;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public void stopThread() {
        pool.shutdownNow();
    }

    public void setClearDBService(ClearDBService clearDBService) {
        this.clearDBService = clearDBService;
    }
}

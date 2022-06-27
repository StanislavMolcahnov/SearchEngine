package main.services;

import lombok.RequiredArgsConstructor;
import main.config.Config;
import main.dto.LinkNodeDto;
import main.model.Site;
import main.model.StatusType;
import main.repositories.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.*;

@Service
@RequiredArgsConstructor
public class SiteBypassService extends RecursiveAction {
    private String path;
    private final SavePageService savePageService;
    private final FieldRepository fieldRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private int siteId;
    private final Config config;
    private LinkNodeDto node;
    private Document site;
    private final PageRepository pageRepository;
    private final List<SiteBypassService> taskList = new ArrayList<>();
    private final SiteRepository siteRepository;

    @Override
    protected void compute() {
        if (!currentThread().isInterrupted()) {
            siteChange();
            try {
                sleep(100);
                path = node.getUrl().substring(node.getRootElement().getUrl().length()) + "/";
                connectToSite();
                savePageService.siteToDBPage(site, path, siteId);
                startIndexing();
                saveChildNodes();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            for (LinkNodeDto child : node.getChildren()) {
                SiteBypassService task = new SiteBypassService(savePageService, fieldRepository, lemmaRepository, indexRepository, config, pageRepository, siteRepository);
                task.setSiteId(siteId);
                task.setNode(child);
                task.fork();
                taskList.add(task);
            }
            for (SiteBypassService task : taskList) {
                task.join();
            }
        }
    }

    private void siteChange() {
        Optional<Site> optionalSite = siteRepository.findById(siteId);
        optionalSite.ifPresent(updatableSite -> {
            updatableSite.setStatus(StatusType.INDEXING);
            updatableSite.setStatusTime(System.currentTimeMillis());
            siteRepository.save(updatableSite);
        });
    }

    private void connectToSite() {
        for (int cycleCount = 1; cycleCount < 3; cycleCount++) {
            try {
                site = Jsoup.connect(node.getUrl())
                        .userAgent(config.getUserAgent())
                        .referrer(config.getReferrer())
                        .ignoreHttpErrors(true)
                        .ignoreContentType(true)
                        .get();
                break;
            } catch (Exception ex) {
                if (cycleCount == 2 && node.getUrl().equals(node.getRootElement().getUrl())) {
                    Optional<Site> failedSite = siteRepository.findById(siteId);
                    failedSite.ifPresent(errorSite -> {
                        errorSite.setStatus(StatusType.FAILED);
                        errorSite.setStatusTime(System.currentTimeMillis());
                        errorSite.setLastError("Главная страница сайта недоступна");
                        siteRepository.save(errorSite);
                    });
                }
                ex.printStackTrace();
            }
        }
    }

    private void saveChildNodes() {
        Elements links = site.select("a");
        for (Element link : links) {
            String correctLink = link.absUrl("href");
            if (correctLink.endsWith("/")) {
                correctLink = correctLink.substring(0, correctLink.length() - 1);
            }
            if (checkLink(correctLink)) {
                continue;
            }
            node.addChild(new LinkNodeDto(correctLink));
        }
    }

    private void startIndexing() {
        IndexingService indexingService = new IndexingService(pageRepository, fieldRepository, lemmaRepository, indexRepository);
        indexingService.setSiteId(siteId);
        indexingService.startIndexingWebPage(path, savePageService.getTitle(),
                savePageService.getBody(), savePageService.getStatusCode());
    }

    private boolean checkLink(String link) {
        Pattern badLink = Pattern.compile("(png)|(jpg)|(gif)|(PNG)|(JPG)|(GIF)|(JPEG)|(jpeg)|(BMP)|(bmp)|(pdf)|(PDF)|[#?]");
        Matcher badLinkBoolean = badLink.matcher(link);
        return !link.startsWith(node.getUrl()) || badLinkBoolean.find();
    }

    public void setNode(LinkNodeDto node) {
        this.node = node;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }
}

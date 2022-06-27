package main.services;

import lombok.RequiredArgsConstructor;
import main.model.Site;
import main.model.StatusType;
import main.repositories.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class IsIndexingService {
    private final SiteRepository siteRepository;

    public boolean indexingCheck(){
        boolean isIndexing = false;
        Iterable<Site> siteIterable = siteRepository.findAll();
        ArrayList<Site> sites = new ArrayList<>();
        for (Site site : siteIterable) {
            sites.add(site);
        }
        for (Site site : sites) {
            if (site.getStatus() == StatusType.INDEXING) {
                isIndexing = true;
                break;
            }
        }
        return isIndexing;
    }
}

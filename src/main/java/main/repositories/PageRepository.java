package main.repositories;

import main.model.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    long countBysiteId(int id);

    Optional<Page> findBypath(String path);

    Page findByPathAndSiteId(String path, int siteId);

    Optional<Page> findByIdAndSiteId(int pageId, int siteId);
}

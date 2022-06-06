package main.repositories;

import main.model.Index;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {
    void deleteByPageId(int pageId);

    Iterable<Index> findByLemmaId(int idLemma);

    Optional<Index> findByLemmaIdAndPageId(int lemmaId, int pageId);
}

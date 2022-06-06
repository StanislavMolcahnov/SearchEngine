package main.repositories;

import main.model.Lemma;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Query("SELECT max(frequency) FROM Lemma")
    int getMaxFrequency();

    long countBysiteId(int id);

    Optional<Lemma> findByLemmaAndSiteId(String lemma, int siteId);

    Iterable<Lemma> findByLemma(String lemma);
}

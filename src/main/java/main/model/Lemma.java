package main.model;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLInsert;

@Entity
@Table(name = "lemma", uniqueConstraints = {@UniqueConstraint(columnNames = {"lemma", "site_id"}, name = "lemmaSiteId")})
@SQLInsert(sql = "INSERT INTO lemma(frequency, lemma, site_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE frequency = frequency + 1")
@Getter
@Setter
public class Lemma {

    public Lemma() {

    }

    public Lemma(String lemma, int frequency, int siteId) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteId = siteId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 191)
    private String lemma;

    private int frequency;

    @Column(name = "site_id")
    private int siteId;
}

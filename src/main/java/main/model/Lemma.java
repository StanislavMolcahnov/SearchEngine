package main.model;

import javax.persistence.*;
import org.hibernate.annotations.SQLInsert;

@Entity
@Table(name = "lemma", uniqueConstraints = {@UniqueConstraint(columnNames = {"lemma", "site_id"}, name = "lemmaSiteId")})
@SQLInsert(sql = "INSERT INTO lemma(frequency, lemma, site_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE frequency = frequency + 1")
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }
}

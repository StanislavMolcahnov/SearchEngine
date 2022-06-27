package main.model;

import javax.persistence.*;
import javax.persistence.Index;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "page", indexes = @Index(name = "path", columnList = "path"))
@Getter
@Setter
public class Page implements Comparable<Page> {

    public Page() {
    }

    public Page(String path, int code, String content, int siteId) {
        this.siteId = siteId;
        this.path = path;
        this.code = code;
        this.content = content;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 191)
    private String path;

    private int code;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String content;

    @Column(name = "site_id")
    private int siteId;

    @Override
    public int compareTo(@NotNull Page page) {
        return this.path.compareTo(page.path);
    }
}

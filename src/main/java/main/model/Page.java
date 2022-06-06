package main.model;

import javax.persistence.*;
import javax.persistence.Index;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "page", indexes = @Index(name = "path", columnList = "path"))
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

    @Column(nullable = false, length = 768)
    private String path;

    private int code;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String content;

    @Column(name = "site_id")
    private int siteId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    @Override
    public int compareTo(@NotNull Page page) {
        return this.path.compareTo(page.path);
    }
}

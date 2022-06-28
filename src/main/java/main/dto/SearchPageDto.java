package main.dto;

public class SearchPageDto {

    public String site;
    public String siteName;
    public String uri;
    public String title;
    public String snippet;
    public float relevance;

    @Override
    public String toString() {

        return "SearchPageDto{" +
                "site='" + site + '\'' +
                ", siteName='" + siteName + '\'' +
                ", uri='" + uri + '\'' +
                ", title='" + title + '\'' +
                ", snippet='" + snippet + '\'' +
                ", relevance=" + relevance +
                '}';
    }
}

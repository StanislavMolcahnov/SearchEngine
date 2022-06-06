package main.services;

import main.model.Page;
import main.repositories.PageRepository;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SavePageService {
    private String title;
    private String body;
    private int statusCode;
    private final PageRepository pageRepository;

    public SavePageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public void siteToDBPage(Document site, String path, int siteId) throws IOException {
        Connection.Response response = site.connection().execute();
        statusCode = response.statusCode();

        title = site.title();
        body = site.select("body").text();
        StringBuilder titleBodyText = new StringBuilder();
        titleBodyText.append("<title>").append(title).append("</title>").append("<body>").append(body).append("</body>");

        Page page = new Page(path, statusCode, titleBodyText.toString(), siteId);
        pageRepository.save(page);
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

package ch.ethz.covspectrum.entity.api;

import java.time.LocalDate;
import java.util.List;


public class RxivArticle {

    private String doi;
    private String title;
    private List<String> authors;
    private LocalDate date;
    private String category;
    private String published;
    private String server;

    public String getDoi() {
        return doi;
    }

    public RxivArticle setDoi(String doi) {
        this.doi = doi;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public RxivArticle setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public RxivArticle setAuthors(List<String> authors) {
        this.authors = authors;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public RxivArticle setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public RxivArticle setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getPublished() {
        return published;
    }

    public RxivArticle setPublished(String published) {
        this.published = published;
        return this;
    }

    public String getServer() {
        return server;
    }

    public RxivArticle setServer(String server) {
        this.server = server;
        return this;
    }
}

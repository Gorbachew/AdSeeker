package com.immo.adseeker.support;



public class Analytic {

    private String searcher = "";
    private String date = "";
    private String task = "";
    private String site = "";
    private String url = "";
    private String dir = "";
    private int numErrors = 0;
    private int numRedir = 0;
    private int numLinks = 0;
    private int numBrokenLinks = 0;
    private int numKeyWords = 0;
    private int numBanners = 0;
    private String errors = "";
    private String redirects = "";
    private String keyWords = "";
    private String banners = "";

    public String getSearcher() {
        return searcher;
    }

    public void setSearcher(String searcher) {
        this.searcher = searcher;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public int getNumErrors() {
        return numErrors;
    }

    public void setNumErrors(int numErrors) {
        this.numErrors = numErrors;
    }

    public int getNumRedir() {
        return numRedir;
    }

    public void setNumRedir(int numRedir) {
        this.numRedir = numRedir;
    }

    public int getNumLinks() {
        return numLinks;
    }

    public void setNumLinks(int numLinks) {
        this.numLinks += numLinks;
    }

    public int getNumBrokenLinks() {
        return numBrokenLinks;
    }

    public void setNumBrokenLinks(int numBrokenLinks) {
        this.numBrokenLinks += numBrokenLinks;
    }

    public int getNumKeyWords() {
        return numKeyWords;
    }

    public void setNumKeyWords(int numKeyWords) {
        this.numKeyWords += numKeyWords;
    }

    public int getNumBanners() {
        return numBanners;
    }

    public void setNumBanners(int numBanners) {
        this.numBanners += numBanners;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors += errors;
    }

    public String getRedirects() {
        return redirects;
    }

    public void setRedirects(String redirects) {
        this.redirects += redirects;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords += keyWords;
    }

    public String getBanners() {
        return banners;
    }

    public void setBanners(String banners) {
        this.banners += banners;
    }
}

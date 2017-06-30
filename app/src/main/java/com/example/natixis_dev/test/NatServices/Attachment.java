package com.example.natixis_dev.test.NatServices;

/**
 * Created by natixis-dev on 29/06/2017.
 */

public class Attachment {

    public Card content;
    public String contentType;
    public String contentUrl;
    public String name;
    public String thumbnailUrl;

    public Card getContent() {
        return content;
    }

    public void setContent(Card content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}

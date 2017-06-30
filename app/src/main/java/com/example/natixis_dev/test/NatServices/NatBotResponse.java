package com.example.natixis_dev.test.NatServices;

import java.util.List;

/**
 * Created by natixis-dev on 28/06/2017.
 */

public class NatBotResponse {

    public String text;
    public List<Attachment> attachments;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}

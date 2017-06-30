package com.example.natixis_dev.test.NatServices;

import java.util.List;

/**
 * Created by natixis-dev on 29/06/2017.
 */

public class Card {

    public List<CardAction> buttons;
    public String title;
    public String subtitle;
    public String text;
    public CardAction tap;

    public List<CardAction> getButtons() {
        return buttons;
    }

    public void setButtons(List<CardAction> buttons) {
        this.buttons = buttons;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CardAction getTap() {
        return tap;
    }

    public void setTap(CardAction tap) {
        this.tap = tap;
    }
}

package com.lumi2021.urna.domain.models.bulletinDocument;

public class BulletinLine extends BulletinElement {

    private String content;
    private BulletinTextMode mode;

    public BulletinLine(String content, BulletinTextMode textMode) {
        this.content = content;
        mode = textMode;
    }

    public String content() {
        return content;
    }
    public BulletinTextMode textMode() {
        return mode;
    }
}

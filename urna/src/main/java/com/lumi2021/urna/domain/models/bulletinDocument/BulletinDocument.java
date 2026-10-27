package com.lumi2021.urna.domain.models.bulletinDocument;

import java.util.List;

public class BulletinDocument {
    private List<BulletinElement> elements;

    public BulletinDocument(List<BulletinElement> elements) {
        this.elements = elements;
    }

    public List<BulletinElement> elements() {
        return elements;
    }
}

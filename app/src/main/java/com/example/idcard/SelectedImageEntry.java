package com.example.idcard;

import android.net.Uri;

/**
 * 用户选择的一张待识别图片及其身份证面（正面/反面）。
 */
public class SelectedImageEntry {
    private final Uri uri;
    private String cardSide; // "FRONT" 或 "BACK"

    public SelectedImageEntry(Uri uri, String cardSide) {
        this.uri = uri;
        this.cardSide = cardSide != null ? cardSide : "FRONT";
    }

    public Uri getUri() {
        return uri;
    }

    public String getCardSide() {
        return cardSide;
    }

    public void setCardSide(String cardSide) {
        if (cardSide != null && ("FRONT".equals(cardSide) || "BACK".equals(cardSide))) {
            this.cardSide = cardSide;
        }
    }

    public boolean isFront() {
        return "FRONT".equals(cardSide);
    }
}

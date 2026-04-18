package com.example.idcard;

/**
 * 批量识别中每一张图片的识别结果。
 */
public class BatchOcrResult {
    private final int index;
    private final String cardSide;
    private final String rawJson;
    private final IdCardInfo idCardInfo;

    public BatchOcrResult(int index, String cardSide, String rawJson, IdCardInfo idCardInfo) {
        this.index = index;
        this.cardSide = cardSide;
        this.rawJson = rawJson;
        this.idCardInfo = idCardInfo;
    }

    public int getIndex() {
        return index;
    }

    public String getCardSide() {
        return cardSide;
    }

    public String getRawJson() {
        return rawJson;
    }

    public IdCardInfo getIdCardInfo() {
        return idCardInfo;
    }
}

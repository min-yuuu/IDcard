package com.example.idcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppData {
    private static AppData instance;
    private String ocrResult;
    private List<BatchOcrResult> batchOcrResults = new ArrayList<>();

    private AppData() {}

    public static synchronized AppData getInstance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }

    public String getOcrResult() {
        return ocrResult;
    }

    public void setOcrResult(String ocrResult) {
        this.ocrResult = ocrResult;
    }

    public void setBatchOcrResults(List<BatchOcrResult> results) {
        batchOcrResults = results != null ? new ArrayList<>(results) : new ArrayList<>();
    }

    public List<BatchOcrResult> getBatchOcrResults() {
        return Collections.unmodifiableList(batchOcrResults);
    }

    public boolean hasBatchResults() {
        return !batchOcrResults.isEmpty();
    }
}
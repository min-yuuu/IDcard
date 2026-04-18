package com.example.idcard;

import org.json.JSONObject;

public class IdCardInfo {
    private String name;
    private String sex;
    private String nation;
    private String birth;
    private String address;
    private String idNum;
    private String authority;  // 签发机关（背面）
    private String validDate;  // 有效期限（背面）
    private boolean isSuccess;
    private String errorMessage;

    public IdCardInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getValidDate() {
        return validDate;
    }

    public void setValidDate(String validDate) {
        this.validDate = validDate;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 解析腾讯云身份证 OCR（IDCardOCR）接口返回的 JSON。
     */
    public static IdCardInfo fromTencentOcrJson(String jsonResult) {
        IdCardInfo info = new IdCardInfo();
        if (jsonResult == null || jsonResult.isEmpty()) {
            info.setSuccess(false);
            info.setErrorMessage("空响应");
            return info;
        }
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (!json.has("Response")) {
                info.setSuccess(false);
                info.setErrorMessage("无 Response 字段");
                return info;
            }
            JSONObject response = json.getJSONObject("Response");
            if (response.has("Error")) {
                JSONObject error = response.getJSONObject("Error");
                info.setSuccess(false);
                info.setErrorMessage(error.optString("Message", "未知错误"));
                return info;
            }
            info.setName(response.optString("Name", ""));
            info.setSex(response.optString("Sex", ""));
            info.setNation(response.optString("Nation", ""));
            info.setBirth(response.optString("Birth", ""));
            info.setAddress(response.optString("Address", ""));
            info.setIdNum(response.optString("IdNum", ""));
            info.setAuthority(response.optString("Authority", ""));
            info.setValidDate(response.optString("ValidDate", ""));
            info.setSuccess(true);
        } catch (Exception e) {
            info.setSuccess(false);
            info.setErrorMessage(e.getMessage() != null ? e.getMessage() : "解析异常");
        }
        return info;
    }
}

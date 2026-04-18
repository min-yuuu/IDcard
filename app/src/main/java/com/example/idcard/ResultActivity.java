package com.example.idcard;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;

import org.json.JSONObject;

public class ResultActivity extends Activity {

    private TextView tvName;
    private TextView tvSex;
    private TextView tvNation;
    private TextView tvBirth;
    private TextView tvAddress;
    private TextView tvIdNumber;
    private TextView tvAuthority;
    private TextView tvValidDate;
    
    private LinearLayout layoutNation;
    private LinearLayout layoutAuthority;
    private LinearLayout layoutValidDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initViews();
        displayResult();
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_export).setOnClickListener(v -> exportResult());
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_name);
        tvSex = findViewById(R.id.tv_sex);
        tvNation = findViewById(R.id.tv_nation);
        tvBirth = findViewById(R.id.tv_birth);
        tvAddress = findViewById(R.id.tv_address);
        tvIdNumber = findViewById(R.id.tv_id_number);
        tvAuthority = findViewById(R.id.tv_authority);
        tvValidDate = findViewById(R.id.tv_valid_date);
        
        layoutNation = findViewById(R.id.layout_nation);
        layoutAuthority = findViewById(R.id.layout_authority);
        layoutValidDate = findViewById(R.id.layout_valid_date);
    }

    private void displayResult() {
        String result = AppData.getInstance().getOcrResult();
        if (result != null) {
            parseAndDisplayResult(result);
        } else {
            tvName.setText("暂无识别结果");
        }
    }

    private void parseAndDisplayResult(String jsonResult) {
        try {
            JSONObject json = new JSONObject(jsonResult);
            
            // 腾讯云 OCR API 返回格式：Response.Name, Response.Sex 等
            if (json.has("Response")) {
                JSONObject response = json.getJSONObject("Response");
                
                // 检查是否有错误
                if (response.has("Error")) {
                    JSONObject error = response.getJSONObject("Error");
                    String errorMsg = error.optString("Message", "未知错误");
                    tvName.setText("识别失败");
                    tvAddress.setText(errorMsg);
                    return;
                }
                
                // 解析正面信息
                if (response.has("Name")) {
                    String name = response.optString("Name", "");
                    if (!name.isEmpty()) {
                        tvName.setText(name);
                    }
                }
                if (response.has("Sex")) {
                    String sex = response.optString("Sex", "");
                    if (!sex.isEmpty()) {
                        tvSex.setText(sex);
                    }
                }
                if (response.has("Nation")) {
                    String nation = response.optString("Nation", "");
                    if (!nation.isEmpty()) {
                        tvNation.setText(nation);
                        layoutNation.setVisibility(View.VISIBLE);
                    }
                }
                if (response.has("Birth")) {
                    String birth = response.optString("Birth", "");
                    if (!birth.isEmpty()) {
                        tvBirth.setText(birth);
                    }
                }
                if (response.has("Address")) {
                    String address = response.optString("Address", "");
                    if (!address.isEmpty()) {
                        tvAddress.setText(address);
                    }
                }
                if (response.has("IdNum")) {
                    String idNum = response.optString("IdNum", "");
                    if (!idNum.isEmpty()) {
                        tvIdNumber.setText(idNum);
                    }
                }
                
                // 解析背面信息
                if (response.has("Authority")) {
                    String authority = response.optString("Authority", "");
                    if (!authority.isEmpty()) {
                        tvAuthority.setText(authority);
                        layoutAuthority.setVisibility(View.VISIBLE);
                    }
                }
                if (response.has("ValidDate")) {
                    String validDate = response.optString("ValidDate", "");
                    if (!validDate.isEmpty()) {
                        tvValidDate.setText(validDate);
                        layoutValidDate.setVisibility(View.VISIBLE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析失败，直接显示原始结果用于调试
            tvName.setText("解析失败");
            tvSex.setText("");
            tvBirth.setText("");
            tvAddress.setText("原始数据：" + jsonResult);
            tvIdNumber.setText("");
        }
    }

    private void exportResult() {
        String result = AppData.getInstance().getOcrResult();
        if (result == null || result.isEmpty()) {
            Toast.makeText(this, "暂无可导出的识别结果", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ExportUtil.exportSingleResult(this, result);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

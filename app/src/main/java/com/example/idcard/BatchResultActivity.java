package com.example.idcard;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class BatchResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_result);

        TextView tvSummary = findViewById(R.id.tv_batch_summary);
        LinearLayout list = findViewById(R.id.ll_batch_results);
        LayoutInflater inflater = LayoutInflater.from(this);

        List<BatchOcrResult> rows = AppData.getInstance().getBatchOcrResults();

        int ok = 0;
        for (BatchOcrResult r : rows) {
            if (r.getIdCardInfo() != null && r.getIdCardInfo().isSuccess()) {
                ok++;
            }
        }
        tvSummary.setText("共 " + rows.size() + " 张，成功 " + ok + " 张");

        list.removeAllViews();
        for (BatchOcrResult row : rows) {
            View item = inflater.inflate(R.layout.item_batch_result, list, false);
            bindBatchRow(item, row);
            list.addView(item);
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_export).setOnClickListener(v -> exportBatchResults(rows));
    }

    private void bindBatchRow(View item, BatchOcrResult row) {
        IdCardInfo info = row.getIdCardInfo();
        TextView tvIndex = item.findViewById(R.id.tv_index);
        TextView tvSide = item.findViewById(R.id.tv_side);
        TextView tvStatus = item.findViewById(R.id.tv_status);
        LinearLayout layoutInfo = item.findViewById(R.id.layout_info);
        TextView tvName = item.findViewById(R.id.tv_name);
        TextView tvIdNumber = item.findViewById(R.id.tv_id_number);
        TextView tvExtra = item.findViewById(R.id.tv_extra);
        TextView tvError = item.findViewById(R.id.tv_error);

        tvIndex.setText(String.valueOf(row.getIndex()));
        boolean isFront = "FRONT".equals(row.getCardSide());
        tvSide.setText(isFront ? "人像面（正面）" : "国徽面（反面）");

        if (info != null && info.isSuccess()) {
            layoutInfo.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.GONE);
            tvStatus.setText("识别成功");
            tvStatus.setTextColor(0xFF4CAF50);

            if (isFront) {
                tvName.setText("姓名：" + nullToDash(info.getName()));
                tvIdNumber.setText("身份证号：" + nullToDash(info.getIdNum()));
                String extra = buildFrontExtra(info);
                if (!extra.isEmpty()) {
                    tvExtra.setText(extra);
                    tvExtra.setVisibility(View.VISIBLE);
                } else {
                    tvExtra.setVisibility(View.GONE);
                }
            } else {
                tvName.setText("签发机关：" + nullToDash(info.getAuthority()));
                tvIdNumber.setText("有效期限：" + nullToDash(info.getValidDate()));
                tvExtra.setVisibility(View.GONE);
            }
        } else {
            layoutInfo.setVisibility(View.GONE);
            tvError.setVisibility(View.VISIBLE);
            tvStatus.setText("识别失败");
            tvStatus.setTextColor(0xFFF44336);
            String msg = info != null && !TextUtils.isEmpty(info.getErrorMessage())
                    ? info.getErrorMessage() : "未知错误";
            tvError.setText(msg);
        }
    }

    private static String nullToDash(String s) {
        return TextUtils.isEmpty(s) ? "—" : s;
    }

    private static String buildFrontExtra(IdCardInfo info) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(info.getSex())) {
            sb.append("性别：").append(info.getSex());
        }
        if (!TextUtils.isEmpty(info.getNation())) {
            if (sb.length() > 0) sb.append("  ");
            sb.append("民族：").append(info.getNation());
        }
        if (!TextUtils.isEmpty(info.getBirth())) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("出生：").append(info.getBirth());
        }
        if (!TextUtils.isEmpty(info.getAddress())) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("住址：").append(info.getAddress());
        }
        return sb.toString();
    }

    private void exportBatchResults(List<BatchOcrResult> rows) {
        if (rows == null || rows.isEmpty()) {
            Toast.makeText(this, "暂无可导出的批量结果", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ExportUtil.exportBatchResults(this, rows);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

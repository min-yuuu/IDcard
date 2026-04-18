package com.example.idcard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class ExportUtil {

    private ExportUtil() {
    }

    public static void exportSingleResult(Context context, String rawJson) throws Exception {
        IdCardInfo info = IdCardInfo.fromTencentOcrJson(rawJson);
        String content = buildSingleText(info, rawJson);
        File file = writeFile(context, "idcard_single_", ".txt", content, false);
        shareFile(context, file, "text/plain", "分享单张识别结果");
    }

    public static void exportBatchResults(Context context, List<BatchOcrResult> results) throws Exception {
        String content = buildBatchCsv(results);
        File file = writeFile(context, "idcard_batch_", ".csv", content, true);
        shareFile(context, file, "text/csv", "分享批量识别结果");
    }

    private static File writeFile(Context context, String prefix, String suffix, String content, boolean withBom) throws Exception {
        File dir = new File(context.getCacheDir(), "exports");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("创建导出目录失败");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(dir, prefix + timestamp + suffix);

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        try {
            if (withBom) {
                writer.write('\uFEFF');
            }
            writer.write(content);
        } finally {
            writer.close();
        }
        return file;
    }

    private static void shareFile(Context context, File file, String mimeType, String chooserTitle) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, chooserTitle));
    }

    private static String buildSingleText(IdCardInfo info, String rawJson) {
        StringBuilder sb = new StringBuilder();
        sb.append("身份证识别结果").append('\n');
        sb.append("导出时间：").append(now()).append('\n').append('\n');

        if (info.isSuccess()) {
            appendLine(sb, "姓名", info.getName());
            appendLine(sb, "性别", info.getSex());
            appendLine(sb, "民族", info.getNation());
            appendLine(sb, "出生", info.getBirth());
            appendLine(sb, "住址", info.getAddress());
            appendLine(sb, "身份证号", info.getIdNum());
            appendLine(sb, "签发机关", info.getAuthority());
            appendLine(sb, "有效期限", info.getValidDate());
        } else {
            appendLine(sb, "状态", "识别失败");
            appendLine(sb, "错误信息", info.getErrorMessage());
        }

        if (!TextUtils.isEmpty(rawJson)) {
            sb.append('\n').append("原始响应：").append('\n').append(rawJson);
        }
        return sb.toString();
    }

    private static String buildBatchCsv(List<BatchOcrResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("序号,识别面,状态,姓名,性别,民族,出生,住址,身份证号,签发机关,有效期限,错误信息").append('\n');
        for (BatchOcrResult row : results) {
            IdCardInfo info = row.getIdCardInfo();
            boolean success = info != null && info.isSuccess();
            sb.append(row.getIndex()).append(',');
            sb.append(csv(sideText(row.getCardSide()))).append(',');
            sb.append(csv(success ? "识别成功" : "识别失败")).append(',');
            sb.append(csv(success && info != null ? info.getName() : "")).append(',');
            sb.append(csv(success && info != null ? info.getSex() : "")).append(',');
            sb.append(csv(success && info != null ? info.getNation() : "")).append(',');
            sb.append(csv(success && info != null ? info.getBirth() : "")).append(',');
            sb.append(csv(success && info != null ? info.getAddress() : "")).append(',');
            sb.append(csvExcelText(success && info != null ? info.getIdNum() : "")).append(',');
            sb.append(csv(success && info != null ? info.getAuthority() : "")).append(',');
            sb.append(csv(success && info != null ? info.getValidDate() : "")).append(',');
            sb.append(csv(info != null ? info.getErrorMessage() : "")).append('\n');
        }
        return sb.toString();
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    /**
     * Force Excel to keep long numbers as text.
     */
    private static String csvExcelText(String value) {
        if (TextUtils.isEmpty(value)) {
            return "\"\"";
        }
        // Append a tab so Excel treats long numbers as text, not scientific notation.
        return csv(value + "\t");
    }

    private static void appendLine(StringBuilder sb, String label, String value) {
        if (!TextUtils.isEmpty(value)) {
            sb.append(label).append("：").append(value).append('\n');
        }
    }

    private static String sideText(String cardSide) {
        return "BACK".equals(cardSide) ? "国徽面（反面）" : "人像面（正面）";
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}

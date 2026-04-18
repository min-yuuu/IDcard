package com.example.idcard;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_MULTI_PICK = 101;

    private ImageView ivIdCard;
    private Button btnRecognize;
    private Button btnViewResult;
    private RadioGroup rgCardSide;
    private RadioButton rbFront;

    private Uri currentImageUri;

    private LinearLayout llSelectedImages;
    private final List<SelectedImageEntry> selectedImages = new ArrayList<>();
    private Button btnRecognizeBatch;
    private Button btnViewBatchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        updateBatchUiState();
    }

    private void initViews() {
        ivIdCard = findViewById(R.id.iv_id_card);
        rgCardSide = findViewById(R.id.rg_card_side);
        rbFront = findViewById(R.id.rb_front);

        btnRecognize = findViewById(R.id.btn_recognize);
        btnViewResult = findViewById(R.id.btn_view_result);

        btnRecognize.setEnabled(false);
        btnViewResult.setEnabled(false);

        llSelectedImages = findViewById(R.id.ll_selected_images);

        btnRecognizeBatch = findViewById(R.id.btn_recognize_batch);
        btnViewBatchResult = findViewById(R.id.btn_view_batch_result);
        btnViewBatchResult.setEnabled(AppData.getInstance().hasBatchResults());
    }

    private void setupListeners() {
        findViewById(R.id.btn_select_image).setOnClickListener(v -> selectSingleImage());
        btnRecognize.setOnClickListener(v -> recognizeIdCard());
        btnViewResult.setOnClickListener(v -> viewResult());

        findViewById(R.id.btn_select_multiple).setOnClickListener(v -> selectMultipleImages());
        findViewById(R.id.btn_clear_selected).setOnClickListener(v -> clearSelectedImages());
        btnRecognizeBatch.setOnClickListener(v -> recognizeBatch());
        btnViewBatchResult.setOnClickListener(v -> viewBatchResult());
    }

    private void clearSelectedImages() {
        selectedImages.clear();
        llSelectedImages.removeAllViews();
        updateBatchUiState();
    }

    private void updateBatchUiState() {
        btnRecognizeBatch.setEnabled(!selectedImages.isEmpty());
    }

    private int addSelectedUris(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            return 0;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        int addedCount = 0;
        for (Uri u : uris) {
            if (u == null) {
                continue;
            }
            try {
                SelectedImageEntry entry = new SelectedImageEntry(u, "FRONT");
                selectedImages.add(entry);
                View row = inflater.inflate(R.layout.item_selected_image, llSelectedImages, false);
                bindSelectedRow(row, entry);
                llSelectedImages.addView(row);
                addedCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateBatchUiState();
        return addedCount;
    }

    private void bindSelectedRow(final View row, final SelectedImageEntry entry) {
        ImageView ivThumb = row.findViewById(R.id.iv_thumb);
        RadioGroup rgSide = row.findViewById(R.id.rg_item_side);
        RadioButton rbFrontRow = row.findViewById(R.id.rb_item_front);
        RadioButton rbBackRow = row.findViewById(R.id.rb_item_back);
        ImageButton btnRemove = row.findViewById(R.id.btn_remove);

        Glide.with(this)
                .load(entry.getUri())
                .centerCrop()
                .into(ivThumb);
        rgSide.setOnCheckedChangeListener(null);
        if (entry.isFront()) {
            rbFrontRow.setChecked(true);
        } else {
            rbBackRow.setChecked(true);
        }
        rgSide.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_item_front) {
                entry.setCardSide("FRONT");
            } else if (checkedId == R.id.rb_item_back) {
                entry.setCardSide("BACK");
            }
        });
        btnRemove.setOnClickListener(v -> {
            selectedImages.remove(entry);
            llSelectedImages.removeView(row);
            updateBatchUiState();
        });
    }

    private void selectSingleImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void selectMultipleImages() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_MULTI_PICK);
    }

    private void recognizeIdCard() {
        if (currentImageUri == null) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRecognize.setEnabled(false);
        Toast.makeText(this, "正在识别...", Toast.LENGTH_SHORT).show();

        String cardSide = rbFront.isChecked() ? "FRONT" : "BACK";

        new Thread(() -> {
            try {
                String result = OCRUtil.recognizeIdCard(MainActivity.this, currentImageUri, cardSide);

                runOnUiThread(() -> {
                    AppData.getInstance().setOcrResult(result);
                    btnViewResult.setEnabled(true);
                    btnRecognize.setEnabled(true);
                    Toast.makeText(MainActivity.this, "识别成功", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnRecognize.setEnabled(true);
                    Toast.makeText(MainActivity.this, "识别失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void recognizeBatch() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "请先选择多张照片", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRecognizeBatch.setEnabled(false);
        Toast.makeText(this, "正在批量识别...", Toast.LENGTH_SHORT).show();

        List<SelectedImageEntry> snapshot = new ArrayList<>(selectedImages);

        new Thread(() -> {
            List<BatchOcrResult> results = new ArrayList<>();
            for (int idx = 0; idx < snapshot.size(); idx++) {
                SelectedImageEntry e = snapshot.get(idx);
                try {
                    String json = OCRUtil.recognizeIdCard(MainActivity.this, e.getUri(), e.getCardSide());
                    IdCardInfo info = IdCardInfo.fromTencentOcrJson(json);
                    results.add(new BatchOcrResult(idx + 1, e.getCardSide(), json, info));
                } catch (Exception ex) {
                    IdCardInfo err = new IdCardInfo();
                    err.setSuccess(false);
                    err.setErrorMessage(ex.getMessage() != null ? ex.getMessage() : "识别异常");
                    results.add(new BatchOcrResult(idx + 1, e.getCardSide(), null, err));
                }
            }
            runOnUiThread(() -> {
                AppData.getInstance().setBatchOcrResults(results);
                btnRecognizeBatch.setEnabled(true);
                btnViewBatchResult.setEnabled(true);
                Toast.makeText(MainActivity.this, "批量识别完成", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void viewResult() {
        if (AppData.getInstance().getOcrResult() == null) {
            Toast.makeText(this, "请先进行识别", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, ResultActivity.class));
    }

    private void viewBatchResult() {
        if (!AppData.getInstance().hasBatchResults()) {
            Toast.makeText(this, "请先进行批量识别", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, BatchResultActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        if (requestCode == REQUEST_IMAGE_PICK) {
            currentImageUri = data.getData();
            if (currentImageUri != null) {
                Glide.with(this)
                        .load(currentImageUri)
                        .centerCrop()
                        .into(ivIdCard);
                btnRecognize.setEnabled(true);
                Toast.makeText(this, "图片选择成功", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_MULTI_PICK) {
            int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            List<Uri> uris = new ArrayList<>();
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri u = clipData.getItemAt(i).getUri();
                    if (u != null) {
                        uris.add(u);
                        try {
                            getContentResolver().takePersistableUriPermission(u, takeFlags);
                        } catch (SecurityException ignored) {
                        }
                    }
                }
            } else if (data.getData() != null) {
                Uri u = data.getData();
                uris.add(u);
                try {
                    getContentResolver().takePersistableUriPermission(u, takeFlags);
                } catch (SecurityException ignored) {
                }
            }
            if (uris.isEmpty()) {
                Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show();
            } else {
                int addedCount = addSelectedUris(uris);
                if (addedCount > 0) {
                    int failedCount = uris.size() - addedCount;
                    if (failedCount > 0) {
                        Toast.makeText(this, "已添加 " + addedCount + " 张，失败 " + failedCount + " 张", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "已添加 " + addedCount + " 张，请为每张选择正反面", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "图片加载失败，请换几张图片重试", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}

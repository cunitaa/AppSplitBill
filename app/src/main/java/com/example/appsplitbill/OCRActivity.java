package com.example.appsplitbill;

import android.app.Activity;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.appsplitbill.model.BillItem;
import com.example.appsplitbill.utils.CurrencyFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private PreviewView viewFinder;
    private TextView tvResult;
    private MaterialButton btnScan, btnUseResult;
    private TextRecognizer recognizer;
    private ExecutorService cameraExecutor;
    private ArrayList<BillItem> detectedItems = new ArrayList<>();
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_ocr);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbarOcr);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewFinder = findViewById(R.id.viewFinder);
        tvResult = findViewById(R.id.tvOcrResult);
        btnScan = findViewById(R.id.btnScan);
        btnUseResult = findViewById(R.id.btnUseResult);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                if (!isPaused) {
                    // Action: CAPTURE/FREEZE
                    if (detectedItems.isEmpty()) {
                        Toast.makeText(this, "Belum ada menu terdeteksi!", Toast.LENGTH_SHORT).show();
                    } else {
                        isPaused = true;
                        btnScan.setText("ULANGI");
                        btnScan.setIconResource(android.R.drawable.ic_menu_rotate);
                        btnUseResult.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Teks dikunci! Klik 'GUNAKAN HASIL' jika sudah benar.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Action: RESUME
                    isPaused = false;
                    btnScan.setText("SCAN");
                    btnScan.setIconResource(android.R.drawable.ic_menu_camera);
                    btnUseResult.setVisibility(View.GONE);
                    detectedItems.clear();
                    tvResult.setText("Arahkan ke daftar harga struk...");
                }
            });
        }

        if (btnUseResult != null) {
            btnUseResult.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("DETECTED_ITEMS", detectedItems);
                resultIntent.putExtra("DETECTED_TAX", detectedTax);
                resultIntent.putExtra("DETECTED_SERVICE", detectedService);
                resultIntent.putExtra("DETECTED_DISCOUNT", detectedDiscount);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            });
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal memulai kamera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        if (isPaused) {
            imageProxy.close();
            return;
        }
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        extractPricesAndMenu(text);
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private double detectedTax = 0, detectedService = 0, detectedDiscount = 0;

    private void extractPricesAndMenu(Text text) {
        detectedItems.clear();
        detectedTax = 0; detectedService = 0; detectedDiscount = 0;
        StringBuilder result = new StringBuilder("🔍 DETEKSI TEKS:\n");
        String fullRawText = text.getText().toLowerCase();
        
        if (fullRawText.isEmpty()) {
            runOnUiThread(() -> tvResult.setText("Arahkan ke daftar harga struk..."));
            return;
        }

        Pattern pricePattern = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})+)");
        
        for (Text.TextBlock block : text.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText().trim();
                String lowerLine = lineText.toLowerCase();
                Matcher matcher = pricePattern.matcher(lineText);
                
                if (matcher.find()) {
                    String priceStr = matcher.group(1).replace(".", "").replace(",", "");
                    try {
                        double price = Double.parseDouble(priceStr);
                        if (price < 100) continue; 

                        // Smart detection for Tax, Service, Discount
                        if (lowerLine.contains("pajak") || lowerLine.contains("tax") || lowerLine.contains("ppn")) {
                            detectedTax = price;
                            result.append("📌 Pajak: ").append(CurrencyFormatter.formatRupiah(price)).append("\n");
                            continue;
                        }
                        if (lowerLine.contains("service") || lowerLine.contains("servis") || lowerLine.contains("sc")) {
                            detectedService = price;
                            result.append("📌 Servis: ").append(CurrencyFormatter.formatRupiah(price)).append("\n");
                            continue;
                        }
                        if (lowerLine.contains("disc") || lowerLine.contains("potongan") || lowerLine.contains("promo")) {
                            detectedDiscount = price;
                            result.append("📌 Diskon: ").append(CurrencyFormatter.formatRupiah(price)).append("\n");
                            continue;
                        }

                        String name = lineText.replace(matcher.group(1), "").trim();
                        name = name.replaceAll("[^a-zA-Z0-9 ]", "").trim();
                        if (name.isEmpty()) name = "Menu " + (detectedItems.size() + 1);
                        
                        detectedItems.add(new BillItem(name, price, 1));
                        result.append("✅ ").append(name).append(" (").append(CurrencyFormatter.formatRupiah(price)).append(")\n");
                    } catch (Exception ignored) {}
                }
            }
        }

        if (detectedItems.isEmpty() && detectedTax == 0) {
            runOnUiThread(() -> {
                tvResult.setText("Mendeteksi teks, tapi belum menemukan daftar harga...");
                btnUseResult.setVisibility(View.GONE);
            });
        } else {
            runOnUiThread(() -> {
                tvResult.setText(result.toString());
                btnUseResult.setVisibility(View.VISIBLE);
            });
        }
    }



    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        recognizer.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && allPermissionsGranted()) {
            startCamera();
        }
    }
}

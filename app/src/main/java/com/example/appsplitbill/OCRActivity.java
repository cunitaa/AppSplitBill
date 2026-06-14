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
    private Button btnUseResult;
    private TextRecognizer recognizer;
    private ExecutorService cameraExecutor;
    private ArrayList<BillItem> detectedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        Toolbar toolbar = findViewById(R.id.toolbarOcr);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewFinder = findViewById(R.id.viewFinder);
        tvResult = findViewById(R.id.tvOcrResult);
        Button btnScan = findViewById(R.id.btnScan);
        btnUseResult = findViewById(R.id.btnUseResult);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        btnScan.setOnClickListener(v -> {
            if (detectedItems.isEmpty()) {
                Toast.makeText(this, "Arahkan kamera ke daftar harga sampai muncul rincian!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Berhasil mendeteksi " + detectedItems.size() + " menu!", Toast.LENGTH_SHORT).show();
            }
        });

        btnUseResult.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("DETECTED_ITEMS", detectedItems);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
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

    private void extractPricesAndMenu(Text text) {
        detectedItems.clear();
        StringBuilder result = new StringBuilder("🔍 DETEKSI TEKS:\n");
        String fullRawText = text.getText();
        
        if (fullRawText.isEmpty()) {
            runOnUiThread(() -> tvResult.setText("Arahkan ke daftar harga struk..."));
            return;
        }

        // Broad search for any currency-like numbers
        Pattern pricePattern = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})+)");
        
        for (Text.TextBlock block : text.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText().trim();
                Matcher matcher = pricePattern.matcher(lineText);
                
                if (matcher.find()) {
                    String priceStr = matcher.group(1).replace(".", "").replace(",", "");
                    try {
                        double price = Double.parseDouble(priceStr);
                        // Filter out small numbers that are likely not prices (e.g., dates, quantities)
                        if (price < 100) continue; 

                        String name = lineText.replace(matcher.group(1), "").trim();
                        // Clean up name from garbage symbols
                        name = name.replaceAll("[^a-zA-Z0-9 ]", "").trim();
                        
                        if (name.isEmpty()) name = "Menu " + (detectedItems.size() + 1);
                        
                        detectedItems.add(new BillItem(name, price, 1));
                        result.append("✅ ").append(name).append(" -> Rp ").append(priceStr).append("\n");
                    } catch (Exception ignored) {}
                }
            }
        }

        if (detectedItems.isEmpty()) {
            runOnUiThread(() -> {
                tvResult.setText("Mendeteksi teks, tapi belum menemukan daftar harga...\n\nRaw: " + (fullRawText.length() > 50 ? fullRawText.substring(0, 50) : fullRawText));
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

package com.example.appsplitbill;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appsplitbill.model.BillItem;
import com.example.appsplitbill.utils.CurrencyFormatter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class ItemizedSplitActivity extends AppCompatActivity {
    private ArrayList<BillItem> items = new ArrayList<>();
    private double currentSubtotal = 0;

    private final ActivityResultLauncher<Intent> ocrLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<BillItem> scannedItems = (ArrayList<BillItem>) result.getData().getSerializableExtra("DETECTED_ITEMS");
                    if (scannedItems != null) {
                        for (BillItem item : scannedItems) {
                            items.add(item);
                            currentSubtotal += (item.getPrice() * item.getQuantity());
                        }
                        updateListAndTotal(findViewById(R.id.tvItemList), findViewById(R.id.tvTotalItemized),
                                findViewById(R.id.etItemTax), findViewById(R.id.etItemService), findViewById(R.id.etItemDiscount));
                        
                        // User wants to go STRAIGHT to people selection after successful scan
                        // No dialog, just a quick confirmation toast and move forward.
                        Toast.makeText(this, scannedItems.size() + " menu ditambahkan. Lanjut pilih orang...", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.btnNextItemized).performClick();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemized_split);

        Toolbar toolbar = findViewById(R.id.toolbarItemized);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText etTitle = findViewById(R.id.etItemizedTitle);
        EditText etName = findViewById(R.id.etItemName);
        EditText etPrice = findViewById(R.id.etItemPrice);
        EditText etQty = findViewById(R.id.etItemQty);
        EditText etTax = findViewById(R.id.etItemTax);
        EditText etService = findViewById(R.id.etItemService);
        EditText etDiscount = findViewById(R.id.etItemDiscount);
        MaterialButton btnAdd = findViewById(R.id.btnAddItem);
        MaterialButton btnScan = findViewById(R.id.btnScanBill);
        TextView tvList = findViewById(R.id.tvItemList);
        TextView tvTotal = findViewById(R.id.tvTotalItemized);
        MaterialButton btnNext = findViewById(R.id.btnNextItemized);

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> ocrLauncher.launch(new Intent(this, OCRActivity.class)));
        }

        if (getIntent().getBooleanExtra("OPEN_OCR", false)) {
            ocrLauncher.launch(new Intent(this, OCRActivity.class));
        }

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String priceS = etPrice.getText().toString();
            String qtyS = etQty != null ? etQty.getText().toString() : "1";

            if (name.isEmpty() || priceS.isEmpty()) {
                Toast.makeText(this, "Isi nama dan harga!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceS);
                int qty = qtyS.isEmpty() ? 1 : Integer.parseInt(qtyS);
                
                items.add(new BillItem(name, price, qty));
                currentSubtotal += (price * qty);

                updateListAndTotal(tvList, tvTotal, etTax, etService, etDiscount);

                etName.setText("");
                etPrice.setText("");
                if (etQty != null) etQty.setText("1");
            } catch (Exception e) {
                Toast.makeText(this, "Input tidak valid!", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (items.isEmpty()) {
                Toast.makeText(this, "Tambah item dulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) title = "Tagihan Per Item";

            String taxS = etTax.getText().toString();
            String serviceS = etService.getText().toString();
            String discS = etDiscount.getText().toString();
            double taxPercent = taxS.isEmpty() ? 0 : Double.parseDouble(taxS);
            double servicePercent = serviceS.isEmpty() ? 0 : Double.parseDouble(serviceS);
            double discVal = discS.isEmpty() ? 0 : Double.parseDouble(discS);
            
            double taxAmount = (currentSubtotal * taxPercent / 100);
            double serviceAmount = (currentSubtotal * servicePercent / 100);

            Intent intent = new Intent(this, SelectPeopleActivity.class);
            intent.putExtra("TITLE", title);
            intent.putExtra("ITEMS", items);
            intent.putExtra("TAX_AMOUNT", taxAmount);
            intent.putExtra("SERVICE_AMOUNT", serviceAmount);
            intent.putExtra("DISCOUNT", discVal);
            startActivity(intent);
        });
    }

    private void updateListAndTotal(TextView tvList, TextView tvTotal, EditText etTax, EditText etService, EditText etDiscount) {
        if (items.isEmpty()) {
            tvList.setText("Belum ada item.");
            tvTotal.setText("Estimasi Total: Rp 0");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (BillItem item : items) {
            sb.append("• ").append(item.getName())
              .append(" x").append(item.getQuantity())
              .append(" (").append(CurrencyFormatter.formatRupiah(item.getPrice() * item.getQuantity()))
              .append(")\n");
        }
        tvList.setText(sb.toString());
        
        String taxS = etTax.getText().toString();
        String serviceS = etService.getText().toString();
        String discS = etDiscount.getText().toString();
        double taxP = taxS.isEmpty() ? 0 : Double.parseDouble(taxS);
        double serviceP = serviceS.isEmpty() ? 0 : Double.parseDouble(serviceS);
        double disc = discS.isEmpty() ? 0 : Double.parseDouble(discS);
        
        double total = currentSubtotal + (currentSubtotal * taxP / 100) + (currentSubtotal * serviceP / 100) - disc;
        tvTotal.setText("Estimasi Total: " + CurrencyFormatter.formatRupiah(total));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

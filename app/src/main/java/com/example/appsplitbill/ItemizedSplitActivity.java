package com.example.appsplitbill;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appsplitbill.model.BillItem;
import com.example.appsplitbill.utils.CurrencyFormatter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class ItemizedSplitActivity extends AppCompatActivity {
    private ArrayList<BillItem> items = new ArrayList<>();
    private ItemEditorAdapter adapter;
    private TextView tvTotal;
    private EditText etTax, etService, etDiscount;

    private final ActivityResultLauncher<Intent> ocrLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<BillItem> scannedItems = (ArrayList<BillItem>) result.getData().getSerializableExtra("DETECTED_ITEMS");
                    if (scannedItems != null) {
                        items.addAll(scannedItems);
                        
                        double taxVal = result.getData().getDoubleExtra("DETECTED_TAX", 0);
                        double serviceVal = result.getData().getDoubleExtra("DETECTED_SERVICE", 0);
                        double discVal = result.getData().getDoubleExtra("DETECTED_DISCOUNT", 0);

                        double sub = calculateSubtotal();
                        if (taxVal > 0 && sub > 0) etTax.setText(String.valueOf(Math.round((taxVal/sub)*100)));
                        if (serviceVal > 0 && sub > 0) etService.setText(String.valueOf(Math.round((serviceVal/sub)*100)));
                        if (discVal > 0) etDiscount.setText(String.valueOf((int)discVal));

                        adapter.notifyDataSetChanged();
                        updateFinalTotal();
                        Toast.makeText(this, scannedItems.size() + " menu ditambahkan. Kamu bisa edit jika ada typo!", Toast.LENGTH_LONG).show();
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
        etTax = findViewById(R.id.etItemTax);
        etService = findViewById(R.id.etItemService);
        etDiscount = findViewById(R.id.etItemDiscount);
        tvTotal = findViewById(R.id.tvTotalItemized);
        
        MaterialButton btnAdd = findViewById(R.id.btnAddItem);
        MaterialButton btnScan = findViewById(R.id.btnScanBill);
        MaterialButton btnNext = findViewById(R.id.btnNextItemized);

        RecyclerView rv = findViewById(R.id.rvItemEditor);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemEditorAdapter();
        rv.setAdapter(adapter);

        btnScan.setOnClickListener(v -> ocrLauncher.launch(new Intent(this, OCRActivity.class)));

        if (getIntent().getBooleanExtra("OPEN_OCR", false)) {
            ocrLauncher.launch(new Intent(this, OCRActivity.class));
        }

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String priceS = etPrice.getText().toString();
            String qtyS = etQty.getText().toString();

            if (name.isEmpty() || priceS.isEmpty()) {
                Toast.makeText(this, "Isi nama dan harga!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceS);
                int qty = qtyS.isEmpty() ? 1 : Integer.parseInt(qtyS);
                items.add(new BillItem(name, price, qty));
                adapter.notifyItemInserted(items.size() - 1);
                updateFinalTotal();
                
                etName.setText("");
                etPrice.setText("");
                etQty.setText("1");
                etName.requestFocus();
            } catch (Exception e) {
                Toast.makeText(this, "Input tidak valid!", Toast.LENGTH_SHORT).show();
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateFinalTotal(); }
        };
        etTax.addTextChangedListener(watcher);
        etService.addTextChangedListener(watcher);
        etDiscount.addTextChangedListener(watcher);

        btnNext.setOnClickListener(v -> {
            if (items.isEmpty()) {
                Toast.makeText(this, "Tambah item dulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) title = "Tagihan Per Item";
            
            Intent intent = new Intent(this, SelectPeopleActivity.class);
            intent.putExtra("TITLE", title);
            intent.putExtra("ITEMS", items);
            intent.putExtra("TAX_AMOUNT", (calculateSubtotal() * getDouble(etTax) / 100));
            intent.putExtra("SERVICE_AMOUNT", (calculateSubtotal() * getDouble(etService) / 100));
            intent.putExtra("DISCOUNT", getDouble(etDiscount));
            startActivity(intent);
        });
    }

    private double calculateSubtotal() {
        double sub = 0;
        for (BillItem item : items) sub += (item.getPrice() * item.getQuantity());
        return sub;
    }

    private void updateFinalTotal() {
        double sub = calculateSubtotal();
        double total = sub + (sub * getDouble(etTax) / 100) + (sub * getDouble(etService) / 100) - getDouble(etDiscount);
        tvTotal.setText("Total: " + CurrencyFormatter.formatRupiah(total));
    }

    private double getDouble(EditText et) {
        String s = et.getText().toString();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }

    private class ItemEditorAdapter extends RecyclerView.Adapter<ItemEditorAdapter.ViewHolder> {
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill_item_edit, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BillItem item = items.get(position);
            
            // Remove previous watchers to avoid recursion/multicalls
            if (holder.nameWatcher != null) holder.etName.removeTextChangedListener(holder.nameWatcher);
            if (holder.priceWatcher != null) holder.etPrice.removeTextChangedListener(holder.priceWatcher);
            if (holder.qtyWatcher != null) holder.etQty.removeTextChangedListener(holder.qtyWatcher);

            holder.etName.setText(item.getName());
            holder.etPrice.setText(String.valueOf((int)item.getPrice()));
            holder.etQty.setText(String.valueOf(item.getQuantity()));

            holder.nameWatcher = new SimpleTextWatcher(s -> { item.setName(s); });
            holder.priceWatcher = new SimpleTextWatcher(s -> { try { item.setPrice(Double.parseDouble(s)); updateFinalTotal(); } catch(Exception ignored){} });
            holder.qtyWatcher = new SimpleTextWatcher(s -> { try { item.setQuantity(Integer.parseInt(s)); updateFinalTotal(); } catch(Exception ignored){} });

            holder.etName.addTextChangedListener(holder.nameWatcher);
            holder.etPrice.addTextChangedListener(holder.priceWatcher);
            holder.etQty.addTextChangedListener(holder.qtyWatcher);

            holder.btnRemove.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    updateFinalTotal();
                }
            });
        }

        @Override public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            EditText etName, etPrice, etQty;
            ImageButton btnRemove;
            TextWatcher nameWatcher, priceWatcher, qtyWatcher;
            public ViewHolder(@NonNull View v) {
                super(v);
                etName = v.findViewById(R.id.etEditItemName);
                etPrice = v.findViewById(R.id.etEditItemPrice);
                etQty = v.findViewById(R.id.etEditItemQty);
                btnRemove = v.findViewById(R.id.btnRemoveItem);
            }
        }
    }

    private interface OnTextChange { void onTextChanged(String s); }
    private class SimpleTextWatcher implements TextWatcher {
        private OnTextChange listener;
        public SimpleTextWatcher(OnTextChange listener) { this.listener = listener; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) { listener.onTextChanged(s.toString()); }
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}

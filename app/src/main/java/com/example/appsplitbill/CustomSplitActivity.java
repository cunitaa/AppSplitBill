package com.example.appsplitbill;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appsplitbill.model.BillItem;
import com.example.appsplitbill.utils.CurrencyFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomSplitActivity extends AppCompatActivity {

    private String title;
    private double baseAmount;
    private double taxPercent, servicePercent, discount;
    private ArrayList<String> participants;
    private Map<String, Integer> personPercentage = new HashMap<>();
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_split);

        title = getIntent().getStringExtra("TITLE");
        ArrayList<BillItem> items = (ArrayList<BillItem>) getIntent().getSerializableExtra("ITEMS");
        baseAmount = (items != null && !items.isEmpty()) ? items.get(0).getPrice() : 0;
        participants = getIntent().getStringArrayListExtra("PEOPLE");
        taxPercent = getIntent().getDoubleExtra("TAX_PERCENT", 0);
        servicePercent = getIntent().getDoubleExtra("SERVICE_PERCENT", 0);
        discount = getIntent().getDoubleExtra("DISCOUNT", 0);

        Toolbar toolbar = findViewById(R.id.toolbarCustom);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView) findViewById(R.id.tvCustomTotalAmount)).setText(CurrencyFormatter.formatRupiah(baseAmount));
        tvStatus = findViewById(R.id.tvPercentageStatus);

        RecyclerView rv = findViewById(R.id.rvCustomSplit);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new CustomAdapter());

        findViewById(R.id.btnFinishCustomSplit).setOnClickListener(v -> calculateAndFinish());
    }

    private void updateStatus() {
        int total = 0;
        for (int p : personPercentage.values()) total += p;
        tvStatus.setText("Total Persen: " + total + "%");
        if (total > 100) tvStatus.setTextColor(getResources().getColor(R.color.error_red));
        else tvStatus.setTextColor(getResources().getColor(R.color.text_primary));
    }

    private void calculateAndFinish() {
        int totalPercent = 0;
        for (int p : personPercentage.values()) totalPercent += p;

        if (totalPercent != 100) {
            Toast.makeText(this, "Total persentase harus 100% (Sekarang: " + totalPercent + "%)", Toast.LENGTH_SHORT).show();
            return;
        }

        double taxAmount = baseAmount * taxPercent / 100;
        double serviceAmount = baseAmount * servicePercent / 100;
        double finalTotal = baseAmount + taxAmount + serviceAmount - discount;
        double factor = (baseAmount == 0) ? 1 : finalTotal / baseAmount;

        ArrayList<String> results = new ArrayList<>();
        ArrayList<String> detailedBreakdown = new ArrayList<>();

        for (String person : participants) {
            int percent = personPercentage.getOrDefault(person, 0);
            double personBase = baseAmount * percent / 100.0;
            double personTaxService = personBase * (factor - 1);
            double personTotal = personBase * factor;
            double rounded = Math.round(personTotal / 500.0) * 500.0;

            if (rounded > 0) {
                results.add(person + ": " + CurrencyFormatter.formatRupiah(rounded));
                
                StringBuilder sb = new StringBuilder();
                sb.append("👤 ").append(person).append(" (").append(percent).append("%)\n");
                sb.append("  ◦ Porsi Tagihan (").append(CurrencyFormatter.formatRupiah(personBase)).append(")\n");
                if (personTaxService != 0) {
                    sb.append("  ◦ Pajak & Servis (").append(CurrencyFormatter.formatRupiah(personTaxService)).append(")\n");
                }
                sb.append("💰 TOTAL: ").append(CurrencyFormatter.formatRupiah(rounded));
                detailedBreakdown.add(sb.toString());
            }
        }

        Intent intent = new Intent(this, BillResultActivity.class);
        intent.putExtra("TITLE", title);
        intent.putExtra("TOTAL", finalTotal);
        intent.putExtra("TAX", taxAmount);
        intent.putExtra("SERVICE", serviceAmount);
        intent.putExtra("DISCOUNT", discount);
        intent.putStringArrayListExtra("PEOPLE_RESULTS", results);
        intent.putStringArrayListExtra("DETAILED_BREAKDOWN", detailedBreakdown);
        startActivity(intent);
    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_split, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = participants.get(position);
            holder.tv.setText(name);
            
            holder.et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        int val = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                        personPercentage.put(name, val);
                        updateStatus();
                    } catch (Exception ignored) {}
                }
            });
        }

        @Override
        public int getItemCount() { return participants.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            EditText et;
            public ViewHolder(@NonNull View v) {
                super(v);
                tv = v.findViewById(R.id.tvCustomPersonName);
                et = v.findViewById(R.id.etCustomPercent);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

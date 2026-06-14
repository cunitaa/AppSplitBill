package com.example.appsplitbill;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appsplitbill.model.BillItem;
import com.example.appsplitbill.utils.CurrencyFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AssignItemsActivity extends AppCompatActivity {

    private String title;
    private ArrayList<BillItem> itemsList;
    private ArrayList<String> participants;
    private double tax;
    private double service;
    private double discount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_items);

        title = getIntent().getStringExtra("TITLE");
        itemsList = (ArrayList<BillItem>) getIntent().getSerializableExtra("ITEMS");
        participants = getIntent().getStringArrayListExtra("PEOPLE");
        tax = getIntent().getDoubleExtra("TAX", 0);
        service = getIntent().getDoubleExtra("SERVICE", 0);
        discount = getIntent().getDoubleExtra("DISCOUNT", 0);

        Toolbar toolbar = findViewById(R.id.toolbarAssign);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView rv = findViewById(R.id.rvAssignItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new AssignAdapter());

        findViewById(R.id.btnFinishAssign).setOnClickListener(v -> calculateAndFinish());
    }

    private void calculateAndFinish() {
        for (BillItem item : itemsList) {
            if (item.getConsumerNames().isEmpty()) {
                Toast.makeText(this, "Item '" + item.getName() + "' belum dipilih siapa yang makan!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Map<String, Double> personOwed = new HashMap<>();
        double subtotal = 0;
        for (BillItem item : itemsList) {
            double itemTotalPrice = item.getPrice() * item.getQuantity();
            subtotal += itemTotalPrice;
            double pricePerConsumer = itemTotalPrice / item.getConsumerNames().size();
            for (String name : item.getConsumerNames()) {
                personOwed.put(name, personOwed.getOrDefault(name, 0.0) + pricePerConsumer);
            }
        }

        double calculatedTotal = subtotal + tax + service - discount;

        final double finalSubtotal = subtotal;
        final double finalCalculatedTotal = calculatedTotal;
        final Map<String, Double> finalPersonOwed = personOwed;

        // Payer Selection Dialog (Professional Step)
        String[] peopleArray = participants.toArray(new String[0]);
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Siapa yang bayar duluan?")
            .setItems(peopleArray, (dialog, which) -> {
                String payer = peopleArray[which];
                finishWithPayer(payer, finalSubtotal, finalCalculatedTotal, finalPersonOwed);
            })
            .show();
    }

    private void finishWithPayer(String payer, double subtotal, double finalTotal, Map<String, Double> personOwed) {
        double factor = (subtotal == 0) ? 1 : finalTotal / subtotal;
        
        ArrayList<String> results = new ArrayList<>();
        ArrayList<String> detailedBreakdown = new ArrayList<>();
        
        for (String person : participants) {
            double basePrice = personOwed.getOrDefault(person, 0.0);
            double taxAndServiceShare = basePrice * (factor - 1);
            double totalPerPerson = basePrice * factor;
            
            // Rounding to nearest 500
            double rounded = Math.round(totalPerPerson / 500.0) * 500.0;
            
            // Fix: ensure even with rounding, we don't exceed the intended total significantly
            // But for simple splitting, 500 rounding is usually preferred.
            
            // Build detailed string for result page
            StringBuilder breakdown = new StringBuilder();
            breakdown.append("👤 ").append(person).append("\n");
            
            // List items for this person
            boolean hasItems = false;
            for (BillItem item : itemsList) {
                if (item.getConsumerNames().contains(person)) {
                    hasItems = true;
                    double portionPrice = (item.getPrice() * item.getQuantity()) / item.getConsumerNames().size();
                    breakdown.append("  ◦ ").append(item.getName()).append(" (").append(CurrencyFormatter.formatRupiah(portionPrice)).append(")\n");
                }
            }
            
            if (!hasItems) breakdown.append("  ◦ (Tidak memesan item spesifik)\n");

            if (taxAndServiceShare != 0) {
                breakdown.append("  ◦ Pajak & Servis (").append(CurrencyFormatter.formatRupiah(taxAndServiceShare)).append(")\n");
            }
            
            breakdown.append("💰 TOTAL: ").append(CurrencyFormatter.formatRupiah(rounded));
            detailedBreakdown.add(breakdown.toString());

            if (!person.equals(payer) && rounded > 0) {
                results.add(person + " → " + payer + ": " + CurrencyFormatter.formatRupiah(rounded));
            }
        }

        Intent intent = new Intent(this, BillResultActivity.class);
        intent.putExtra("TITLE", title);
        intent.putExtra("TOTAL", finalTotal);
        intent.putExtra("TAX", tax);
        intent.putExtra("SERVICE", service);
        intent.putExtra("DISCOUNT", discount);
        intent.putExtra("PAYER", payer);
        intent.putExtra("ITEMS", itemsList);
        intent.putStringArrayListExtra("PEOPLE_RESULTS", results);
        intent.putStringArrayListExtra("DETAILED_BREAKDOWN", detailedBreakdown);
        startActivity(intent);
    }

    private class AssignAdapter extends RecyclerView.Adapter<AssignAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assign_people, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BillItem item = itemsList.get(position);
            holder.tvName.setText(item.getName());
            holder.tvPrice.setText(CurrencyFormatter.formatRupiah(item.getPrice()));

            holder.cg.removeAllViews();
            for (String person : participants) {
                Chip chip = new Chip(AssignItemsActivity.this);
                chip.setText(person);
                chip.setCheckable(true);
                chip.setChecked(item.getConsumerNames().contains(person));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!item.getConsumerNames().contains(person)) {
                            item.getConsumerNames().add(person);
                        }
                    } else {
                        item.getConsumerNames().remove(person);
                    }
                });
                holder.cg.addView(chip);
            }
        }

        @Override
        public int getItemCount() { return itemsList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            ChipGroup cg;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvItemNameAssign);
                tvPrice = v.findViewById(R.id.tvItemPriceAssign);
                cg = v.findViewById(R.id.cgParticipants);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

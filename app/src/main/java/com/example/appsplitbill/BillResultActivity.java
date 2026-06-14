package com.example.appsplitbill;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appsplitbill.model.Bill;
import com.example.appsplitbill.model.BillItem;
import com.example.appsplitbill.model.User;
import com.example.appsplitbill.utils.CurrencyFormatter;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillResultActivity extends AppCompatActivity {

    private User currentUser;
    private boolean isAlreadySaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_result);

        // Load real user data
        android.content.SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userJson = sp.getString(StorageUtils.getUserKey(this, "user_profile"), null);
        if (userJson != null) {
            currentUser = new Gson().fromJson(userJson, User.class);
        } else {
            currentUser = new User("1", "User", StorageUtils.getCurrentUserEmail(this));
            currentUser.setBankInfo("Belum diatur");
        }

        Toolbar toolbar = findViewById(R.id.toolbarResult);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getIntent().getStringExtra("TITLE");
        if (title == null) title = "Tagihan Baru";
        
        double total = getIntent().getDoubleExtra("TOTAL", 0);
        double tax = getIntent().getDoubleExtra("TAX", 0);
        double service = getIntent().getDoubleExtra("SERVICE", 0);
        double discount = getIntent().getDoubleExtra("DISCOUNT", 0);
        String payer = getIntent().getStringExtra("PAYER");
        double subtotal = total - tax - service + discount;
        ArrayList<String> peopleResults = getIntent().getStringArrayListExtra("PEOPLE_RESULTS");
        ArrayList<String> detailedBreakdown = getIntent().getStringArrayListExtra("DETAILED_BREAKDOWN");
        ArrayList<BillItem> itemsList = (ArrayList<BillItem>) getIntent().getSerializableExtra("ITEMS");

        ((TextView)findViewById(R.id.tvResultTitle)).setText(title);
        ((TextView)findViewById(R.id.tvResultTotal)).setText(CurrencyFormatter.formatRupiah(total));
        ((TextView)findViewById(R.id.tvResSubtotal)).setText(CurrencyFormatter.formatRupiah(subtotal));
        ((TextView)findViewById(R.id.tvResTax)).setText("+ " + CurrencyFormatter.formatRupiah(tax + service));
        ((TextView)findViewById(R.id.tvResDiscount)).setText("- " + CurrencyFormatter.formatRupiah(discount));

        LinearLayout layoutMenu = findViewById(R.id.layoutMenuDetails);
        if (itemsList != null) {
            for (BillItem item : itemsList) {
                View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, null);
                TextView text1 = row.findViewById(android.R.id.text1);
                TextView text2 = row.findViewById(android.R.id.text2);
                
                text1.setText(item.getName() + " x" + item.getQuantity());
                text1.setTextColor(getResources().getColor(R.color.text_primary));
                text1.setTextSize(14);
                
                text2.setText(CurrencyFormatter.formatRupiah(item.getPrice() * item.getQuantity()));
                text2.setTextColor(getResources().getColor(R.color.blue_primary));
                text2.setTextSize(12);
                
                layoutMenu.addView(row);
            }
        }

        String bankInfo = (currentUser.getBankInfo() != null) ? currentUser.getBankInfo() : "Belum diatur";
        if (payer != null && !payer.equals(currentUser.getName())) {
             ((TextView)findViewById(R.id.tvPaymentDest)).setText("Dibayar oleh: " + payer);
        } else {
             ((TextView)findViewById(R.id.tvPaymentDest)).setText(bankInfo + " (A/N " + currentUser.getName() + ")");
        }

        LinearLayout layoutParticipants = findViewById(R.id.layoutParticipantsResult);
        if (detailedBreakdown != null) {
            for (String detail : detailedBreakdown) {
                View cardView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);
                TextView text1 = cardView.findViewById(android.R.id.text1);
                text1.setText(detail);
                text1.setTextSize(13);
                text1.setPadding(20, 20, 20, 20);
                text1.setTextColor(getResources().getColor(R.color.text_primary));
                
                // Add separator line
                View line = new View(this);
                line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                line.setBackgroundColor(getResources().getColor(R.color.grey_light));
                
                layoutParticipants.addView(cardView);
                layoutParticipants.addView(line);
            }
        } else if (peopleResults != null) {
            for (String res : peopleResults) {
                View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);
                TextView text1 = row.findViewById(android.R.id.text1);
                text1.setText(res);
                text1.setTextSize(14);
                text1.setTextColor(getResources().getColor(R.color.text_primary));
                layoutParticipants.addView(row);
            }
        }

        String finalTitle = title;
        findViewById(R.id.btnSaveOnly).setOnClickListener(v -> {
            saveToLocal(finalTitle, total, peopleResults);
            Toast.makeText(this, "Tagihan disimpan!", Toast.LENGTH_SHORT).show();
            finishAffinity();
            startActivity(new Intent(this, MainActivity.class));
        });

        findViewById(R.id.btnShareWA).setOnClickListener(v -> {
            saveToLocal(finalTitle, total, peopleResults);
            shareToWA(finalTitle, total, peopleResults, detailedBreakdown);
        });
    }

    private void saveToLocal(String title, double total, ArrayList<String> peopleResults) {
        if (isAlreadySaved) return;

        android.content.SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String key = StorageUtils.getBillHistoryKey(this);
        String json = sp.getString(key, "[]");
        List<Bill> history = gson.fromJson(json, new TypeToken<List<Bill>>(){}.getType());
        if (history == null) history = new ArrayList<>();

        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        Bill bill = new Bill(title, date, total, "PENDING", StorageUtils.getCurrentUserEmail(this));
        bill.setId(String.valueOf(System.currentTimeMillis()));
        bill.setPeopleResults(peopleResults);
        
        // Ensure paidStatus is initialized for settlement tracking
        List<Boolean> initialStatus = new ArrayList<>();
        if (peopleResults != null) {
            for (int i = 0; i < peopleResults.size(); i++) initialStatus.add(false);
        }
        bill.setPaidStatus(initialStatus);

        history.add(0, bill);
        sp.edit().putString(key, gson.toJson(history)).apply();
        isAlreadySaved = true;
    }

    private void shareToWA(String title, double total, ArrayList<String> peopleResults, ArrayList<String> detailedBreakdown) {
        StringBuilder msg = new StringBuilder("*RINCIAN TAGIHAN SPLITBILL*\n");
        msg.append("Judul: ").append(title).append("\n");
        msg.append("Total: ").append(CurrencyFormatter.formatRupiah(total)).append("\n");
        msg.append("----------------------------\n");
        
        if (detailedBreakdown != null) {
            for (String detail : detailedBreakdown) {
                msg.append(detail).append("\n");
                msg.append("----------------------------\n");
            }
        } else if (peopleResults != null) {
            for (String p : peopleResults) msg.append("✅ ").append(p).append("\n");
        }

        String bank = currentUser.getBankInfo() != null ? currentUser.getBankInfo() : "Belum diatur";
        msg.append("\n*Transfer ke:*\n").append(bank).append(" (A/N ").append(currentUser.getName()).append(")\n");
        msg.append("Terima kasih! 🙏");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
        sendIntent.setType("text/plain");
        try {
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch (Exception e) {
            sendIntent.setPackage(null);
            startActivity(Intent.createChooser(sendIntent, "Bagikan tagihan"));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

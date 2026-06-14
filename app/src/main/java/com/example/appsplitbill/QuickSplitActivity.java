package com.example.appsplitbill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appsplitbill.utils.CurrencyFormatter;
import java.util.ArrayList;

public class QuickSplitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_split);

        Toolbar toolbar = findViewById(R.id.toolbarQuick);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        EditText etTitle = findViewById(R.id.etBillTitle);
        EditText etAmount = findViewById(R.id.etTotalAmount);
        EditText etPeopleCount = findViewById(R.id.etNumPeople);
        EditText etTax = findViewById(R.id.etTax);
        EditText etService = findViewById(R.id.etServiceCharge);
        EditText etDiscount = findViewById(R.id.etDiscount);
        android.widget.RadioGroup rgMode = findViewById(R.id.rgSplitMode);
        Button btnCalc = findViewById(R.id.btnCalculateQuick);

        btnCalc.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String amountS = etAmount.getText().toString();
            String peopleS = etPeopleCount.getText().toString();
            String taxS = etTax.getText().toString();
            String serviceS = etService.getText().toString();
            String discountS = etDiscount.getText().toString();

            if (amountS.isEmpty()) {
                Toast.makeText(this, "Isi nominal dulu!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountS);
                double taxPercent = taxS.isEmpty() ? 0 : Double.parseDouble(taxS);
                double servicePercent = serviceS.isEmpty() ? 0 : Double.parseDouble(serviceS);
                double discount = discountS.isEmpty() ? 0 : Double.parseDouble(discountS);

                if (rgMode.getCheckedRadioButtonId() == R.id.rbEqual) {
                    if (peopleS.isEmpty()) {
                        Toast.makeText(this, "Isi jumlah orang!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int count = Integer.parseInt(peopleS);
                    if (count <= 0) return;

                    double taxAmount = amount * taxPercent / 100;
                    double serviceAmount = amount * servicePercent / 100;
                    double finalTotal = amount + taxAmount + serviceAmount - discount;

                    ArrayList<String> peopleResults = new ArrayList<>();
                    double perPerson = finalTotal / count;
                    for (int i = 1; i <= count; i++) {
                        peopleResults.add("Orang " + i + ": " + CurrencyFormatter.formatRupiah(perPerson));
                    }

                    Intent intent = new Intent(this, BillResultActivity.class);
                    intent.putExtra("TITLE", title.isEmpty() ? "Bagi Rata Cepat" : title);
                    intent.putExtra("TOTAL", finalTotal);
                    intent.putExtra("TAX", taxAmount);
                    intent.putExtra("SERVICE", serviceAmount);
                    intent.putExtra("DISCOUNT", discount);
                    
                    ArrayList<com.example.appsplitbill.model.BillItem> dummyItems = new ArrayList<>();
                    dummyItems.add(new com.example.appsplitbill.model.BillItem("Patungan Rata", amount / count, count));
                    intent.putExtra("ITEMS", dummyItems);

                    intent.putStringArrayListExtra("PEOPLE_RESULTS", peopleResults);
                    startActivity(intent);
                } else {
                    // Percentage Mode
                    Intent intent = new Intent(this, SelectPeopleActivity.class);
                    intent.putExtra("TITLE", title.isEmpty() ? "Bagi Persentase" : title);
                    
                    ArrayList<com.example.appsplitbill.model.BillItem> items = new ArrayList<>();
                    items.add(new com.example.appsplitbill.model.BillItem("Total Tagihan", amount, 1));
                    
                    intent.putExtra("ITEMS", items);
                    intent.putExtra("TAX_PERCENT", taxPercent);
                    intent.putExtra("SERVICE_PERCENT", servicePercent);
                    intent.putExtra("DISCOUNT", discount);
                    intent.putExtra("IS_PERCENTAGE_MODE", true);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Input tidak valid!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

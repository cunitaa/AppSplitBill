package com.example.appsplitbill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.example.appsplitbill.model.User;
import com.example.appsplitbill.model.Bill;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.gson.Gson;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText etEmail = findViewById(R.id.etLoginEmail);
        TextInputEditText etPassword = findViewById(R.id.etLoginPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnDemo = findViewById(R.id.btnDemo);
        TextView tvReg = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String pass = Objects.requireNonNull(etPassword.getText()).toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi email dan password!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save login state
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("userEmail", email).apply();

            Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        btnDemo.setOnClickListener(v -> {
            setupDemoData();
            Toast.makeText(this, "Demo Data Loaded!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        tvReg.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void setupDemoData() {
        android.content.SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        sp.edit().putString("userEmail", "demo@app.com").apply();

        Gson gson = new Gson();

        // 1. Setup Profile
        User user = new User("demo_id", "Budi (Host)", "demo@app.com");
        user.setWhatsapp("08123456789");
        user.setBankInfo("BCA 123-456-7890");
        sp.edit().putString(StorageUtils.getUserKey(this, "user_profile"), gson.toJson(user)).apply();

        // 2. Setup Friends (3 People for Demo)
        List<String> friends = Arrays.asList("Andi", "Cici", "Deni");
        sp.edit().putString(StorageUtils.getFriendsListKey(this), gson.toJson(friends)).apply();

        // 3. Setup History
        List<Bill> history = new ArrayList<>();
        Bill oldBill = new Bill("Makan Malam Seafood", "05 Jun 2024", 450000, "LUNAS", "demo@app.com");
        oldBill.setPeopleResults(new ArrayList<>(Arrays.asList("Andi: Rp 150.000", "Cici: Rp 150.000", "Deni: Rp 150.000")));
        history.add(oldBill);
        sp.edit().putString(StorageUtils.getBillHistoryKey(this), gson.toJson(history)).apply();
    }
}

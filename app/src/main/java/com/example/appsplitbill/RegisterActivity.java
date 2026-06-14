package com.example.appsplitbill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appsplitbill.model.User;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextInputEditText etName = findViewById(R.id.etRegName);
        TextInputEditText etEmail = findViewById(R.id.etRegEmail);
        TextInputEditText etPass = findViewById(R.id.etRegPassword);
        Button btnReg = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvBackToLogin);

        btnReg.setOnClickListener(v -> {
            String name = Objects.requireNonNull(etName.getText()).toString().trim();
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String pass = Objects.requireNonNull(etPass.getText()).toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user session
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("userEmail", email).apply();

            // Create and save initial profile
            User user = new User(String.valueOf(System.currentTimeMillis()), name, email);
            user.setBankInfo("Belum diatur");
            user.setWhatsapp("");
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                    .putString(StorageUtils.getUserKey(this, "user_profile"), new Gson().toJson(user))
                    .apply();

            Toast.makeText(this, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finishAffinity();
        });

        tvLogin.setOnClickListener(v -> finish());
    }
}

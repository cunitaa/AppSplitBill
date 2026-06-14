package com.example.appsplitbill.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.appsplitbill.LoginActivity;
import com.example.appsplitbill.databinding.FragmentProfileBinding;
import com.example.appsplitbill.model.User;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private boolean isEditMode = false;
    private User currentUser;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Grant persistable permission if possible (not always needed for simple display but good practice)
                        try {
                            getContext().getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        binding.ivProfilePicture.setImageURI(selectedImageUri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Dummy data
        currentUser = new User("1", "User", "user@example.com");
        currentUser.setWhatsapp("08123456789");
        currentUser.setBankInfo("BCA 123456789");

        loadProfileData();

        binding.ivProfilePicture.setOnClickListener(v -> {
            if (isEditMode) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                pickImageLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "Tekan EDIT untuk mengubah foto", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnEditToggle.setOnClickListener(v -> toggleEditMode(!isEditMode));
        binding.btnUpdateProfile.setOnClickListener(v -> saveProfileData());
        binding.btnLogout.setOnClickListener(v -> logout());

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                isChecked ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : 
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Ensure initial state is locked
        toggleEditMode(false);
    }

    private void loadProfileData() {
        if (getContext() == null || binding == null) return;
        android.content.SharedPreferences sp = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String json = sp.getString(StorageUtils.getUserKey(getContext(), "user_profile"), null);
        
        if (json != null) {
            currentUser = new Gson().fromJson(json, User.class);
        } else {
            // Default data for new users
            currentUser = new User("1", "User", StorageUtils.getCurrentUserEmail(getContext()));
            currentUser.setWhatsapp("08123456789");
            currentUser.setBankInfo("BCA - 123456789");
        }

        binding.etProfileName.setText(currentUser.getName());
        binding.tvProfileNameDisplay.setText(currentUser.getName());
        binding.etProfileWhatsapp.setText(currentUser.getWhatsapp());
        binding.tvDisplayEmail.setText(currentUser.getEmail());

        // Split Bank Info for the new UI
        String bankInfo = currentUser.getBankInfo() != null ? currentUser.getBankInfo() : "";
        if (bankInfo.contains(" - ")) {
            String[] parts = bankInfo.split(" - ");
            binding.etProfileBankProvider.setText(parts[0], false);
            binding.etProfileAccountNumber.setText(parts.length > 1 ? parts[1] : "");
        } else {
            binding.etProfileBankProvider.setText(bankInfo, false);
        }

        // Setup Dropdown
        String[] providers = {"BCA", "BNI", "Mandiri", "BRI", "GOPAY", "OVO", "DANA", "ShopeePay"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, providers);
        binding.etProfileBankProvider.setAdapter(adapter);

        // Load image from prefs if exists
        String imageUriS = sp.getString("profile_image_uri_" + currentUser.getId(), null);
        if (imageUriS != null) {
            binding.ivProfilePicture.setImageURI(Uri.parse(imageUriS));
        }
    }

    private void toggleEditMode(boolean enable) {
        isEditMode = enable;
        if (binding == null) return;
        
        binding.tilProfileName.setEnabled(enable);
        binding.tilProfileWhatsapp.setEnabled(enable);
        binding.tilProfileBank.setEnabled(enable);
        binding.tilProfileAccountNumber.setEnabled(enable);
        
        // Deep lock for the dropdown
        binding.etProfileBankProvider.setEnabled(enable);
        binding.etProfileBankProvider.setClickable(enable);
        binding.etProfileBankProvider.setFocusable(false);
        binding.tilProfileBank.setEndIconVisible(enable); // Hide arrow when locked
        
        // Also explicitly disable the inputs to be sure
        binding.etProfileName.setEnabled(enable);
        binding.etProfileWhatsapp.setEnabled(enable);
        binding.etProfileAccountNumber.setEnabled(enable);
        
        // Disable profile image click if not in edit mode
        binding.ivProfilePicture.setClickable(enable);
        binding.ivProfilePicture.setEnabled(enable);

        binding.btnUpdateProfile.setVisibility(enable ? View.VISIBLE : View.GONE);
        binding.btnEditToggle.setText(enable ? "BATAL" : "EDIT");
        
        if (enable) {
            Toast.makeText(getContext(), "Klik foto atau dropdown bank untuk mengubah", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileData() {
        if (binding == null) return;
        String newName = binding.etProfileName.getText().toString().trim();
        String newWA = binding.etProfileWhatsapp.getText().toString().trim();
        String provider = binding.etProfileBankProvider.getText().toString().trim();
        String accNum = binding.etProfileAccountNumber.getText().toString().trim();
        
        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(newName);
        currentUser.setWhatsapp(newWA);
        currentUser.setBankInfo(provider + " - " + accNum);
        
        // Save to Local
        if (getContext() != null) {
            android.content.SharedPreferences sp = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = sp.edit();
            editor.putString(StorageUtils.getUserKey(getContext(), "user_profile"), new Gson().toJson(currentUser));
            
            if (selectedImageUri != null) {
                editor.putString("profile_image_uri_" + currentUser.getId(), selectedImageUri.toString());
            }
            editor.apply();
        }
        
        binding.tvProfileNameDisplay.setText(newName);
        toggleEditMode(false);
        Toast.makeText(getContext(), "Profil diperbarui!", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        if (getActivity() != null) getActivity().finishAffinity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

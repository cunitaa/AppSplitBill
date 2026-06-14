package com.example.appsplitbill.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.appsplitbill.FriendsActivity;
import com.example.appsplitbill.ItemizedSplitActivity;
import com.example.appsplitbill.OCRActivity;
import com.example.appsplitbill.QuickSplitActivity;
import com.example.appsplitbill.R;
import com.example.appsplitbill.databinding.FragmentDashboardBinding;
import com.example.appsplitbill.model.User;
import com.example.appsplitbill.utils.CurrencyFormatter;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        updateUI();

        binding.btnQuickSplitCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuickSplitActivity.class)));
        binding.btnItemizedSplitCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), ItemizedSplitActivity.class)));
        
        binding.btnScanBillCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ItemizedSplitActivity.class);
            intent.putExtra("OPEN_OCR", true);
            startActivity(intent);
        });

        binding.btnFriendsCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), FriendsActivity.class)));
        
        binding.btnGroupsCard.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.example.appsplitbill.GroupsActivity.class));
        });

        // 11. Instagram-Style Dark Mode & 15. Dark Mode Toggle
        binding.ivProfilePictureHome.setOnClickListener(v -> {
             Navigation.findNavController(v).navigate(R.id.nav_profile);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (binding == null || getContext() == null) return;
        
        android.content.SharedPreferences sp = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String json = sp.getString(StorageUtils.getUserKey(getContext(), "user_profile"), null);
        if (json != null) {
            currentUser = new Gson().fromJson(json, User.class);
        } else {
            currentUser = new User("1", "User", StorageUtils.getCurrentUserEmail(getContext()));
        }

        binding.tvUserNameHome.setText(currentUser.getName());
        
        // Load profile image
        String imageUriS = sp.getString("profile_image_uri_" + currentUser.getId(), null);
        if (imageUriS != null) {
            binding.ivProfilePictureHome.setImageURI(android.net.Uri.parse(imageUriS));
        } else {
            binding.ivProfilePictureHome.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        
        // Calculate settlement status
        double totalToReceive = 0;
        String historyJson = sp.getString(StorageUtils.getBillHistoryKey(getContext()), "[]");
        java.util.List<com.example.appsplitbill.model.Bill> history = new Gson().fromJson(historyJson, new com.google.gson.reflect.TypeToken<java.util.List<com.example.appsplitbill.model.Bill>>(){}.getType());
        
        if (history != null) {
            for (com.example.appsplitbill.model.Bill bill : history) {
                if (bill.getPeopleResults() != null) {
                    for (int i = 0; i < bill.getPeopleResults().size(); i++) {
                        boolean isPaid = (bill.getPaidStatus() != null && i < bill.getPaidStatus().size()) ? bill.getPaidStatus().get(i) : false;
                        if (!isPaid) {
                            String res = bill.getPeopleResults().get(i);
                            try {
                                String clean = res.replaceAll("[^0-9]", "");
                                totalToReceive += Double.parseDouble(clean);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        
        binding.tvYouOwe.setText(CurrencyFormatter.formatRupiah(0));
        binding.tvYouAreOwed.setText(CurrencyFormatter.formatRupiah(totalToReceive));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

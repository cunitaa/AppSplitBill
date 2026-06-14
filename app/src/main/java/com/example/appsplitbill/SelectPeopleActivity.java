package com.example.appsplitbill;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appsplitbill.model.BillItem;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SelectPeopleActivity extends AppCompatActivity {
    private ArrayList<String> selectedPeople = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_people);

        prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbarPeople);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText etName = findViewById(R.id.etPersonName);
        Button btnAdd = findViewById(R.id.btnAddPerson);
        TextView tvList = findViewById(R.id.tvPeopleList);
        ChipGroup cgFriends = findViewById(R.id.cgMyFriends);
        ChipGroup cgGroups = findViewById(R.id.cgMyGroups); // Make sure this exists in layout
        Button btnNext = findViewById(R.id.btnFinishItemized);

        if (getIntent().getBooleanExtra("FOR_GROUP_CREATION", false)) {
            btnNext.setText("SIMPAN GRUP");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Pilih Anggota Grup");
        }

        loadFriends(cgFriends, tvList);
        loadGroups(cgGroups, tvList);

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                addPersonToList(name, tvList);
                etName.setText("");
            }
        });

        btnNext.setOnClickListener(v -> {
            if (selectedPeople.isEmpty()) {
                Toast.makeText(this, "Pilih minimal 1 orang!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (getIntent().getBooleanExtra("FOR_GROUP_CREATION", false)) {
                saveGroupAndFinish();
                return;
            }

            String title = getIntent().getStringExtra("TITLE");
            ArrayList<BillItem> items = (ArrayList<BillItem>) getIntent().getSerializableExtra("ITEMS");
            double tax = getIntent().getDoubleExtra("TAX_AMOUNT", 0);
            double service = getIntent().getDoubleExtra("SERVICE_AMOUNT", 0);
            double disc = getIntent().getDoubleExtra("DISCOUNT", 0);
            boolean isPercentage = getIntent().getBooleanExtra("IS_PERCENTAGE_MODE", false);

            if (isPercentage) {
                Intent intent = new Intent(this, CustomSplitActivity.class);
                intent.putExtra("TITLE", title);
                intent.putExtra("ITEMS", items);
                intent.putExtra("PEOPLE", selectedPeople);
                intent.putExtra("TAX_PERCENT", getIntent().getDoubleExtra("TAX_PERCENT", 0));
                intent.putExtra("SERVICE_PERCENT", getIntent().getDoubleExtra("SERVICE_PERCENT", 0));
                intent.putExtra("DISCOUNT", disc);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, AssignItemsActivity.class);
                intent.putExtra("TITLE", title);
                intent.putExtra("ITEMS", items);
                intent.putExtra("PEOPLE", selectedPeople);
                intent.putExtra("TAX", tax);
                intent.putExtra("SERVICE", service);
                intent.putExtra("DISCOUNT", disc);
                startActivity(intent);
            }
        });
    }

    private void loadGroups(ChipGroup cg, TextView tvList) {
        if (cg == null) return;
        String json = prefs.getString("user_groups", "[]");
        List<com.example.appsplitbill.model.Group> groups = new Gson().fromJson(json, new TypeToken<List<com.example.appsplitbill.model.Group>>(){}.getType());
        if (groups != null) {
            for (com.example.appsplitbill.model.Group g : groups) {
                Chip chip = new Chip(this);
                chip.setText("Grup: " + g.getName());
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((bv, isChecked) -> {
                    if (isChecked) {
                        for (String m : g.getMemberNames()) addPersonToList(m, tvList);
                    }
                });
                cg.addView(chip);
            }
        }
    }

    private void saveGroupAndFinish() {
        String groupName = getIntent().getStringExtra("TEMP_GROUP_NAME");
        String json = prefs.getString("user_groups", "[]");
        List<com.example.appsplitbill.model.Group> groups = new Gson().fromJson(json, new TypeToken<List<com.example.appsplitbill.model.Group>>(){}.getType());
        if (groups == null) groups = new ArrayList<>();
        
        com.example.appsplitbill.model.Group g = new com.example.appsplitbill.model.Group(String.valueOf(System.currentTimeMillis()), groupName);
        g.setMemberNames(selectedPeople);
        groups.add(0, g);
        
        prefs.edit().putString("user_groups", new Gson().toJson(groups)).apply();
        Toast.makeText(this, "Grup '" + groupName + "' dibuat!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, GroupsActivity.class));
        finish();
    }

    private void loadFriends(ChipGroup cg, TextView tvList) {
        String json = prefs.getString(StorageUtils.getFriendsListKey(this), "[]");
        if (!json.isEmpty()) {
            try {
                List<com.example.appsplitbill.model.Participant> myFriends = new Gson().fromJson(json, new TypeToken<List<com.example.appsplitbill.model.Participant>>() {}.getType());
                if (myFriends != null) {
                    for (com.example.appsplitbill.model.Participant friend : myFriends) {
                        addFriendChip(friend.getName(), cg, tvList);
                    }
                }
            } catch (Exception e) {
                // Fallback for old String list
                List<String> legacy = new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
                if (legacy != null) {
                    for (String name : legacy) addFriendChip(name, cg, tvList);
                }
            }
        }
    }

    private void addFriendChip(String name, ChipGroup cg, TextView tvList) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setCheckable(true);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) addPersonToList(name, tvList);
            else removePersonFromList(name, tvList);
        });
        cg.addView(chip);
    }

    private void addPersonToList(String name, TextView tvList) {
        if (!selectedPeople.contains(name)) {
            selectedPeople.add(name);
            updateListText(tvList);
        }
    }

    private void removePersonFromList(String name, TextView tvList) {
        selectedPeople.remove(name);
        updateListText(tvList);
    }

    private void updateListText(TextView tvList) {
        StringBuilder sb = new StringBuilder();
        for (String p : selectedPeople) sb.append("- ").append(p).append("\n");
        tvList.setText(sb.toString().isEmpty() ? "Belum ada teman dipilih." : sb.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

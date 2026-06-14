package com.example.appsplitbill;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appsplitbill.model.Group;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private RecyclerView rv;
    private List<Group> groupsList = new ArrayList<>();
    private GroupsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        etName = findViewById(R.id.etGroupName);
        rv = findViewById(R.id.rvGroups);
        
        Toolbar toolbar = findViewById(R.id.toolbarGroups);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupsAdapter();
        rv.setAdapter(adapter);

        loadGroups();

        findViewById(R.id.btnAddGroup).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                createNewGroup(name);
            } else {
                Toast.makeText(this, "Masukkan nama grup!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroups() {
        android.content.SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String json = sp.getString("user_groups", "[]");
        groupsList.clear();
        List<Group> loaded = new Gson().fromJson(json, new TypeToken<List<Group>>(){}.getType());
        if (loaded != null) groupsList.addAll(loaded);
        adapter.notifyDataSetChanged();
    }

    private void createNewGroup(String name) {
        Group g = new Group(String.valueOf(System.currentTimeMillis()), name);
        // Start Member Selection for Group
        Intent intent = new Intent(this, SelectPeopleActivity.class);
        intent.putExtra("FOR_GROUP_CREATION", true);
        intent.putExtra("TEMP_GROUP_NAME", name);
        startActivity(intent);
        finish(); // We will save it after returning from selection (actually easier to do it in SelectPeople if flag is set)
    }

    private void deleteGroup(int position) {
        groupsList.remove(position);
        saveGroups();
        adapter.notifyDataSetChanged();
    }

    private void saveGroups() {
        android.content.SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        sp.edit().putString("user_groups", new Gson().toJson(groupsList)).apply();
    }

    private class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Group group = groupsList.get(position);
            holder.tvName.setText(group.getName());
            holder.tvMembers.setText(group.getMemberNames().size() + " Anggota");
            holder.btnDelete.setOnClickListener(v -> deleteGroup(position));
        }

        @Override
        public int getItemCount() { return groupsList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMembers;
            ImageButton btnDelete;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvGroupName);
                tvMembers = v.findViewById(R.id.tvGroupMembers);
                btnDelete = v.findViewById(R.id.btnDeleteGroup);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

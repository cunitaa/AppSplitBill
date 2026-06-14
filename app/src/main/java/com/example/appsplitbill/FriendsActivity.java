package com.example.appsplitbill;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appsplitbill.model.Participant;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private ImageView ivPreview;
    private RecyclerView rv;
    private List<Participant> friendsList = new ArrayList<>();
    private FriendsAdapter adapter;
    private Uri tempImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    tempImageUri = result.getData().getData();
                    if (tempImageUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(tempImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        ivPreview.setImageURI(tempImageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        etName = findViewById(R.id.etFriendName);
        ivPreview = findViewById(R.id.ivFriendAddPreview);
        rv = findViewById(R.id.rvFriends);
        
        Toolbar toolbar = findViewById(R.id.toolbarFriends);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsAdapter();
        rv.setAdapter(adapter);

        loadFriends();

        ivPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        findViewById(R.id.btnAddFriend).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                addFriend(name, tempImageUri != null ? tempImageUri.toString() : null);
            }
        });
    }

    private void loadFriends() {
        android.content.SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String json = sp.getString(StorageUtils.getFriendsListKey(this), "[]");
        friendsList.clear();
        
        try {
            List<Participant> loaded = new Gson().fromJson(json, new TypeToken<List<Participant>>(){}.getType());
            if (loaded != null) friendsList.addAll(loaded);
        } catch (Exception e) {
            // Handle legacy data (List<String>)
            List<String> legacy = new Gson().fromJson(json, new TypeToken<List<String>>(){}.getType());
            if (legacy != null) {
                for (String name : legacy) friendsList.add(new Participant(String.valueOf(System.currentTimeMillis()), name, 0));
            }
        }

        if (friendsList.isEmpty()) {
            friendsList.add(new Participant("1", "Andi", 0));
            friendsList.add(new Participant("2", "Budi", 0));
        }
        adapter.notifyDataSetChanged();
    }

    private void addFriend(String name, String imageUri) {
        Participant p = new Participant(String.valueOf(System.currentTimeMillis()), name, 0);
        p.setImageUri(imageUri);
        friendsList.add(0, p);
        saveFriends();
        adapter.notifyDataSetChanged();
        etName.setText("");
        ivPreview.setImageResource(android.R.drawable.ic_menu_camera);
        tempImageUri = null;
    }

    private void deleteFriend(int position) {
        friendsList.remove(position);
        saveFriends();
        adapter.notifyDataSetChanged();
    }

    private void saveFriends() {
        android.content.SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        sp.edit().putString(StorageUtils.getFriendsListKey(this), new Gson().toJson(friendsList)).apply();
    }

    private class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Participant friend = friendsList.get(position);
            holder.tv.setText(friend.getName());
            if (friend.getImageUri() != null) {
                holder.iv.setImageURI(Uri.parse(friend.getImageUri()));
            } else {
                holder.iv.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
            holder.btnDelete.setOnClickListener(v -> deleteFriend(position));
        }

        @Override
        public int getItemCount() { return friendsList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            ImageView iv;
            ImageButton btnDelete;
            public ViewHolder(@NonNull View v) {
                super(v);
                tv = v.findViewById(R.id.tvFriendName);
                iv = v.findViewById(R.id.ivFriendProfile);
                btnDelete = v.findViewById(R.id.btnDeleteFriend);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

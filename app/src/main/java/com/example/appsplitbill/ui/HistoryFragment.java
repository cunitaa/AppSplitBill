package com.example.appsplitbill.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appsplitbill.R;
import com.example.appsplitbill.model.Bill;
import com.example.appsplitbill.utils.CurrencyFormatter;
import com.example.appsplitbill.utils.StorageUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private List<Bill> historyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        rvHistory = view.findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadHistory();
    }

    private void loadHistory() {
        if (getContext() == null) return;
        android.content.SharedPreferences sp = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String json = sp.getString(StorageUtils.getBillHistoryKey(getContext()), "[]");
        historyList = new Gson().fromJson(json, new TypeToken<List<Bill>>(){}.getType());
        
        rvHistory.setAdapter(new HistoryAdapter(historyList));
    }

    private void deleteHistory(int position) {
        if (getContext() == null) return;
        historyList.remove(position);
        android.content.SharedPreferences sp = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        sp.edit().putString(StorageUtils.getBillHistoryKey(getContext()), new Gson().toJson(historyList)).apply();
        rvHistory.getAdapter().notifyItemRemoved(position);
        rvHistory.getAdapter().notifyItemRangeChanged(position, historyList.size());
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<Bill> list;
        public HistoryAdapter(List<Bill> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Bill bill = list.get(position);
            holder.tvTitle.setText(bill.getTitle());
            holder.tvDate.setText(bill.getDate());
            holder.tvAmount.setText(CurrencyFormatter.formatRupiah(bill.getTotalAmount()));
            
            String status = bill.getStatus();
            holder.tvStatus.setText(status);
            
            // Dynamic styling for status
            if ("LUNAS".equals(status)) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_lunas);
                holder.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.success_green));
            } else {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                holder.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.warning_orange));
            }

            holder.itemView.setOnClickListener(v -> showBillDetails(bill));
            holder.btnDelete.setOnClickListener(v -> deleteHistory(position));
        }

        private void showBillDetails(Bill bill) {
            if (bill.getPeopleResults() == null || bill.getPeopleResults().isEmpty()) return;

            StringBuilder details = new StringBuilder();
            for (int i = 0; i < bill.getPeopleResults().size(); i++) {
                String result = bill.getPeopleResults().get(i);
                boolean isPaid = false;
                if (bill.getPaidStatus() != null && i < bill.getPaidStatus().size()) {
                    isPaid = bill.getPaidStatus().get(i);
                }
                details.append(isPaid ? "✅ " : "⏳ ").append(result).append("\n");
            }

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(bill.getTitle())
                    .setMessage(details.toString())
                    .setPositiveButton("Tutup", null)
                    .setNeutralButton("Update Status Bayar", (dialog, which) -> {
                        togglePaidStatus(bill);
                    })
                    .show();
        }

        private void togglePaidStatus(Bill bill) {
            String[] items = new String[bill.getPeopleResults().size()];
            boolean[] checked = new boolean[bill.getPeopleResults().size()];
            for (int i = 0; i < bill.getPeopleResults().size(); i++) {
                items[i] = bill.getPeopleResults().get(i);
                checked[i] = (bill.getPaidStatus() != null && i < bill.getPaidStatus().size()) ? bill.getPaidStatus().get(i) : false;
            }

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Pilih yang sudah bayar")
                    .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> {
                        if (bill.getPaidStatus() != null && which < bill.getPaidStatus().size()) {
                            bill.getPaidStatus().set(which, isChecked);
                        }
                    })
                    .setPositiveButton("Simpan", (dialog, which) -> {
                        saveHistoryUpdate();
                    })
                    .show();
        }

        private void saveHistoryUpdate() {
            if (getContext() == null) return;
            
            // Check if all participants are paid to update bill status
            for (Bill bill : historyList) {
                if (bill.getPaidStatus() != null && !bill.getPaidStatus().isEmpty()) {
                    boolean allPaid = true;
                    for (Boolean paid : bill.getPaidStatus()) {
                        if (!paid) {
                            allPaid = false;
                            break;
                        }
                    }
                    bill.setStatus(allPaid ? "LUNAS" : "PENDING");
                }
            }

            android.content.SharedPreferences sp = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
            sp.edit().putString(StorageUtils.getBillHistoryKey(getContext()), new Gson().toJson(historyList)).apply();
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvAmount, tvStatus;
            ImageButton btnDelete;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvHistoryTitle);
                tvDate = v.findViewById(R.id.tvHistoryDate);
                tvAmount = v.findViewById(R.id.tvHistoryAmount);
                tvStatus = v.findViewById(R.id.tvHistoryStatus);
                btnDelete = v.findViewById(R.id.btnDeleteHistory);
            }
        }
    }
}

package com.example.studybest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybest.R;
import com.example.studybest.models.ReminderItem;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.VH> {

    private final List<ReminderItem> items;

    public AlertsAdapter(List<ReminderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ReminderItem item = items.get(position);

        holder.tvTitle.setText(item.getSubjectName() + " — " + item.getTaskTitle());

        String time = DateFormat.getDateTimeInstance().format(new Date(item.getRemindAt()));
        holder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
        }
    }
}
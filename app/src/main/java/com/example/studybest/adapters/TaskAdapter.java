package com.example.studybest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybest.R;
import com.example.studybest.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    private final List<Task> items;

    public interface OnTaskCheckedListener {
        void onChecked(Task task, boolean isChecked);
    }

    private OnTaskCheckedListener checkedListener;

    public void setOnTaskCheckedListener(OnTaskCheckedListener l) {
        checkedListener = l;
    }

    public TaskAdapter(List<Task> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Task task = items.get(position);

        holder.cb.setText(task.getTitle());
        holder.cb.setChecked(task.isDone());

        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkedListener != null) {
                checkedListener.onChecked(task, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb;

        VH(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbTask);
        }
    }
}
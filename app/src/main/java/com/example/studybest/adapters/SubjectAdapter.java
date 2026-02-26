package com.example.studybest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybest.R;
import com.example.studybest.models.Subject;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.VH> {

    private final List<Subject> items;

    public interface OnSubjectClickListener { void onClick(Subject subject); }
    public interface OnSubjectLongClickListener { void onLongClick(Subject subject); }

    private OnSubjectClickListener clickListener;
    private OnSubjectLongClickListener longClickListener;

    public void setOnSubjectClickListener(OnSubjectClickListener l) { clickListener = l; }
    public void setOnSubjectLongClickListener(OnSubjectLongClickListener l) { longClickListener = l; }

    public SubjectAdapter(List<Subject> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Subject subject = items.get(position);
        holder.tv.setText(subject.getName());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(subject);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onLongClick(subject);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvSubjectName);
        }
    }
}
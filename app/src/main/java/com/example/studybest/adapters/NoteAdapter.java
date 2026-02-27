package com.example.studybest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybest.R;
import com.example.studybest.models.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {

    private final List<Note> items;

    public interface OnNoteClickListener { void onClick(Note note); }
    public interface OnNoteLongClickListener { void onLongClick(Note note); }

    private OnNoteClickListener clickListener;
    private OnNoteLongClickListener longClickListener;

    public void setOnNoteClickListener(OnNoteClickListener l) { clickListener = l; }
    public void setOnNoteLongClickListener(OnNoteLongClickListener l) { longClickListener = l; }

    public NoteAdapter(List<Note> items) { this.items = items; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Note n = items.get(position);
        holder.tvTitle.setText(n.getTitle());
        holder.tvContent.setText(n.getContent());

        holder.itemView.setOnClickListener(v -> { if (clickListener != null) clickListener.onClick(n); });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onLongClick(n);
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
        }
    }
}
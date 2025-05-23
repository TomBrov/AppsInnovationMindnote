
package com.example.mindnote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<JournalEntry> entries;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(JournalEntry entry);
    }

    public NotesAdapter(List<JournalEntry> entries, OnNoteClickListener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    public void updateEntries(List<JournalEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        JournalEntry entry = entries.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText;
        private TextView moodIcon;
        private TextView noteText;
        private TextView tagsText;
        private ImageView entryImageView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            moodIcon = itemView.findViewById(R.id.moodIcon);  // now TextView
            noteText = itemView.findViewById(R.id.noteText);
            tagsText = itemView.findViewById(R.id.tagsText);
            entryImageView = itemView.findViewById(R.id.entryImageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNoteClick(entries.get(position));
                }
            });
        }

        public void bind(JournalEntry entry) {
            dateText.setText(entry.getShortDate());
            moodIcon.setText(entry.getMoodEmoji());
            noteText.setText(entry.getNote());

            String tags = entry.getTagsAsString();
            if (tags.isEmpty()) {
                tagsText.setVisibility(View.GONE);
            } else {
                tagsText.setVisibility(View.VISIBLE);
                tagsText.setText(tags);
            }

            if (entryImageView != null) {
                String imagePath = entry.getImagePath();

                if (JournalDataManager.isDemoImage(imagePath)) {
                    if (imagePath.equals(JournalDataManager.DEMO_IMAGE_FAMILY)) {
                        entryImageView.setImageResource(R.drawable.family_sunset);
                    } else if (imagePath.equals(JournalDataManager.DEMO_IMAGE_MEDITATION)) {
                        entryImageView.setImageResource(R.drawable.meditation_sunrise);
                    } else if (imagePath.equals(JournalDataManager.DEMO_IMAGE_LIGHTBULB)) {
                        entryImageView.setImageResource(R.drawable.lightbulb);
                    }
                    entryImageView.setVisibility(View.VISIBLE);
                } else if (imagePath != null && !imagePath.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(imagePath)
                            .into(entryImageView);
                    entryImageView.setVisibility(View.VISIBLE);
                } else {
                    entryImageView.setVisibility(View.GONE);
                }
            }
        }
    }
}

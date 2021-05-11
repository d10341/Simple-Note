package com.lx2td.simplenote;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lx2td.simplenote.database.DbHelper;
import com.lx2td.simplenote.models.Note;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.ViewHolder> {

    private DbHelper db;
    private List<Note> fullList, notesList;
    private int colourText, colourBackground;

    NotesListAdapter(int colourText, int colourBackground, DbHelper db) {
        notesList = new ArrayList<>();
        fullList = new ArrayList<>();
        this.colourText = colourText;
        this.colourBackground = colourBackground;
        this.db = db;
    }

    @Override
    public void onBindViewHolder(@NonNull NotesListAdapter.ViewHolder holder, int position) {
        Note note = notesList.get(position);
        String noteName = note.getTitle();
        String noteDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(note.getLastModification());
        String noteTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(note.getLastModification());
        holder.setData(noteName, noteDate, noteTime);
    }

    @NonNull
    @Override
    public NotesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(inflatedView, colourText, colourBackground);
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    void updateList(List<Note> notes, boolean sortAlphabetical) {
        notesList = notes;
        sortList(sortAlphabetical);
        fullList = new ArrayList<>(notesList);
    }

    void filterList(String query) {
        if (TextUtils.isEmpty(query)) {
            DiffUtil.calculateDiff(new NotesDiffCallback(notesList, fullList)).dispatchUpdatesTo(this);
            notesList = new ArrayList<>(fullList);
        } else {
            notesList.clear();
            for (int i = 0; i < fullList.size(); i++) {
                final Note note = fullList.get(i);
                final String noteName = note.getTitle().toLowerCase();
                if (noteName.contains(query)) {
                    notesList.add(fullList.get(i));
                }
            }
            DiffUtil.calculateDiff(new NotesDiffCallback(fullList, notesList)).dispatchUpdatesTo(this);
        }
    }

    void sortList(boolean sortAlphabetical) {
        if (sortAlphabetical) {
            sortAlphabetical(notesList);
        } else {
            sortDate(notesList);
        }
        DiffUtil.calculateDiff(new NotesDiffCallback(fullList, notesList)).dispatchUpdatesTo(this);
        fullList = new ArrayList<>(notesList);
    }

    private void sortAlphabetical(List<Note> notes) {
        Collections.sort(notes, new Comparator<Note>() {
            public int compare(Note f1, Note f2) {
                return (f1.getTitle().compareTo(f2.getTitle()));
            }
        });
    }

    private void sortDate(List<Note> notes) {
        Collections.sort(notes, new Comparator<Note>() {
            public int compare(Note f1, Note f2) {
                return Long.compare(f2.getLastModification(), f1.getLastModification());
            }
        });
    }

    void deleteNote(int position) {
        Note note = notesList.get(position);
        fullList.remove(note);
        notesList.remove(note);
        notifyItemRemoved(position);
        db.deleteNote(note);
    }

    void cancelDelete(int position) {
        notifyItemChanged(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView noteTitle, noteDate, noteTime;
        private String stringTitle;
        ConstraintLayout constraintLayout;

        ViewHolder(View view, int colourText, int colourBackground) {
            super(view);
            noteTitle = view.findViewById(R.id.tv_title);
            noteDate = view.findViewById(R.id.tv_date);
            noteTime = view.findViewById(R.id.tv_time);
            noteTitle.setTextColor(colourText);
            noteDate.setTextColor(colourText);
            noteTime.setTextColor(colourText);
            constraintLayout = view.findViewById(R.id.layout_constraint);
            constraintLayout.setBackgroundColor(colourBackground);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemView.getContext().startActivity(NoteActivity.getStartIntent(itemView.getContext(), stringTitle));
        }

        void setData(String title, String date, String time) {
            stringTitle = title;
            noteTitle.setText(title);
            noteDate.setText(date);
            noteTime.setText(time);
        }
    }

}

package com.lx2td.simplenote;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.lx2td.simplenote.models.Note;

import java.util.List;

public class NotesDiffCallback extends DiffUtil.Callback {

    private List<Note> oldList;
    private List<Note> newList;

    public NotesDiffCallback(List<Note> oldList, List<Note> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getTitle().equals(newList.get(newItemPosition).getTitle());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getContent().equals(newList.get(newItemPosition).getContent());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
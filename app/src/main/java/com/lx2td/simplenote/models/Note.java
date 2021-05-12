package com.lx2td.simplenote.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.lx2td.simplenote.utils.EqualityChecker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Note implements Serializable, Parcelable {
    private String title;
    private String content;
    private Long creation;
    private Long lastModification;
    private String alarm;
    private Boolean reminderFired;
    private String recurrenceRule;
    private Double latitude;
    private Double longitude;
    private String address;
    private List<? extends Attachment> attachmentsList = new ArrayList();
    private transient List<Attachment> attachmentsListOld = new ArrayList();

    public Note() {
        this.title = "";
        this.content = "";
    }

    public Note(Long creation, Long lastModification, String title, String content, String alarm, Integer reminderFired, String recurrenceRule, String latitude, String longitude, Integer checklist) {
        this.title = title;
        this.content = content;
        this.creation = creation;
        this.lastModification = lastModification;
        this.alarm = alarm;
        this.reminderFired = reminderFired == 1;
        this.recurrenceRule = recurrenceRule;
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public Note(Note note) {
        this.buildFromNote(note);
    }

    protected Note(Parcel in) {
        title = in.readString();
        content = in.readString();
        if (in.readByte() == 0) {
            creation = null;
        } else {
            creation = in.readLong();
        }
        if (in.readByte() == 0) {
            lastModification = null;
        } else {
            lastModification = in.readLong();
        }
        alarm = in.readString();
        byte tmpReminderFired = in.readByte();
        reminderFired = tmpReminderFired == 0 ? null : tmpReminderFired == 1;
        recurrenceRule = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        address = in.readString();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    private void buildFromNote(Note note) {
        this.setTitle(note.getTitle());
        this.setContent(note.getContent());
        this.setCreation(note.getCreation());
        this.setLastModification(note.getLastModification());
        this.setAlarm(note.getAlarm());
        this.setRecurrenceRule(note.getRecurrenceRule());
        this.setReminderFired(note.isReminderFired());
        this.setLatitude(note.getLatitude());
        this.setLongitude(note.getLongitude());
        this.setAddress(note.getAddress());
        ArrayList<Attachment> list = new ArrayList();
        Iterator i$ = note.getAttachmentsList().iterator();

        while(i$.hasNext()) {
            Attachment mAttachment = (Attachment)i$.next();
            list.add(mAttachment);
        }

        this.setAttachmentsList(list);
    }

    public void buildFromJson(String jsonNote) {
        Gson gson = new Gson();
        Note noteFromJson = (Note)gson.fromJson(jsonNote, this.getClass());
        this.buildFromNote(noteFromJson);
    }

    public void set_id(Long _id) {
        this.creation = _id;
    }

    public Long get_id() {
        return this.creation;
    }

    public String getTitle() {
        return this.title == null ? "" : this.title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getContent() {
        return this.content == null ? "" : this.content;
    }

    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }

    public Long getCreation() {
        return this.creation;
    }

    public void setCreation(Long creation) {
        this.creation = creation;
    }

    public void setCreation(String creation) {
        Long creationLong;
        try {
            creationLong = Long.parseLong(creation);
        } catch (NumberFormatException var4) {
            creationLong = null;
        }

        this.creation = creationLong;
    }

    public Long getLastModification() {
        return this.lastModification;
    }

    public void setLastModification(Long lastModification) {
        this.lastModification = lastModification;
    }

    public void setLastModification(String lastModification) {
        Long lastModificationLong;
        try {
            lastModificationLong = Long.parseLong(lastModification);
        } catch (NumberFormatException var4) {
            lastModificationLong = null;
        }

        this.lastModification = lastModificationLong;
    }

    public String getAlarm() {
        return this.alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public void setAlarm(long alarm) {
        this.alarm = String.valueOf(alarm);
    }

    public Boolean isReminderFired() {
        return this.reminderFired != null && this.reminderFired;
    }

    public void setReminderFired(Boolean reminderFired) {
        this.reminderFired = reminderFired;
    }

    public void setReminderFired(int reminderFired) {
        this.reminderFired = reminderFired == 1;
    }

    public String getRecurrenceRule() {
        return this.recurrenceRule;
    }

    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLatitude(String latitude) {
        try {
            this.setLatitude(Double.parseDouble(latitude));
        } catch (NullPointerException | NumberFormatException var3) {
            this.latitude = null;
        }

    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLongitude(String longitude) {
        try {
            this.setLongitude(Double.parseDouble(longitude));
        } catch (NumberFormatException var3) {
            this.longitude = null;
        } catch (NullPointerException var4) {
            this.longitude = null;
        }

    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<? extends Attachment> getAttachmentsList() {
        return this.attachmentsList;
    }

    public void setAttachmentsList(List<? extends Attachment> attachmentsList) {
        this.attachmentsList = attachmentsList;
    }

    public void backupAttachmentsList() {
        List<Attachment> attachmentsListOld = new ArrayList();
        Iterator i$ = this.getAttachmentsList().iterator();

        while(i$.hasNext()) {
            Attachment mAttachment = (Attachment)i$.next();
            attachmentsListOld.add(mAttachment);
        }

        this.attachmentsListOld = attachmentsListOld;
    }

    public List<Attachment> getAttachmentsListOld() {
        return this.attachmentsListOld;
    }

    public void setAttachmentsListOld(List<Attachment> attachmentsListOld) {
        this.attachmentsListOld = attachmentsListOld;
    }

    public boolean equals(Object o) {
        boolean res = false;

        Note note;
        try {
            note = (Note)o;
        } catch (Exception var6) {
            return res;
        }

        Object[] a = new Object[]{this.getTitle(), this.getContent(), this.getCreation(), this.getLastModification(), this.getAlarm(), this.getRecurrenceRule(), this.getLatitude(), this.getLongitude(), this.getAddress()};
        Object[] b = new Object[]{note.getTitle(), note.getContent(), note.getCreation(), note.getLastModification(), note.getAlarm(), note.getRecurrenceRule(), note.getLatitude(), note.getLongitude(), note.getAddress()};
        if (EqualityChecker.check(a, b)) {
            res = true;
        }

        return res;
    }

    public boolean isChanged(Note note) {
        return !this.equals(note) || !this.getAttachmentsList().equals(note.getAttachmentsList());
    }

    public boolean isEmpty() {
        Note emptyNote = new Note();
        emptyNote.setCreation(this.getCreation());
        return !this.isChanged(emptyNote);
    }

    public String toString() {
        return this.getTitle();
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void addAttachment(Attachment attachment) {
        List<Attachment> attachmentsList = ((List<Attachment>) getAttachmentsList());
        attachmentsList.add(attachment);
        setAttachmentsList(attachmentsList);
    }

    public void removeAttachment(Attachment attachment) {
        List<Attachment> attachmentsList = ((List<Attachment>) getAttachmentsList());
        attachmentsList.remove(attachment);
        setAttachmentsList(attachmentsList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(String.valueOf(getCreation()));
        dest.writeString(String.valueOf(getLastModification()));
        dest.writeString(getTitle());
        dest.writeString(getContent());
        dest.writeString(getAlarm());
        dest.writeInt(isReminderFired() ? 1 : 0);
        dest.writeString(getRecurrenceRule());
        dest.writeString(String.valueOf(getLatitude()));
        dest.writeString(String.valueOf(getLongitude()));
        dest.writeString(getAddress());
        dest.writeList(getAttachmentsList());
    }
}

package com.lx2td.simplenote.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.lx2td.simplenote.models.Attachment;
import com.lx2td.simplenote.models.Note;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class DbHelper extends SQLiteOpenHelper {
    // Database name
    private static final String DATABASE_NAME = "simplenote";

    // Database version aligned if possible to software version
    private static final int DATABASE_VERSION = 1;
    // Sql query file directory
    private static final String SQL_DIR = "sql";

    // Notes table name
    public static final String TABLE_NOTES = "notes";
    // Notes table columns
    public static final String KEY_ID = "creation";
    public static final String KEY_CREATION = "creation";
    public static final String KEY_LAST_MODIFICATION = "last_modification";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_REMINDER = "alarm";
    public static final String KEY_REMINDER_FIRED = "reminder_fired";
    public static final String KEY_RECURRENCE_RULE = "recurrence_rule";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ADDRESS = "address";

    // Attachments table name
    public static final String TABLE_ATTACHMENTS = "attachments";
    // Attachments table columns
    public static final String KEY_ATTACHMENT_ID = "attachment_id";
    public static final String KEY_ATTACHMENT_URI = "uri";
    public static final String KEY_ATTACHMENT_NAME = "name";
    public static final String KEY_ATTACHMENT_SIZE = "size";
    public static final String KEY_ATTACHMENT_LENGTH = "length";
    public static final String KEY_ATTACHMENT_MIME_TYPE = "mime_type";
    public static final String KEY_ATTACHMENT_NOTE_ID = "note_id";

    // Queries
    private static final String CREATE_QUERY = "create.sql";
    private static final String UPGRADE_QUERY_PREFIX = "upgrade-";
    private static final String UPGRADE_QUERY_SUFFIX = ".sql";


    private final Context mContext;

    private static DbHelper instance = null;
    private SQLiteDatabase db;


    public static synchronized DbHelper getInstance() {
        return getInstance();
    }


    public static synchronized DbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbHelper(context);
        }
        return instance;
    }


    public DbHelper(Context mContext) {
        super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = mContext;
    }


    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    public SQLiteDatabase getDatabase() {
        return getDatabase(false);
    }

    public SQLiteDatabase getDatabase(boolean forceWritable) {
        try {
            return forceWritable ? getWritableDatabase() : getReadableDatabase();
        } catch (IllegalStateException e) {
            return this.db;
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.disableWriteAheadLogging();
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            execSqlFile(CREATE_QUERY, db);
        } catch (IOException e) {
            throw new RuntimeException("Database creation failed: " + e.getMessage(), e);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.db = db;

        try {
            String[] files = mContext.getAssets().list(SQL_DIR);
            Arrays.sort(files);
            for (String sqlFile : files) {
                if (sqlFile.startsWith(UPGRADE_QUERY_PREFIX)) {
                    int fileVersion = Integer.parseInt(sqlFile.substring(UPGRADE_QUERY_PREFIX.length(),
                            sqlFile.length() - UPGRADE_QUERY_SUFFIX.length()));
                    if (fileVersion > oldVersion && fileVersion <= newVersion) {
                        execSqlFile(sqlFile, db);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Database upgrade failed", e);
        }
    }


    public Note updateNote(Note note, boolean updateLastModification) {
        db = getDatabase(true);

        String content = note.getContent();

        // To ensure note and attachments insertions are atomic and boost performances transaction are used
        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_CONTENT, content);
        values.put(KEY_CREATION,
                note.getCreation() != null ? note.getCreation() : Calendar.getInstance().getTimeInMillis());
        long lastModification = note.getLastModification() != null && !updateLastModification
                ? note.getLastModification()
                : Calendar.getInstance().getTimeInMillis();
        values.put(KEY_LAST_MODIFICATION, lastModification);
        values.put(KEY_REMINDER, note.getAlarm());
        values.put(KEY_REMINDER_FIRED, note.isReminderFired());
        values.put(KEY_RECURRENCE_RULE, note.getRecurrenceRule());
        values.put(KEY_LATITUDE, note.getLatitude());
        values.put(KEY_LONGITUDE, note.getLongitude());
        values.put(KEY_ADDRESS, note.getAddress());

        db.insertWithOnConflict(TABLE_NOTES, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);

        // Updating attachments
        List<Attachment> deletedAttachments = note.getAttachmentsListOld();
        for (Attachment attachment : note.getAttachmentsList()) {
            updateAttachment(note.get_id() != null ? note.get_id() : values.getAsLong(KEY_CREATION),
                    attachment, db);
            deletedAttachments.remove(attachment);
        }
        // Remove from database deleted attachments
        for (Attachment attachmentDeleted : deletedAttachments) {
            db.delete(TABLE_ATTACHMENTS, KEY_ATTACHMENT_ID + " = ?",
                    new String[]{String.valueOf(attachmentDeleted.getId())});
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        // Fill the note with correct data before returning it
        note.setCreation(
                note.getCreation() != null ? note.getCreation() : values.getAsLong(KEY_CREATION));
        note.setLastModification(values.getAsLong(KEY_LAST_MODIFICATION));

        return note;
    }


    private void execSqlFile(String sqlFile, SQLiteDatabase db) throws SQLException, IOException {
        for (String sqlInstruction : SqlParser
                .parseSqlFile(SQL_DIR + "/" + sqlFile, mContext.getAssets())) {
            try {
                db.execSQL(sqlInstruction);
            } catch (Exception e) {
                Log.e("error","Error executing command: " + sqlInstruction);
            }
        }
    }


    /**
     * Attachments update
     */
    public Attachment updateAttachment(Attachment attachment) {
        return updateAttachment(-1, attachment, getDatabase(true));
    }


    /**
     * New attachment insertion
     */
    public Attachment updateAttachment(long noteId, Attachment attachment, SQLiteDatabase db) {
        ContentValues valuesAttachments = new ContentValues();
        valuesAttachments
                .put(KEY_ATTACHMENT_ID, attachment.getId() != null ? attachment.getId() : Calendar
                        .getInstance().getTimeInMillis());
        valuesAttachments.put(KEY_ATTACHMENT_NOTE_ID, noteId);
        valuesAttachments.put(KEY_ATTACHMENT_URI, attachment.getUriPath().toString());
        valuesAttachments.put(KEY_ATTACHMENT_MIME_TYPE, attachment.getMime_type());
        valuesAttachments.put(KEY_ATTACHMENT_NAME, attachment.getName());
        valuesAttachments.put(KEY_ATTACHMENT_SIZE, attachment.getSize());
        valuesAttachments.put(KEY_ATTACHMENT_LENGTH, attachment.getLength());
        db.insertWithOnConflict(TABLE_ATTACHMENTS, KEY_ATTACHMENT_ID, valuesAttachments,
                SQLiteDatabase.CONFLICT_REPLACE);
        return attachment;
    }


    /**
     * Getting single note
     */
    public Note getNote(long id) {
        List<Note> notes = getNotes(" WHERE " + KEY_ID + " = " + id, true);
        return notes.isEmpty() ? null : notes.get(0);
    }


    /**
     * Getting All notes
     *
     * @return Notes list
     */
    public List<Note> getAllNotes() {
        String whereCondition = "";
        return getNotes(whereCondition, true);
    }


    /**
     * Common method for notes retrieval. It accepts a query to perform and returns matching records.
     */
    public List<Note> getNotes(String whereCondition, boolean order) {
        List<Note> noteList = new ArrayList<>();

        String sortColumn = "";
        String sortOrder = "";

        if (order) {
            sortOrder =
                    KEY_TITLE.equals(sortColumn) || KEY_REMINDER.equals(sortColumn) ? " ASC " : " DESC ";
        }

        // In case of title sorting criteria it must be handled empty title by concatenating content
        sortColumn = KEY_TITLE.equals(sortColumn) ? KEY_TITLE + "||" + KEY_CONTENT : sortColumn;

        // In case of reminder sorting criteria the empty reminder notes must be moved on bottom of results
        sortColumn = KEY_REMINDER.equals(sortColumn) ? "IFNULL(" + KEY_REMINDER + ", " +
                "" + 0 + ")" : sortColumn;

        // Generic query to be specialized with conditions passed as parameter
        String query = "SELECT "
                + KEY_CREATION + ","
                + KEY_LAST_MODIFICATION + ","
                + KEY_TITLE + ","
                + KEY_CONTENT + ","
                + KEY_REMINDER + ","
                + KEY_REMINDER_FIRED + ","
                + KEY_RECURRENCE_RULE + ","
                + KEY_LATITUDE + ","
                + KEY_LONGITUDE + ","
                + KEY_ADDRESS
                + " FROM " + TABLE_NOTES + " "
                + whereCondition;
                //+ (order ? " ORDER BY " + sortColumn + " COLLATE NOCASE " + sortOrder : "");


        try (Cursor cursor = getDatabase().rawQuery(query, null)) {

            if (cursor.moveToFirst()) {
                do {
                    int i = 0;
                    Note note = new Note();
                    note.setCreation(cursor.getLong(i++));
                    note.setLastModification(cursor.getLong(i++));
                    note.setTitle(cursor.getString(i++));
                    note.setContent(cursor.getString(i++));
                    note.setAlarm(cursor.getString(i++));
                    note.setReminderFired(cursor.getInt(i++));
                    note.setRecurrenceRule(cursor.getString(i++));
                    note.setLatitude(cursor.getString(i++));
                    note.setLongitude(cursor.getString(i++));
                    note.setAddress(cursor.getString(i++));
                    // Add eventual attachments uri
                    note.setAttachmentsList(getNoteAttachments(note));

                    // Adding note to list
                    noteList.add(note);

                } while (cursor.moveToNext());
            }

        }
        return noteList;
    }


    /**
     * Deleting single note
     */
    public boolean deleteNote(Note note) {
        return deleteNote(note, false);
    }


    /**
     * Deleting single note, eventually keeping attachments
     */
    public boolean deleteNote(Note note, boolean keepAttachments) {
        return deleteNote(note.get_id(), keepAttachments);
    }


    /**
     * Deleting single note by its ID
     */
    public boolean deleteNote(long noteId, boolean keepAttachments) {
        SQLiteDatabase db = getDatabase(true);
        db.delete(TABLE_NOTES, KEY_ID + " = ?", new String[]{String.valueOf(noteId)});
        if (!keepAttachments) {
            db.delete(TABLE_ATTACHMENTS, KEY_ATTACHMENT_NOTE_ID + " = ?",
                    new String[]{String.valueOf(noteId)});
        }
        return true;
    }

    /**
     * Retrieves all attachments related to specific note
     */
    public ArrayList<Attachment> getNoteAttachments(Note note) {
        String whereCondition = " WHERE " + KEY_ATTACHMENT_NOTE_ID + " = " + note.get_id();
        return getAttachments(whereCondition);
    }


    /**
     * Retrieves all attachments
     */
    public ArrayList<Attachment> getAllAttachments() {
        return getAttachments("");
    }


    /**
     * Retrieves attachments using a condition passed as parameter
     *
     * @return List of attachments
     */
    public ArrayList<Attachment> getAttachments(String whereCondition) {

        ArrayList<Attachment> attachmentsList = new ArrayList<>();
        String sql = "SELECT "
                + KEY_ATTACHMENT_ID + ","
                + KEY_ATTACHMENT_URI + ","
                + KEY_ATTACHMENT_NAME + ","
                + KEY_ATTACHMENT_SIZE + ","
                + KEY_ATTACHMENT_LENGTH + ","
                + KEY_ATTACHMENT_MIME_TYPE
                + " FROM " + TABLE_ATTACHMENTS
                + whereCondition;
        SQLiteDatabase db;
        Cursor cursor = null;

        try {

            cursor = getDatabase().rawQuery(sql, null);

            // Looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                Attachment mAttachment;
                do {
                    mAttachment = new Attachment(cursor.getLong(0),
                            cursor.getString(1), cursor.getString(2), cursor.getInt(3),
                            (long) cursor.getInt(4), cursor.getString(5));
                    attachmentsList.add(mAttachment);
                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return attachmentsList;
    }



    public void setReminderFired(long noteId, boolean fired) {
        ContentValues values = new ContentValues();
        values.put(KEY_REMINDER_FIRED, fired);
        getDatabase(true)
                .update(TABLE_NOTES, values, KEY_ID + " = ?", new String[]{String.valueOf(noteId)});
    }

}

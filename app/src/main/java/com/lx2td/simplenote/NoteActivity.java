package com.lx2td.simplenote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import 	androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lx2td.simplenote.database.DbHelper;
import com.lx2td.simplenote.models.Attachment;
import com.lx2td.simplenote.models.Note;
import com.lx2td.simplenote.utils.ConnectionManager;
import com.lx2td.simplenote.helper.FileProviderHelper;
import com.lx2td.simplenote.utils.GoogleLocation;
import com.lx2td.simplenote.helper.HelperUtils;
import com.lx2td.simplenote.helper.PermissionsHelper;
import com.lx2td.simplenote.helper.StorageHelper;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.lx2td.simplenote.utils.Constants.MIME_TYPE_IMAGE_EXT;
import static com.lx2td.simplenote.utils.Constants.MIME_TYPE_VIDEO_EXT;
import static com.lx2td.simplenote.utils.Constants.MIME_TYPE_AUDIO_EXT;
import static com.lx2td.simplenote.utils.Constants.MIME_TYPE_AUDIO;
import static com.lx2td.simplenote.utils.Constants.THUMBNAIL_SIZE;
import static java.lang.Long.parseLong;

public class NoteActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE_TITLE = "EXTRA_NOTE_TITLE";

    private static final int TAKE_PHOTO = 1;
    private static final int TAKE_VIDEO = 2;
    private static final int DETAIL = 3;
    private static final int FILES = 4;

    private boolean colourNavbar;
    private String title, content;
    private EditText noteText, titleText;
    private AlertDialog dialog;
    private MaterialDialog attachmentDialog;
    private NoteActivity mainActivity;
    private Note note;
    private static Context context;
    private AttachmentAdapter mAttachmentAdapter;

    // Audio recording
    private String recordName;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean isRecording = false;
    private View isPlayingView = null;
    private Bitmap recordingBitmap;
    private Uri attachmentUri;
    private long audioRecordingTimeStart;
    private long audioRecordingTime;

    private EditText detailTitle, detailContent;
    private TextView creation, lastModification;

    private @ColorInt
    int colourPrimary, colourFont, colourBackground;

    public static Intent getStartIntent(Context context, String title) {
        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra(EXTRA_NOTE_TITLE, title);
        return intent;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        titleText = findViewById(R.id.detail_title);
        noteText = findViewById(R.id.detail_content);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        init();

        // If activity started from a share intent
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                noteText.setText(sharedText);
                content = sharedText;
                title = "";
            }
        } else { // If activity started from the notes list
            title = intent.getStringExtra(EXTRA_NOTE_TITLE);
            if (title == null || TextUtils.isEmpty(title)) {
                title = "";
                content = "";
                noteText.requestFocus();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(getString(R.string.new_note));
            } else {
                titleText.setText(title);
//                content = ;
//                noteText.setText(content);
                initViews();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(title);
            }
        }

        getSettings(PreferenceManager.getDefaultSharedPreferences(NoteActivity.this));
        applySettings();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        content = noteText.getText().toString().trim();
        if (getCurrentFocus() != null)
            getCurrentFocus().clearFocus();
    }

    @Override
    public void onPause() {
        if (!isChangingConfigurations()) {
            note.setTitle(titleText.getText().toString());
            note.setContent(noteText.getText().toString());
            DbHelper.getInstance().updateNote(note, true);
        }
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = null;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_attachment:
                final View layout = LayoutInflater.from(this).inflate(R.layout.attachment_dialog, null);

                attachmentDialog = new MaterialDialog.Builder(this)
                        .autoDismiss(false)
                        .customView(layout, false)
                        .build();
                attachmentDialog.show();

                // Camera
                android.widget.TextView cameraSelection = layout.findViewById(R.id.camera);
                cameraSelection.setOnClickListener(new AttachmentOnClickListener());
                // Audio recording
                android.widget.TextView recordingSelection = layout.findViewById(R.id.recording);
                toggleAudioRecordingStop(recordingSelection);
                recordingSelection.setOnClickListener(new AttachmentOnClickListener());
                // Video recording
                android.widget.TextView videoSelection = layout.findViewById(R.id.video);
                videoSelection.setOnClickListener(new AttachmentOnClickListener());
                // Files
                android.widget.TextView filesSelection = layout.findViewById(R.id.files);
                filesSelection.setOnClickListener(new AttachmentOnClickListener());
                // Location
                android.widget.TextView locationSelection = layout.findViewById(R.id.location);
                locationSelection.setOnClickListener(new AttachmentOnClickListener());
                // Time
                android.widget.TextView timeStampSelection = layout.findViewById(R.id.timestamp);
                timeStampSelection.setOnClickListener(new AttachmentOnClickListener());
                return(true);

            case R.id.btn_undo:
                noteText.setText(content);
                noteText.setSelection(noteText.getText().length());
                return (true);

            case R.id.btn_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, noteText.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_to)));
                return (true);

            case R.id.btn_delete:
                dialog = new AlertDialog.Builder(NoteActivity.this, R.style.AlertDialogTheme)
                        .setTitle(getString(R.string.confirm_delete))
                        .setMessage(getString(R.string.confirm_delete_text))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DbHelper.getInstance().deleteNote(note);
                                title = "";
                                content = "";
                                titleText.setText(title);
                                noteText.setText(content);
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete_white_24dp))
                        .show();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().getDecorView().setBackgroundColor(colourPrimary);
                }
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    public void startGetContentAction() {
        Intent filesIntent;
        filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
        filesIntent.setType("*/*");
        startActivityForResult(filesIntent, FILES);
    }

    private void init() {
        creation = (TextView)findViewById(R.id.creation);
        lastModification = (TextView)findViewById(R.id.creation);
        detailTitle = ((EditText)findViewById(R.id.detail_title));
        detailContent = ((EditText)findViewById(R.id.detail_content));
    }

    @SuppressLint("NewApi")
    private void initViews() {

        initViewTitle();

        initViewContent();

        initViewAttachments();

//        initViewReminder();
//
       initViewFooter();
    }

    private void initViewFooter() {
        String m = String.valueOf(note.getCreation() / 1000 / 60);
        String s = String.format("%02d", (note.getCreation() / 1000) % 60);
        String creationStr = m + ":" + s;
        creation.append(creation.length() > 0 ? "Created: " + creationStr : "");
        if (creation.getText().length() == 0) {
            creation.setVisibility(View.GONE);
        }

        m = String.valueOf(note.getLastModification() / 1000 / 60);
        s = String.format("%02d", (note.getLastModification() / 1000) % 60);
        String lastModificationStr = m + ":" + s;
        lastModification.append(lastModification.length() > 0 ? "Updated: " + lastModificationStr : "");
        if (lastModification.getText().length() == 0) {
            lastModification.setVisibility(View.GONE);
        }
    }

    private void initViewTitle() {
        detailTitle.setText(note.getTitle());
        //When editor action is pressed focus is moved to last character in content field
        detailTitle.setOnEditorActionListener((v, actionId, event) -> {
            EditText detailContent = ((EditText)findViewById(R.id.detail_content));
            detailContent.requestFocus();
            detailContent.setSelection(detailContent.getText().length());
            return false;
        });
    }

    private void initViewContent() {
        detailContent.setText(note.getContent());
        // Avoids focused line goes under the keyboard
        detailContent.addTextChangedListener((TextWatcher) this);
    }

    private void initViewAttachments() {

    }


    /**
     * Audio recordings playback
     */
    private void playback(View v, Uri uri) {
        // Some recording is playing right now
        if (mPlayer != null && mPlayer.isPlaying()) {
            if (isPlayingView != v) {
                // If the audio actually played is NOT the one from the click view the last one is played
                stopPlaying();
                isPlayingView = v;
                startPlaying(uri);
                replacePlayingAudioBitmap(v);
            } else {
                // Otherwise just stops playing
                stopPlaying();
            }
        } else {
            // If nothing is playing audio just plays
            isPlayingView = v;
            startPlaying(uri);
            replacePlayingAudioBitmap(v);
        }
    }

    private void replacePlayingAudioBitmap(View v) {
        Drawable d = ((ImageView) v.findViewById(R.id.gridview_item_picture)).getDrawable();
        if (BitmapDrawable.class.isAssignableFrom(d.getClass())) {
            recordingBitmap = ((BitmapDrawable) d).getBitmap();
        } else {
            recordingBitmap = ((BitmapDrawable) d.getCurrent()).getBitmap();
        }
        ((ImageView) v.findViewById(R.id.gridview_item_picture)).setImageBitmap(ThumbnailUtils
                .extractThumbnail(BitmapFactory.decodeResource(mainActivity.getResources(),
                        R.drawable.stop), THUMBNAIL_SIZE, THUMBNAIL_SIZE));
    }

    private void startPlaying(Uri uri) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        try {
            mPlayer.setDataSource(mainActivity, uri);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(mp -> {
                mPlayer = null;
                if (isPlayingView != null) {
                    ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap
                            (recordingBitmap);
                    recordingBitmap = null;
                    isPlayingView = null;
                }
            });
        } catch (IOException e) {

        }
    }

    private void stopPlaying() {
        if (mPlayer != null) {
            if (isPlayingView != null) {
                ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture))
                        .setImageBitmap(recordingBitmap);
            }
            isPlayingView = null;
            recordingBitmap = null;
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void startRecording(View v) {
        PermissionsHelper.requestPermission(NoteActivity.this, Manifest.permission.RECORD_AUDIO,
                1, findViewById(R.id.snackbar_placeholder), () -> {

                    isRecording = true;
                    toggleAudioRecordingStop(v);

                    File f = StorageHelper.createNewAttachmentFile(mainActivity, MIME_TYPE_AUDIO_EXT);
                    if (mRecorder == null) {
                        mRecorder = new MediaRecorder();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        mRecorder.setAudioEncodingBitRate(96000);
                        mRecorder.setAudioSamplingRate(44100);
                    }
                    recordName = f.getAbsolutePath();
                    mRecorder.setOutputFile(recordName);

                    try {
                        audioRecordingTimeStart = Calendar.getInstance().getTimeInMillis();
                        mRecorder.prepare();
                        mRecorder.start();
                    } catch (IOException | IllegalStateException e) {

                    }
                });
    }

    private void toggleAudioRecordingStop(View v) {
        if (isRecording) {
            ((android.widget.TextView) v).setText("Stop");
            ((android.widget.TextView) v).setTextColor(Color.parseColor("#ff0000"));
        }
    }

    private void stopRecording() {
        isRecording = false;
        if (mRecorder != null) {
            mRecorder.stop();
            audioRecordingTime = Calendar.getInstance().getTimeInMillis() - audioRecordingTimeStart;
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void getSettings(SharedPreferences preferences) {
        colourPrimary = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_PRIMARY, ContextCompat.getColor(NoteActivity.this, R.color.colorPrimary));
        colourFont = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK);
        colourBackground = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_BACKGROUND, Color.WHITE);
        colourNavbar = preferences.getBoolean(HelperUtils.PREFERENCE_COLOUR_NAVBAR, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void applySettings() {
        HelperUtils.applyColours(NoteActivity.this, colourPrimary, colourNavbar);

        // Set text field underline colour
        noteText.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));
        titleText.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));

        // Set actionbar and background colour
        findViewById(R.id.detail_root).setBackgroundColor(colourBackground);
        if (getSupportActionBar() != null)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colourPrimary));

        // Set font colours
        titleText.setTextColor(colourFont);
        noteText.setTextColor(colourFont);

        // Set hint colours
        titleText.setHintTextColor(ColorUtils.setAlphaComponent(colourFont, 120));
        noteText.setHintTextColor(ColorUtils.setAlphaComponent(colourFont, 120));
    }

    private void saveFile() {
        // Get current title and note
        String newTitle = titleText.getText().toString().trim().replace("/", " ");
        String newNote = noteText.getText().toString().trim();

        // Check if title and note are empty
        if (TextUtils.isEmpty(newTitle) && TextUtils.isEmpty(newNote)) {
            return;
        }

        // Check if title and note are unchanged
        if (newTitle.equals(title) && newNote.equals(note)) {
            return;
        }

        // Get file name to be saved if the title has changed or if it is empty
        if (!title.equals(newTitle) || TextUtils.isEmpty(newTitle)) {
            newTitle = newFileName(newTitle);
            titleText.setText(newTitle);
        }

        // Save the file with the new file name and content
        HelperUtils.writeFile(NoteActivity.this, newTitle, newNote);

        // If the title is not empty and the file name has changed then delete the old file
        if (!TextUtils.isEmpty(title) && !newTitle.equals(title)) {
            deleteFile(title + HelperUtils.TEXT_FILE_EXTENSION);
        }

        // Set the title to be the new saved title for when the home button is pressed
        title = newTitle;

    }

    private String newFileName(String name) {
        // If it is empty, give it a default title of "Note"
        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.note);
        }
        // If the name already exists, append a number to it
        if (HelperUtils.fileExists(NoteActivity.this, name)) {
            int i = 1;
            while (true) {
                if (!HelperUtils.fileExists(NoteActivity.this, name + " (" + i + ")") || title.equals(name + " (" + i + ")")) {
                    name = (name + " (" + i + ")");
                    break;
                }
                i++;
            }
        }
        return name;
    }

    private void addAttachment(Attachment attachment) {
        note.addAttachment(attachment);
    }

    private void takePhoto() {
        // Checks for camera app available
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Checks for created file validity
        File f = StorageHelper.createNewAttachmentFile(mainActivity, MIME_TYPE_IMAGE_EXT);
        attachmentUri = FileProviderHelper.getFileProvider(f);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, attachmentUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void takeVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // File is stored in custom ON folder to speedup the attachment
        File f = StorageHelper.createNewAttachmentFile(mainActivity, MIME_TYPE_VIDEO_EXT);
        attachmentUri = FileProviderHelper.getFileProvider(f);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, attachmentUri);
        String maxVideoSizeStr = "70";
        long maxVideoSize = parseLong(maxVideoSizeStr) * 1024L * 1024L;
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
        startActivityForResult(takeVideoIntent, TAKE_VIDEO);
    }

    private void addTimestamp() {
        Editable editable = (Editable) ((EditText)findViewById(R.id.detail_content)).getText();
        int position = ((EditText)findViewById(R.id.detail_content)).getSelectionStart();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String dateStamp = dateFormat.format(new Date().getTime()) + " ";
        String leadSpace = position == 0 ? "" : " ";
        dateStamp = leadSpace + dateStamp;
        editable.insert(position, dateStamp);
        Selection.setSelection(editable, position + dateStamp.length());
    }

    public static Context getContext() {
        return context;
    }

    private void displayLocation() {
        if (!ConnectionManager.internetAvailable(context)) {
            Location location = GoogleLocation.getLocation();
            note.setLatitude(location.getLatitude());
            note.setLongitude(location.getLongitude());
            return;
        }
    }

    private void askReadExternalStoragePermission() {
        PermissionsHelper.requestPermission(NoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE,
                0,
                findViewById(R.id.snackbar_placeholder), this::startGetContentAction);
    }

    /**
     * Manages clicks on attachment dialog
     */
    @SuppressLint("InlinedApi")
    private class AttachmentOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                // Photo from camera
                case R.id.camera:
                    takePhoto();
                    break;
                case R.id.recording:
                    if (!isRecording) {
                        startRecording(v);
                    } else {
                        stopRecording();
                        Attachment attachment = new Attachment(Uri.fromFile(new File(recordName)).toString(),
                                MIME_TYPE_AUDIO);
                        attachment.setLength(audioRecordingTime);
                        addAttachment(attachment);
                        mAttachmentAdapter.notifyDataSetChanged();
                        //mGridView.autoresize();
                    }
                    break;
                case R.id.video:
                    takeVideo();
                    break;
                case R.id.files:
                    if (ContextCompat
                            .checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        startGetContentAction();
                    } else {
                        askReadExternalStoragePermission();
                    }
                    break;
                case R.id.location:
                    displayLocation();
                    break;
                case R.id.timestamp:
                    addTimestamp();
                    break;
            }
            if (!isRecording) {
                attachmentDialog.dismiss();
            }
        }
    }
}

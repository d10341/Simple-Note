package com.lx2td.simplenote.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.lx2td.simplenote.NoteActivity;
import com.lx2td.simplenote.models.Attachment;

import java.io.File;

public class FileProviderHelper {
    private FileProviderHelper() {
        // hides public constructor
    }

    /**
     * Generates the FileProvider URI for a given existing file
     */
    public static Uri getFileProvider(File file) {
        Context context = NoteActivity.getContext();
        return FileProvider.getUriForFile(context,
                context.getPackageName() + ".authority", file);
    }

    /**
     * Generates a shareable URI for a given attachment by evaluating its stored (into DB) path
     */
    public static Uri getShareableUri(Attachment attachment) {
        File attachmentFile = new File(attachment.getUriPath());
        if (attachmentFile.exists()) {
            return FileProviderHelper.getFileProvider(attachmentFile);
        } else {
            return Uri.parse(attachment.getUriPath());
        }
    }
}

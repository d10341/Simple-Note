package com.lx2td.simplenote.utils;

import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

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
        return FileProvider.getUriForFile(getAppContext(),
                OmniNotes.getAppContext().getPackageName() + ".authority", file);
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

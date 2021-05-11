package com.lx2td.simplenote.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions.RxPermissions;

public class PermissionsHelper {
    private PermissionsHelper() {
        // hides public constructor
    }

    public static void requestPermission(Activity activity, String permission,
                                         int rationaleDescription, View
                                                 messageView, OnPermissionRequestedListener onPermissionRequestedListener) {

        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Snackbar.make(messageView, rationaleDescription, Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> requestPermissionExecute(activity, permission,
                                onPermissionRequestedListener, messageView))
                        .show();
            } else {
                requestPermissionExecute(activity, permission, onPermissionRequestedListener, messageView);
            }
        } else {
            if (onPermissionRequestedListener != null) {
                onPermissionRequestedListener.onPermissionGranted();
            }
        }
    }

    private static void requestPermissionExecute(Activity activity, String permission,
                                                 OnPermissionRequestedListener
                                                         onPermissionRequestedListener, View messageView) {
        RxPermissions.getInstance(activity)
                .request(permission)
                .subscribe(granted -> {
                    if (granted && onPermissionRequestedListener != null) {
                        onPermissionRequestedListener.onPermissionGranted();
                    } else {
                        String msg = "Permission not granted" + ": " + permission;
                        Snackbar.make(messageView, msg, Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}

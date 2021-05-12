package com.lx2td.simplenote.helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.lx2td.simplenote.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class HelperUtils {

    public static String TEXT_FILE_EXTENSION = ".txt";
    public static String PREFERENCE_COLOUR_PRIMARY = "colourPrimary";
    public static String PREFERENCE_COLOUR_FONT = "colourFont";
    public static String PREFERENCE_COLOUR_BACKGROUND = "colourBackground";
    public static String PREFERENCE_COLOUR_NAVBAR = "colourNavbar";

    public static int darkenColor(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darken(red, fraction);
        green = darken(green, fraction);
        blue = darken(blue, fraction);
        int alpha = Color.alpha(color);
        return Color.argb(alpha, red, green, blue);
    }

    private static int darken(int color, double fraction) {
        return (int) Math.max(color - (color * fraction), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void applyColours(Activity activity, int colourPrimary, boolean colourNavbar) {
        //Get the activity window
        Window window = activity.getWindow();

        // Draw over the navigation bar
        if (colourNavbar)
            window.setNavigationBarColor(colourPrimary);

        // Colour the status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(HelperUtils.darkenColor(colourPrimary, 0.2));

        // Set task description, colour and icon for the app switcher (TaskDescription constructor deprecated in API 28)
        activity.setTaskDescription(new ActivityManager.TaskDescription(activity.getString(R.string.app_name),
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_note), colourPrimary));
    }

}

package com.jctsl.jctslonline;

import android.graphics.Color;
import android.util.Pair;
import android.widget.Toast;

import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.nightonke.boommenu.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Weiping Huang at 23:44 on 16/11/21
 * For Personal Open Source
 * Contact me at 2584541288@qq.com or nightonke@outlook.com
 * For more projects: https://github.com/Nightonke
 */
public class BuilderManager {

    private static int[] imageResources = new int[]{
            R.drawable.profile,
            R.drawable.history,
            R.drawable.buspass,
            R.drawable.concession,
            R.drawable.busroute,
            R.drawable.logout,
            R.drawable.aboutus,
            R.drawable.contactus,
            R.drawable.exit
    };

    private static String[] stringResources = new String[]{
            "Profile",
            "History",
            "Bus Pass",
            "Consession",
            "Bus Route",
            "Logout",
            "About Us",
            "Contact Us",
            "Exit"
    };

    private static int imageResourceIndex = 0;
    private static int stringResourceIndex = 0;

    static int getImageResource() {
        if (imageResourceIndex >= imageResources.length) imageResourceIndex = 0;
        return imageResources[imageResourceIndex++];
    }
    static String getStringResource() {
        if (stringResourceIndex >= stringResources.length) stringResourceIndex = 0;
        return stringResources[stringResourceIndex++];
    }










    private BuilderManager() {
    }
}

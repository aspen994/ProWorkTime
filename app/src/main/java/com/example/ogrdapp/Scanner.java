package com.example.ogrdapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.zxing.Result;
import com.journeyapps.barcodescanner.CaptureActivity;

public class Scanner extends CaptureActivity {
    public static boolean SCANNER_FLAG = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SCANNER_FLAG=true;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

}
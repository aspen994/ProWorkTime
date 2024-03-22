package com.osinTechInnovation.ogrdapp;


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
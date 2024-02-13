package com.example.ogrdapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ogrdapp.model.QRModel;
import com.example.ogrdapp.model.User;
import com.example.ogrdapp.viewmodel.AuthViewModel;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRCodeManagement extends AppCompatActivity {


    private int inputValue;

    private QRGEncoder qrgEncoder;
    private Bitmap bitmap;

    EditText editText;
    Button generatedQR, saveQR;
    ImageView qrImage;
    int min = 33;
    int max = 126;

    int lengthPassword = 73;
    private StringBuilder qrCodeEncode;

    private AuthViewModel authViewModel;
    private List<QRModel> arrayListQRModel;
    private List<Integer> arrayListDelayInteger;
    String name;
    String adminId;
    private boolean flag=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_managment);

        editText = findViewById(R.id.editText_delay_input);
        generatedQR = findViewById(R.id.generatedBtn);
        saveQR = findViewById(R.id.saveBtn);
        qrImage = findViewById(R.id.qr_image);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        arrayListQRModel = new ArrayList<>();
        arrayListDelayInteger = new ArrayList<>();


        authViewModel.getAdminIdMutableLiveData().observe(QRCodeManagement.this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                adminId=s;
                authViewModel.getDataQRCode(s).observe(QRCodeManagement.this, new Observer<List<QRModel>>() {
                    @Override
                    public void onChanged(List<QRModel> qrModels) {
                        arrayListQRModel.addAll(qrModels);
                    }
                });
            }
        });


        generatedQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                inputValue = Integer.parseInt(editText.getText().toString().trim().equals("")?("-1"):editText.getText().toString().trim());

                for (QRModel qrModel : arrayListQRModel) {
                    if (inputValue == qrModel.getDelay()) {
                        flag = false;
                        Toast.makeText(QRCodeManagement.this, "Retrive Old one", Toast.LENGTH_SHORT).show();
                        qrCodeEncode = new StringBuilder();
                        qrCodeEncode.append(qrModel.getQRCode());
                        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        Display display = manager.getDefaultDisplay();
                        Point point = new Point();
                        display.getSize(point);
                        int width = point.x;
                        int height = point.y;
                        int smallerDimension = Math.min(width, height);
                        smallerDimension = smallerDimension * 3 / 4;

                        qrgEncoder = new QRGEncoder(qrCodeEncode.toString(), null, QRGContents.Type.TEXT, smallerDimension);
                        qrgEncoder.setColorWhite(Color.parseColor("#000000"));
                        qrgEncoder.setColorBlack(Color.parseColor("#ffffff"));
                        try {
                            bitmap = qrgEncoder.getBitmap();
                            qrImage.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                    if (flag) {
                        Toast.makeText(QRCodeManagement.this, "Generate new one", Toast.LENGTH_SHORT).show();
                        qrCodeEncode = new StringBuilder();
                        generateQR();
                        // Toast.makeText(QRCodeManagement.this, "generuje", Toast.LENGTH_SHORT).show();
                        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        Display display = manager.getDefaultDisplay();
                        Point point = new Point();
                        display.getSize(point);
                        int width = point.x;
                        int height = point.y;
                        int smallerDimension = Math.min(width, height);
                        smallerDimension = smallerDimension * 3 / 4;

                        qrgEncoder = new QRGEncoder(qrCodeEncode.toString(), null, QRGContents.Type.TEXT, smallerDimension);
                        qrgEncoder.setColorWhite(Color.parseColor("#000000"));
                        qrgEncoder.setColorBlack(Color.parseColor("#ffffff"));
                        try {
                            bitmap = qrgEncoder.getBitmap();
                            qrImage.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }



            }

        });

        saveQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qrCodeEncode != null) {
                    // Toast.makeText(QRCodeManagement.this, "isNotNull", Toast.LENGTH_SHORT).show();
                    saveImage();
                    if(flag) {
                        saveToDatabase(qrCodeEncode.toString(), Integer.parseInt(editText.getText().toString()));
                        Toast.makeText(QRCodeManagement.this, "SAVING: "+flag, Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setType("image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    qrCodeEncode = null;
                }
            }
        });
    }

    private void saveToDatabase(String qrCode, int delayInMinutes) {

        Map<String, Object> qrCodeMap = new HashMap<>();

        qrCodeMap.put("idAdmin", adminId);
        qrCodeMap.put("QRCode", qrCode);
        qrCodeMap.put("delay", delayInMinutes);

        authViewModel.setNewQrCode(qrCodeMap);
    }

    private void generateQR() {

        for (int i = 0; i < lengthPassword; i++) {
            int i1 = (int) (Math.random() * (max - min + 1) + min);
            char generatedASCIIChar = (char) i1;
            qrCodeEncode.append(generatedASCIIChar);
        }
    }

    private void saveImage() {
        Uri images;
        ContentResolver contentResolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        name = System.currentTimeMillis() + ".jpg";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");

        Uri uri = contentResolver.insert(images, contentValues);

        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) qrImage.getDrawable();
            Bitmap bitmap1 = bitmapDrawable.getBitmap();

            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
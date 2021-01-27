package com.greenhub.counter.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.greenhub.counter.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import java.io.IOException;

public class SecondSettingActivity extends AppCompatActivity {

    final String THIRD_SETTING_ACTIVITY = "THIRD_SETTING_ACTIVITY";
    private String id_meter;
    private String id_label;

    SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_setting);

        initViews();

        View line = findViewById(R.id.line);
        Animation lineAnim = AnimationUtils.loadAnimation(this, R.anim.line_animation);
        line.startAnimation(lineAnim);
    }

    private void initViews() {
        surfaceView = findViewById(R.id.surfaceView);
    }

    private void initialiseDetectorsAndSources() {
        System.out.println("Сканер штрих-кода запущен");
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(SecondSettingActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(SecondSettingActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                System.out.println("Для предотвращения утечек памяти сканер штрих-кода был остановлен");
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size()!=0){
                    if (barcodes.valueAt(0).displayValue.length() == 8){
                        id_meter = barcodes.valueAt(0).displayValue;
                        System.out.println(id_meter);
                    }else {
                        id_label = barcodes.valueAt(0).displayValue;
                        System.out.println(id_label);
                    }

                    if (id_meter!=null && id_label!=null){
                        Intent intent = new Intent(THIRD_SETTING_ACTIVITY);
                        intent.putExtra("code_label2", id_label);
                        intent.putExtra("code_meter2", id_meter);

                        Bundle args = getIntent().getExtras();
                        String typeOfMeter2 = args.getString("type_Of_Meter");

                        intent.putExtra("type_Of_Meter2", typeOfMeter2);

                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}
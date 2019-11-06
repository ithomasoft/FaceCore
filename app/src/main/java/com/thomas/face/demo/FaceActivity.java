package com.thomas.face.demo;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.thomas.face.core.FaceServer;
import com.thomas.face.core.model.ScanResult;
import com.thomas.face.core.widget.FaceView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class FaceActivity extends AppCompatActivity implements FaceView.Delegate {
    FaceView faceView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            }
        });


        faceView = findViewById(R.id.face_view);
        faceView.setDelegate(this);
        textView = findViewById(R.id.tv_tips);

    }

    @Override
    protected void onStart() {
        super.onStart();
        faceView.startCamera();
        faceView.startSpot();
    }

    @Override
    protected void onStop() {
        super.onStop();
        faceView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        faceView.onDestroy();

        super.onDestroy();
    }

    @Override
    public void onScanSuccess(ScanResult scanResult) {
        if (scanResult.code==666){
            textView.setText(scanResult.result);
            Toast.makeText(FaceActivity.this,scanResult.result,Toast.LENGTH_LONG).show();
        }else {
            textView.setText(scanResult.result);
            faceView.startSpot();
        }

    }

    @Override
    public void onOpenCameraError() {

    }
}

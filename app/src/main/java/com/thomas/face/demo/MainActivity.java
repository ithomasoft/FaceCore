package com.thomas.face.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.thomas.face.core.FaceSDK;
import com.thomas.face.core.FaceServer;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yanzhenjie.permission.runtime.PermissionDef;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Album.initialize(AlbumConfig.newBuilder(this)
                .setAlbumLoader(new MediaLoader())
                .build());
        setContentView(R.layout.activity_main);
        activeEngine();
        button = findViewById(R.id.btn_scan_face);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpScanFace();
            }
        });
        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


    }

    private void chooseImage() {
        AndPermission.with(this).runtime().permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        openImages();
                    }
                }).onDenied(new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {

            }
        }).start();
    }

    private void openImages() {
        Album.image(this)
                .singleChoice()
                .camera(true)
                .columnCount(3)
                .widget(
                        Widget.newDarkBuilder(this)
                                .title("选择图片")
                                .build()
                )
                .onResult(new com.yanzhenjie.album.Action<ArrayList<AlbumFile>>() {
                    @Override
                    public void onAction(@NonNull ArrayList<AlbumFile> result) {
                        Log.e("faceSDK", "filePath---" + result.get(0).getPath());
                        registerFace(result.get(0).getPath());
                    }
                })
                .onCancel(new com.yanzhenjie.album.Action<String>() {
                    @Override
                    public void onAction(@NonNull String result) {

                    }
                })
                .start();
    }

    private void registerFace(String path) {
        FaceServer.getInstance().init(this);
        FaceServer.getInstance().clearAllFaces(this);
       boolean success = FaceServer.getInstance().registerBgr24(this, path);
       if (success){
           Log.e("faceSDK","注册成功");
       }else {
           Log.e("faceSDK","注册失败");
       }
        Toast.makeText(MainActivity.this,"注册结果："+success,Toast.LENGTH_LONG).show();
    }

    private void activeEngine() {
        AndPermission.with(this).runtime().permission(Permission.READ_PHONE_STATE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        int code = FaceSDK.activeFaceEngine(MainActivity.this, "7CeW7tQ1NgFs2t5T8eaVpUSBSSyq1y3tdLEXoohJreHg", "3h1wcGMeoQsaGNZU5bAFipAuFa21ZZbJmCtv4xHQU1Yq");
                        if (code == 0) {
                            button.setClickable(true);
                            button.setEnabled(true);
                        } else {
                            button.setClickable(false);
                            button.setEnabled(false);
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        button.setClickable(false);
                        button.setEnabled(false);
                    }
                }).start();
    }

    private void jumpScanFace() {
        AndPermission.with(this).runtime().permission(Permission.CAMERA)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, FaceActivity.class);
                        startActivity(intent);

                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        finish();
                    }
                }).start();

    }

    @Override
    protected void onDestroy() {
        FaceServer.getInstance().unInit();
        super.onDestroy();
    }
}

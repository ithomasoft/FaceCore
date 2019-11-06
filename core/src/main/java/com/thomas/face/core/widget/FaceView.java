package com.thomas.face.core.widget;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.thomas.face.core.FaceSDK;
import com.thomas.face.core.FaceServer;
import com.thomas.face.core.R;
import com.thomas.face.core.helper.ProcessDataTask;
import com.thomas.face.core.helper.ScreenHelper;
import com.thomas.face.core.model.ScanResult;

/**
 * @author Thomas
 * @describe
 * @date 2019/10/15
 * @updatelog
 * @since
 */
public class FaceView extends RelativeLayout implements Camera.PreviewCallback {

    private static final int NO_CAMERA_ID = -1;
    protected Camera mCamera;
    protected CameraPreview mCameraPreview;
    protected Delegate mDelegate;
    protected boolean mSpotAble = false;
    protected ProcessDataTask mProcessDataTask;
    protected int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    protected FaceEngine faceEngine;

    public FaceView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
        setReader(context);
    }

    private void setReader(Context context) {
        FaceServer.getInstance().init(context);
        faceEngine = FaceSDK.initVideoFaceEngine(context, 1);
    }

    private void initView(Context context, AttributeSet attrs) {
        mCameraPreview = new CameraPreview(context);
        mCameraPreview.setDelegate(new CameraPreview.Delegate() {
            @Override
            public void onStartPreview() {
                setOneShotPreviewCallback();
            }
        });

        mCameraPreview.setId(R.id.camera_preview);
        addView(mCameraPreview);
    }


    private void setOneShotPreviewCallback() {
        if (mSpotAble && mCameraPreview.isPreviewing()) {
            try {
                mCamera.setOneShotPreviewCallback(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setDelegate(Delegate delegate) {
        mDelegate = delegate;
    }

    public CameraPreview getCameraPreview() {
        return mCameraPreview;
    }


    /**
     * 打开后置摄像头开始预览，但是并未开始识别
     */
    public void startCamera() {
        startCamera(mCameraId);
    }

    /**
     * 打开指定摄像头开始预览，但是并未开始识别
     */
    public void startCamera(int cameraFacing) {
        if (mCamera != null || Camera.getNumberOfCameras() == 0) {
            return;
        }
        int ultimateCameraId = findCameraIdByFacing(cameraFacing);
        if (ultimateCameraId != NO_CAMERA_ID) {
            startCameraById(ultimateCameraId);
            return;
        }

        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            ultimateCameraId = findCameraIdByFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            ultimateCameraId = findCameraIdByFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        if (ultimateCameraId != NO_CAMERA_ID) {
            startCameraById(ultimateCameraId);
        }
    }

    private int findCameraIdByFacing(int cameraFacing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            try {
                Camera.getCameraInfo(cameraId, cameraInfo);
                if (cameraInfo.facing == cameraFacing) {
                    return cameraId;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return NO_CAMERA_ID;
    }

    private void startCameraById(int cameraId) {
        try {
            mCameraId = cameraId;
            mCamera = Camera.open(cameraId);
            mCameraPreview.setCamera(mCamera);
        } catch (Exception e) {
            e.printStackTrace();
            if (mDelegate != null) {
                mDelegate.onOpenCameraError();
            }
        }
    }

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void stopCamera() {
        try {
            stopSpot();
            if (mCamera != null) {
                mCameraPreview.stopCameraPreview();
                mCameraPreview.setCamera(null);
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始识别
     */
    public void startSpot() {
        mSpotAble = true;
        startCamera();
        setOneShotPreviewCallback();
    }

    /**
     * 停止识别
     */
    public void stopSpot() {
        mSpotAble = false;

        if (mProcessDataTask != null) {
            mProcessDataTask.cancelTask();
            mProcessDataTask = null;
        }

        if (mCamera != null) {
            try {
                mCamera.setOneShotPreviewCallback(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开闪光灯
     */
    public void openFlashlight() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mCameraPreview.openFlashlight();
            }
        }, mCameraPreview.isPreviewing() ? 0 : 500);
    }

    /**
     * 关闭闪光灯
     */
    public void closeFlashlight() {
        mCameraPreview.closeFlashlight();
    }

    /**
     * 销毁二维码扫描控件
     */
    public void onDestroy() {
        stopCamera();
        FaceServer.getInstance().unInit();
        faceEngine.unInit();
        mDelegate = null;
    }


    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        if (!mSpotAble || (mProcessDataTask != null && (mProcessDataTask.getStatus() == AsyncTask.Status.PENDING
                || mProcessDataTask.getStatus() == AsyncTask.Status.RUNNING))) {
            return;
        }

        mProcessDataTask = new ProcessDataTask(camera, data, this, ScreenHelper.isPortrait(getContext())).perform();
    }


    void onScanBoxRectChanged(Rect rect) {
        mCameraPreview.onScanBoxRectChanged(rect);
    }


    public ScanResult processData(byte[] data, int width, int height) {
        int code = FaceSDK.detectVideoFaces(faceEngine, data, width, height);
        if (code != ErrorInfo.MOK) {
            return new ScanResult("未检测到人脸", code);
        }

        code = FaceSDK.process(faceEngine, data, width, height);

        if (code != ErrorInfo.MOK) {
            return new ScanResult("未检测到人脸", code);
        }

        code = FaceSDK.checkLiveness(faceEngine);
        if (code != 1) {
            return new ScanResult("活体认证失败", code);
        }

        FaceFeature faceFeature = FaceSDK.extractFaceFeature(faceEngine, data, width, height);

        if (faceFeature == null) {
            return new ScanResult("未检测到人脸", code);
        }

        float similar =FaceServer.getInstance().getTopOfFaceLib(faceFeature);
        if (similar > 0.8) {
            return new ScanResult("认证，进行下一步",666);
        } else {
            return new ScanResult("认证失败，请重试",999);
        }


    }

    public void onPostParseData(ScanResult scanResult) {
        if (!mSpotAble) {
            return;
        }
        String result = scanResult == null ? null : scanResult.result;
        if (TextUtils.isEmpty(result)) {
            try {
                if (mCamera != null) {
                    mCamera.setOneShotPreviewCallback(FaceView.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mSpotAble = false;
            try {
                if (mDelegate != null) {
                    mDelegate.onScanSuccess(scanResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface Delegate {
        /**
         * 处理扫描结果
         */
        void onScanSuccess(ScanResult scanResult);

        /**
         * 处理打开相机出错
         */
        void onOpenCameraError();
    }

}

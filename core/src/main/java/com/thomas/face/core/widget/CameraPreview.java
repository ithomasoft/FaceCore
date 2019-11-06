package com.thomas.face.core.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.thomas.face.core.helper.CameraConfigurationHelper;
import com.thomas.face.core.helper.ScreenHelper;

import java.util.Collections;

/**
 * @author Thomas
 * @describe
 * @date 2019/10/15
 * @updatelog
 * @since
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private boolean mPreviewing = true;
    private boolean mSurfaceCreated = false;
    private Delegate mDelegate;

    private CameraConfigurationHelper mCameraConfigurationManager;


    public CameraPreview(Context context) {
        super(context);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mCameraConfigurationManager = new CameraConfigurationHelper(getContext());
            mCameraConfigurationManager.initFromCameraParameters(mCamera);

            getHolder().addCallback(this);
            if (mPreviewing) {
                requestLayout();
            } else {
                showCameraPreview();
            }
        }
    }

    void setDelegate(Delegate delegate) {
        mDelegate = delegate;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        stopCameraPreview();
        showCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = false;
        stopCameraPreview();
    }

    private void showCameraPreview() {
        if (mCamera != null) {
            try {
                mPreviewing = true;
                SurfaceHolder surfaceHolder = getHolder();
                surfaceHolder.setKeepScreenOn(true);
                mCamera.setPreviewDisplay(surfaceHolder);

                mCameraConfigurationManager.setDesiredCameraParameters(mCamera);
                mCamera.startPreview();
                if (mDelegate != null) {
                    mDelegate.onStartPreview();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void stopCameraPreview() {
        if (mCamera != null) {
            try {
                mPreviewing = false;
                mCamera.cancelAutoFocus();
                mCamera.setOneShotPreviewCallback(null);
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        if (mCameraConfigurationManager != null && mCameraConfigurationManager.getCameraResolution() != null) {
            Point cameraResolution = mCameraConfigurationManager.getCameraResolution();
            // 取出来的cameraResolution高宽值与屏幕的高宽顺序是相反的
            int cameraPreviewWidth = cameraResolution.x;
            int cameraPreviewHeight = cameraResolution.y;
            if (width * 1f / height < cameraPreviewWidth * 1f / cameraPreviewHeight) {
                float ratio = cameraPreviewHeight * 1f / cameraPreviewWidth;
                width = (int) (height / ratio + 0.5f);
            } else {
                float ratio = cameraPreviewWidth * 1f / cameraPreviewHeight;
                height = (int) (width / ratio + 0.5f);
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    interface Delegate {
        void onStartPreview();
    }

    void openFlashlight() {
        if (flashLightAvailable()) {
            mCameraConfigurationManager.openFlashlight(mCamera);
        }
    }

    void closeFlashlight() {
        if (flashLightAvailable()) {
            mCameraConfigurationManager.closeFlashlight(mCamera);
        }
    }
    private boolean flashLightAvailable() {
        return isPreviewing() && getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    void onScanBoxRectChanged(Rect scanRect) {
        if (mCamera == null || scanRect == null || scanRect.left <= 0 || scanRect.top <= 0) {
            return;
        }
        int centerX = scanRect.centerX();
        int centerY = scanRect.centerY();
        int rectHalfWidth = scanRect.width() / 2;
        int rectHalfHeight = scanRect.height() / 2;


        if (ScreenHelper.isPortrait(getContext())) {
            int temp = centerX;
            centerX = centerY;
            centerY = temp;

            temp = rectHalfWidth;
            rectHalfWidth = rectHalfHeight;
            rectHalfHeight = temp;
        }
        scanRect = new Rect(centerX - rectHalfWidth, centerY - rectHalfHeight, centerX + rectHalfWidth, centerY + rectHalfHeight);

        handleFocusMetering(scanRect.centerX(), scanRect.centerY(), scanRect.width(), scanRect.height());
    }


    private void handleFocusMetering(float originFocusCenterX, float originFocusCenterY,
                                     int originFocusWidth, int originFocusHeight) {
        try {
            boolean isNeedUpdate = false;
            Camera.Parameters focusMeteringParameters = mCamera.getParameters();
            Camera.Size size = focusMeteringParameters.getPreviewSize();
            if (focusMeteringParameters.getMaxNumFocusAreas() > 0) {
                isNeedUpdate = true;
                Rect focusRect = ScreenHelper.calculateFocusMeteringArea(1f,
                        originFocusCenterX, originFocusCenterY,
                        originFocusWidth, originFocusHeight,
                        size.width, size.height);
                focusMeteringParameters.setFocusAreas(Collections.singletonList(new Camera.Area(focusRect, 1000)));
                focusMeteringParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            }

            if (focusMeteringParameters.getMaxNumMeteringAreas() > 0) {
                isNeedUpdate = true;
                Rect meteringRect = ScreenHelper.calculateFocusMeteringArea(1.5f,
                        originFocusCenterX, originFocusCenterY,
                        originFocusWidth, originFocusHeight,
                        size.width, size.height);
                focusMeteringParameters.setMeteringAreas(Collections.singletonList(new Camera.Area(meteringRect, 1000)));
            }

            if (isNeedUpdate) {
                mCamera.cancelAutoFocus();
                mCamera.setParameters(focusMeteringParameters);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    public void onAutoFocus(boolean success, Camera camera) {
                        startContinuousAutoFocus();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            startContinuousAutoFocus();
        }
    }

    /**
     * 连续对焦
     */
    private void startContinuousAutoFocus() {
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            // 连续对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            // 要实现连续的自动对焦，这一句必须加上
            mCamera.cancelAutoFocus();
        } catch (Exception e) {
        }
    }


    boolean isPreviewing() {
        return mCamera != null && mPreviewing && mSurfaceCreated;
    }


}

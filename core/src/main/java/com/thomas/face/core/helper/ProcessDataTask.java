package com.thomas.face.core.helper;

import android.hardware.Camera;
import android.os.AsyncTask;

import com.thomas.face.core.model.ScanResult;
import com.thomas.face.core.widget.FaceView;

import java.lang.ref.WeakReference;

/**
 * @author Thomas
 * @describe
 * @date 2019/10/15
 * @updatelog
 * @since
 */
public class ProcessDataTask extends AsyncTask<Void, Void, ScanResult> {

    private Camera mCamera;
    private byte[] mData;
    private boolean mIsPortrait;
    private WeakReference<FaceView> mFaceViewRef;
    private static long sLastStartTime = 0;

    public ProcessDataTask(Camera camera, byte[] data, FaceView faceView, boolean isPortrait) {
        mCamera = camera;
        mData = data;
        mFaceViewRef = new WeakReference<>(faceView);
        mIsPortrait = isPortrait;
    }

    public ProcessDataTask perform() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return this;
    }

    public void cancelTask() {
        if (getStatus() != Status.FINISHED) {
            cancel(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mFaceViewRef.clear();
        mData = null;
    }

    private ScanResult processData(FaceView faceView) {
        if (mData == null) {
            return null;
        }

        int width = 0;
        int height = 0;
        byte[] data = mData;

        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        width = size.width;
        height = size.height;

        return faceView.processData(data, width, height);

    }

    @Override
    protected ScanResult doInBackground(Void... params) {
        FaceView faceView = mFaceViewRef.get();
        if (faceView == null) {
            return null;
        }


        sLastStartTime = System.currentTimeMillis();

        ScanResult scanResult = processData(faceView);

        return scanResult;

    }

    @Override
    protected void onPostExecute(ScanResult result) {
        FaceView faceView = mFaceViewRef.get();
        if (faceView == null) {
            return;
        }
        faceView.onPostParseData(result);
    }


}

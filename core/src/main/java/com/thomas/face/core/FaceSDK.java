package com.thomas.face.core;

import android.content.Context;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;
import com.thomas.face.core.helper.FaceSharePrefHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas
 * @describe
 * @date 2019/10/15
 * @updatelog
 * @since
 */
public class FaceSDK {
    private static List<FaceInfo> faceInfoList = new ArrayList<>();
    private static List<LivenessInfo> livenessInfoList = new ArrayList<>();


    /**
     * 激活引擎
     *
     * @param context
     * @param appId
     * @param sdkKey
     * @return
     */
    public static int activeFaceEngine(Context context, String appId, String sdkKey) {
        if (isActived(context)) {
            return ErrorInfo.MOK;
        } else {
            FaceEngine faceEngine = new FaceEngine();
            int activeCode = faceEngine.activeOnline(context, appId, sdkKey);
            if (activeCode == ErrorInfo.MOK) {
                FaceSharePrefHelper.getInstance(context).put("active", true);
                return activeCode;
            } else if (activeCode == ErrorInfo.MERR_ASF_READ_PHONE_STATE_DENIED) {
                throw new RuntimeException("在激活之前必须允许【android.permission.READ_PHONE_STATE】权限");
            } else if (activeCode == ErrorInfo.MERR_ASF_INTERNET_DENIED) {
                throw new RuntimeException("在激活之前必须允许【android.permission.INTERNET】权限");
            } else {
                return activeCode;
            }

        }
    }

    /**
     * 初始化视频识别引擎
     *
     * @param context
     * @param detectFaceMaxNum 最大检测人数
     * @return
     */
    public static FaceEngine initVideoFaceEngine(Context context, int detectFaceMaxNum) {
        FaceEngine faceEngine = new FaceEngine();
        int initCode = faceEngine.init(context, FaceEngine.ASF_DETECT_MODE_VIDEO,
                FaceEngine.ASF_OP_270_ONLY,
                16,
                detectFaceMaxNum,
                FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT |
                        FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER |
                        FaceEngine.ASF_LIVENESS);
        if (initCode == ErrorInfo.MOK) {
            FaceSharePrefHelper.getInstance(context).put("initVideo", true);
            return faceEngine;
        } else {
            return null;
        }

    }

    /**
     * 获取视频流中的图像信息
     *
     * @param faceEngine
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static int detectVideoFaces(FaceEngine faceEngine, byte[] data, int width, int height) {
        faceInfoList.clear();
        int code = faceEngine.detectFaces(data, width, height, FaceEngine.CP_PAF_NV21, faceInfoList);

        if (code == ErrorInfo.MOK) {
            if (faceInfoList.size() > 0) {
                return code;
            } else {
                return -1;
            }
        }
        return code;
    }

    /**
     * 人脸信息检测
     *
     * @param faceEngine
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static int process(FaceEngine faceEngine, byte[] data, int width, int height) {
        int code = faceEngine.process(data, width, height, FaceEngine.CP_PAF_NV21, faceInfoList, FaceEngine.ASF_LIVENESS);
        return code;
    }

    public static int checkLiveness(FaceEngine faceEngine) {
        int code = faceEngine.getLiveness(livenessInfoList);
        if (code == ErrorInfo.MOK) {
            if (livenessInfoList.size() > 0) {
                return livenessInfoList.get(0).getLiveness();
            }
        }
        return code;
    }

    public static FaceFeature extractFaceFeature(FaceEngine faceEngine, byte[] data, int width, int height) {
        FaceFeature faceFeature = new FaceFeature();
        int code = faceEngine.extractFaceFeature(data, width, height, FaceEngine.CP_PAF_NV21, faceInfoList.get(0), faceFeature);
        if (code == ErrorInfo.MOK) {
            return faceFeature;
        } else {
            return null;
        }

    }

    /**
     * 引擎是否激活
     *
     * @param context
     * @return
     */
    public static boolean isActived(Context context) {
        return FaceSharePrefHelper.getInstance(context).getBoolean("active");
    }

    /**
     * 引擎是否激活
     *
     * @param context
     * @return
     */
    public static boolean isVideoInit(Context context) {
        return FaceSharePrefHelper.getInstance(context).getBoolean("initVideo");
    }

}

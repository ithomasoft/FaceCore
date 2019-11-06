package com.thomas.face.core.model;

/**
 * @author Thomas
 * @describe
 * @date 2019/10/15
 * @updatelog
 * @since
 */
public class FaceRegisterInfo {
    private byte[] featureData;
    private String name;

    public FaceRegisterInfo(byte[] faceFeature, String name) {
        this.featureData = faceFeature;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getFeatureData() {
        return featureData;
    }

    public void setFeatureData(byte[] featureData) {
        this.featureData = featureData;
    }

}

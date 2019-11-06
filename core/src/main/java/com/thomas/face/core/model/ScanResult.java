package com.thomas.face.core.model;

/**
 * @author Thomas
 * @describe
 * @date 2019/10/15
 * @updatelog
 * @since
 */
public class ScanResult {
    public String result;
    public int code;

    public ScanResult(String result) {
        this.result = result;
    }

    public ScanResult(String result, int code) {
        this.result = result;
        this.code = code;
    }
}

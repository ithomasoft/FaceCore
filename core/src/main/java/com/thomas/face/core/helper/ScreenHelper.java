package com.thomas.face.core.helper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author Thomas
 * @describe
 * @date 2019/10/15
 * @updatelog
 * @since
 */
public class ScreenHelper {
    /**
     * 是否为竖屏
     */
    public static boolean isPortrait(Context context) {
        Point screenResolution = getScreenResolution(context);
        return screenResolution.y > screenResolution.x;
    }

    public static Point getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenResolution = new Point();
        display.getSize(screenResolution);
        return screenResolution;
    }

    public static int getStatusBarHeight(Context context) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowFullscreen
        });
        boolean windowFullscreen = typedArray.getBoolean(0, false);
        typedArray.recycle();

        if (windowFullscreen) {
            return 0;
        }

        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }


    /**
     * 计算对焦和测光区域
     *
     * @param coefficient        比率
     * @param originFocusCenterX 对焦中心点X
     * @param originFocusCenterY 对焦中心点Y
     * @param originFocusWidth   对焦宽度
     * @param originFocusHeight  对焦高度
     * @param previewViewWidth   预览宽度
     * @param previewViewHeight  预览高度
     */
    public static Rect calculateFocusMeteringArea(float coefficient,
                                                  float originFocusCenterX, float originFocusCenterY,
                                                  int originFocusWidth, int originFocusHeight,
                                                  int previewViewWidth, int previewViewHeight) {

        int halfFocusAreaWidth = (int) (originFocusWidth * coefficient / 2);
        int halfFocusAreaHeight = (int) (originFocusHeight * coefficient / 2);

        int centerX = (int) (originFocusCenterX / previewViewWidth * 2000 - 1000);
        int centerY = (int) (originFocusCenterY / previewViewHeight * 2000 - 1000);

        RectF rectF = new RectF(clamp(centerX - halfFocusAreaWidth, -1000, 1000),
                clamp(centerY - halfFocusAreaHeight, -1000, 1000),
                clamp(centerX + halfFocusAreaWidth, -1000, 1000),
                clamp(centerY + halfFocusAreaHeight, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top),
                Math.round(rectF.right), Math.round(rectF.bottom));
    }


    static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }


}

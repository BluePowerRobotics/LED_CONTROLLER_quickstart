/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.firstinspires.ftc.teamcode.controllers.led;

import androidx.annotation.IntRange;
import androidx.annotation.Size;

import java.util.Arrays;
import java.util.HashMap;


/**
 * Color实现，基于android.graphics.Color进行简化，20260718
 */
public class Color {
    public static final int BLACK       = 0xFF000000;
     public static final int DKGRAY      = 0xFF444444;
     public static final int GRAY        = 0xFF888888;
     public static final int LTGRAY      = 0xFFCCCCCC;
     public static final int WHITE       = 0xFFFFFFFF;
     public static final int RED         = 0xFFFF0000;
     public static final int GREEN       = 0xFF00FF00;
     public static final int BLUE        = 0xFF0000FF;
     public static final int YELLOW      = 0xFFFFFF00;
     public static final int CYAN        = 0xFF00FFFF;
     public static final int MAGENTA     = 0xFFFF00FF;


    private double r=0;
    private double g=0;
    private double b=0;
    private double a=0;


    public Color() {
    }


    private Color(float r, float g, float b, float a) {
        this.r=r;
        this.g=g;
        this.b=b;
        this.a=a;
    }

    public int toArgb() {

        return ((int) (a * 255.0f + 0.5f) << 24) |
               ((int) (r * 255.0f + 0.5f) << 16) |
               ((int) (g * 255.0f + 0.5f) <<  8) |
                (int) (b * 255.0f + 0.5f);
    }


    public float red() {
        return (float) r;
    }

    public float green() {
        return (float) g;
    }

    public float blue() {
        return (float) b;
    }


    public float alpha() {
        return (float) a;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;
        if(color.a!=a) return false;
        if(color.r!=r) return false;
        if(color.g!=g) return false;
        if(color.b!=b) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(new double[]{a, r, g, b});
        return result;
    }

    /**
     * <p>Returns a string representation of the object. This method returns
     * a string equal to the value of:</p>
     *
     * <pre class="prettyprint">
     * "Color(" + r + ", " + g + ", " + b + ", " + a +
     *         ", " + getColorSpace().getName + ')'
     * </pre>
     *
     * <p>For instance, the string representation of opaque black in the sRGB
     * color space is equal to the following value:</p>
     *
     * <pre>
     * Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
     * </pre>
     *
     * @return A non-null string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Color(").append(a)
                .append(',')
                .append(r)
                .append(',')
                .append(g)
                .append(',')
                .append(b);

        s.append(')');
        return s.toString();
    }
    public static Color valueOf( int color) {
        float r = ((color >> 16) & 0xff) / 255.0f;
        float g = ((color >>  8) & 0xff) / 255.0f;
        float b = ((color      ) & 0xff) / 255.0f;
        float a = ((color >> 24) & 0xff) / 255.0f;
        return new Color(r, g, b, a);
    }
    public static Color valueOf(float r, float g, float b, float a) {
        return new Color(r,g,b,a);
    }
    public static Color valueOf(float r, float g, float b) {
        return new Color(r, g, b, 1.0f);
    }

    /**
     * Convert HSV components to an ARGB color. The alpha component is passed
     * through unchanged.
     * <ul>
     *   <li><code>hsv[0]</code> is Hue \([0..360[\)</li>
     *   <li><code>hsv[1]</code> is Saturation \([0...1]\)</li>
     *   <li><code>hsv[2]</code> is Value \([0...1]\)</li>
     * </ul>
     * If hsv values are out of range, they are pinned.
     * @param alpha the alpha component of the returned argb color.
     * @param hsv  3 element array which holds the input HSV components.
     * @return the resulting argb color
     */

    public static int HSVToColor(@IntRange(from = 0, to = 255) int alpha, @Size(3) float[] hsv) {
        if (hsv.length < 3) {
            return 0;
        }

        float h = hsv[0];
        float s = hsv[1];
        float v = hsv[2];

        // 处理异常数值（NaN / Infinite）
        if (Float.isNaN(h) || Float.isInfinite(h)) h = 0f;
        if (Float.isNaN(s) || Float.isInfinite(s)) s = 0f;
        if (Float.isNaN(v) || Float.isInfinite(v)) v = 0f;

        // 裁剪 S 和 V 到 [0, 1] 区间
        s = Math.max(0.0f, Math.min(1.0f, s));
        v = Math.max(0.0f, Math.min(1.0f, v));

        float r, g, b;

        // 饱和度 S = 0 时，颜色为灰色，RGB 全等于 V
        if (s == 0.0f) {
            r = v;
            g = v;
            b = v;
        } else {
            // 将 H 归一化到 [0, 360) 区间
            // 使用 floor 而不是 %，兼容原生代码对负数的处理逻辑
            float hNormalized = h - (float) Math.floor(h / 360.0f) * 360.0f;
            // 转换为六色扇区 (0 ~ 6)
            float hh = hNormalized / 60.0f;
            int i = (int) hh;          // 扇区索引 (0 ~ 5)
            float f = hh - i;          // 扇区内的小数部分

            float p = v * (1.0f - s);
            float q = v * (1.0f - s * f);
            float t = v * (1.0f - s * (1.0f - f));

            // 根据扇区索引分配 RGB
            switch (i) {
                case 0:
                    r = v;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = v;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = v;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = v;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = v;
                    break;
                case 5:
                default:
                    r = v;
                    g = p;
                    b = q;
                    break;
            }
        }

        // 将浮点颜色 [0,1] 转换为 int [0,255]，原生代码采用 (int)(value * 255.0f + 0.5f) 四舍五入
        // Math.round(float) 底层就是 (int)(value + 0.5f)
        int ir = Math.round(r * 255.0f);
        int ig = Math.round(g * 255.0f);
        int ib = Math.round(b * 255.0f);

        // 裁剪到 [0, 255]
        ir = Math.max(0, Math.min(255, ir));
        ig = Math.max(0, Math.min(255, ig));
        ib = Math.max(0, Math.min(255, ib));


        alpha = Math.max(0, Math.min(255, alpha));


        return (alpha << 24) | (ir << 16) | (ig << 8) | ib;
    }

    private static final HashMap<String, Integer> sColorNameMap;
    static {
        sColorNameMap = new HashMap<>();
        sColorNameMap.put("black", BLACK);
        sColorNameMap.put("darkgray", DKGRAY);
        sColorNameMap.put("gray", GRAY);
        sColorNameMap.put("lightgray", LTGRAY);
        sColorNameMap.put("white", WHITE);
        sColorNameMap.put("red", RED);
        sColorNameMap.put("green", GREEN);
        sColorNameMap.put("blue", BLUE);
        sColorNameMap.put("yellow", YELLOW);
        sColorNameMap.put("cyan", CYAN);
        sColorNameMap.put("magenta", MAGENTA);
        sColorNameMap.put("aqua", 0xFF00FFFF);
        sColorNameMap.put("fuchsia", 0xFFFF00FF);
        sColorNameMap.put("darkgrey", DKGRAY);
        sColorNameMap.put("grey", GRAY);
        sColorNameMap.put("lightgrey", LTGRAY);
        sColorNameMap.put("lime", 0xFF00FF00);
        sColorNameMap.put("maroon", 0xFF800000);
        sColorNameMap.put("navy", 0xFF000080);
        sColorNameMap.put("olive", 0xFF808000);
        sColorNameMap.put("purple", 0xFF800080);
        sColorNameMap.put("silver", 0xFFC0C0C0);
        sColorNameMap.put("teal", 0xFF008080);

    }
}

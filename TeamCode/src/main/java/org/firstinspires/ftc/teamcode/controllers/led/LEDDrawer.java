package org.firstinspires.ftc.teamcode.controllers.led;

import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;

import java.util.Random;

/**
 * 模拟 REV Blinkin LED 驱动器的动画绘制器。
 * 支持所有 100 种模式，参数 ad1/ad2/brightness 实时可调。
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class LEDDrawer {

    // ---------- 可配置 ----------
    private final int numLeds;
    private Color[] leds;
    private Color color1 = Color.valueOf(Color.RED);
    private Color color2 = Color.valueOf(Color.BLUE);
    private long lastTime = 0;
    private float elapsedSeconds = 0;

    // 动画辅助状态
    private int lightChaseOffset = 0;
    private int endToEndPos = 0;

    // ---------- 调色板 ----------
    private static final Color[] RAINBOW_PALETTE = buildRainbowPalette();
    private static final Color[] PARTY_PALETTE   = buildPartyPalette();
    private static final Color[] OCEAN_PALETTE   = buildOceanPalette();
    private static final Color[] LAVA_PALETTE    = buildLavaPalette();
    private static final Color[] FOREST_PALETTE  = buildForestPalette();
    private static final Color[] HEAT_PALETTE    = buildHeatPalette();

    private final Random random = new Random();

    // ---------- 构造 ----------
    public LEDDrawer(int numLeds) {
        this.numLeds = numLeds;
        this.leds = new Color[numLeds];
        clear();
    }

    public void setColor1(Color c) { this.color1 = c; }
    public void setColor2(Color c) { this.color2 = c; }
    public Color[] getLeds() { return leds.clone(); }

    public void clear() {
        for (int i = 0; i < numLeds; i++) leds[i] = Color.valueOf(Color.BLACK);
    }

    // ---------- 主更新接口 ----------
    public void update(RevBlinkinLedDriver.BlinkinPattern pattern, int ad1, int ad2, int brightness) {
        long now = System.currentTimeMillis();
        if (lastTime == 0) lastTime = now;
        float delta = (now - lastTime) / 1000.0f;
        elapsedSeconds += delta;
        lastTime = now;

        switch (pattern) {
            // ========== FIXED PALETTE PATTERNS ==========
            case RAINBOW_RAINBOW_PALETTE:
                rainbowWithPalette(RAINBOW_PALETTE, ad1, ad2, brightness);
                break;
            case RAINBOW_PARTY_PALETTE:
                rainbowWithPalette(PARTY_PALETTE, ad1, ad2, brightness);
                break;
            case RAINBOW_OCEAN_PALETTE:
                rainbowWithPalette(OCEAN_PALETTE, ad1, ad2, brightness);
                break;
            case RAINBOW_LAVA_PALETTE:
                rainbowWithPalette(LAVA_PALETTE, ad1, ad2, brightness);
                break;
            case RAINBOW_FOREST_PALETTE:
                rainbowWithPalette(FOREST_PALETTE, ad1, ad2, brightness);
                break;
            case RAINBOW_WITH_GLITTER:
                rainbowWithPalette(RAINBOW_PALETTE, ad1, ad2, brightness);
                addGlitter(80, brightness);
                break;
            case CONFETTI:
                confetti(ad2, brightness);
                break;
            case SHOT_RED:
                shot(Color.valueOf(Color.RED), ad2, brightness);
                break;
            case SHOT_BLUE:
                shot(Color.valueOf(Color.BLUE), ad2, brightness);
                break;
            case SHOT_WHITE:
                shot(Color.valueOf(Color.WHITE), ad2, brightness);
                break;
            case SINELON_RAINBOW_PALETTE:
                sinelonWithPalette(RAINBOW_PALETTE, ad1, ad2, brightness);
                break;
            case SINELON_PARTY_PALETTE:
                sinelonWithPalette(PARTY_PALETTE, ad1, ad2, brightness);
                break;
            case SINELON_OCEAN_PALETTE:
                sinelonWithPalette(OCEAN_PALETTE, ad1, ad2, brightness);
                break;
            case SINELON_LAVA_PALETTE:
                sinelonWithPalette(LAVA_PALETTE, ad1, ad2, brightness);
                break;
            case SINELON_FOREST_PALETTE:
                sinelonWithPalette(FOREST_PALETTE, ad1, ad2, brightness);
                break;
            case BEATS_PER_MINUTE_RAINBOW_PALETTE:
                bpmWithPalette(RAINBOW_PALETTE, ad1, ad2, brightness);
                break;
            case BEATS_PER_MINUTE_PARTY_PALETTE:
                bpmWithPalette(PARTY_PALETTE, ad1, ad2, brightness);
                break;
            case BEATS_PER_MINUTE_OCEAN_PALETTE:
                bpmWithPalette(OCEAN_PALETTE, ad1, ad2, brightness);
                break;
            case BEATS_PER_MINUTE_LAVA_PALETTE:
                bpmWithPalette(LAVA_PALETTE, ad1, ad2, brightness);
                break;
            case BEATS_PER_MINUTE_FOREST_PALETTE:
                bpmWithPalette(FOREST_PALETTE, ad1, ad2, brightness);
                break;
            case FIRE_MEDIUM:
                fireEffect(128, ad2, brightness);
                break;
            case FIRE_LARGE:
                fireEffect(200, ad2, brightness);
                break;
            case TWINKLES_RAINBOW_PALETTE:
                twinklesWithPalette(RAINBOW_PALETTE, ad2, brightness);
                break;
            case TWINKLES_PARTY_PALETTE:
                twinklesWithPalette(PARTY_PALETTE, ad2, brightness);
                break;
            case TWINKLES_OCEAN_PALETTE:
                twinklesWithPalette(OCEAN_PALETTE, ad2, brightness);
                break;
            case TWINKLES_LAVA_PALETTE:
                twinklesWithPalette(LAVA_PALETTE, ad2, brightness);
                break;
            case TWINKLES_FOREST_PALETTE:
                twinklesWithPalette(FOREST_PALETTE, ad2, brightness);
                break;
            case COLOR_WAVES_RAINBOW_PALETTE:
                colorWavesWithPalette(RAINBOW_PALETTE, ad2, brightness);
                break;
            case COLOR_WAVES_PARTY_PALETTE:
                colorWavesWithPalette(PARTY_PALETTE, ad2, brightness);
                break;
            case COLOR_WAVES_OCEAN_PALETTE:
                colorWavesWithPalette(OCEAN_PALETTE, ad2, brightness);
                break;
            case COLOR_WAVES_LAVA_PALETTE:
                colorWavesWithPalette(LAVA_PALETTE, ad2, brightness);
                break;
            case COLOR_WAVES_FOREST_PALETTE:
                colorWavesWithPalette(FOREST_PALETTE, ad2, brightness);
                break;
            case LARSON_SCANNER_RED:
                larsonScanner(Color.valueOf(Color.RED), ad1, ad2, brightness);
                break;
            case LARSON_SCANNER_GRAY:
                larsonScanner(Color.valueOf(Color.GRAY), ad1, ad2, brightness);
                break;
            case LIGHT_CHASE_RED:
                lightChase(Color.valueOf(Color.RED), ad1, ad2, brightness);
                break;
            case LIGHT_CHASE_BLUE:
                lightChase(Color.valueOf(Color.BLUE), ad1, ad2, brightness);
                break;
            case LIGHT_CHASE_GRAY:
                lightChase(Color.valueOf(Color.GRAY), ad1, ad2, brightness);
                break;
            case HEARTBEAT_RED:
                heartbeat(Color.valueOf(Color.RED), ad2, brightness);
                break;
            case HEARTBEAT_BLUE:
                heartbeat(Color.valueOf(Color.BLUE), ad2, brightness);
                break;
            case HEARTBEAT_WHITE:
                heartbeat(Color.valueOf(Color.WHITE), ad2, brightness);
                break;
            case HEARTBEAT_GRAY:
                heartbeat(Color.valueOf(Color.GRAY), ad2, brightness);
                break;
            case BREATH_RED:
                breath(Color.valueOf(Color.RED), ad2, brightness);
                break;
            case BREATH_BLUE:
                breath(Color.valueOf(Color.BLUE), ad2, brightness);
                break;
            case BREATH_GRAY:
                breath(Color.valueOf(Color.GRAY), ad2, brightness);
                break;
            case STROBE_RED:
                strobe(Color.valueOf(Color.RED), ad2, brightness);
                break;
            case STROBE_BLUE:
                strobe(Color.valueOf(Color.BLUE), ad2, brightness);
                break;
            case STROBE_GOLD:
                strobe(Color.valueOf(0xFFFFD700), ad2, brightness);
                break;
            case STROBE_WHITE:
                strobe(Color.valueOf(Color.WHITE), ad2, brightness);
                break;

            // ========== CP1 ==========
            case CP1_END_TO_END_BLEND_TO_BLACK:
                endToEndBlendToBlack(color1, ad2, brightness);
                break;
            case CP1_LARSON_SCANNER:
                larsonScanner(color1, ad1, ad2, brightness);
                break;
            case CP1_LIGHT_CHASE:
                lightChase(color1, ad1, ad2, brightness);
                break;
            case CP1_HEARTBEAT_SLOW:
                heartbeatSpeed(color1, 0, brightness);
                break;
            case CP1_HEARTBEAT_MEDIUM:
                heartbeatSpeed(color1, 1, brightness);
                break;
            case CP1_HEARTBEAT_FAST:
                heartbeatSpeed(color1, 2, brightness);
                break;
            case CP1_BREATH_SLOW:
                breathSpeed(color1, 0, brightness);
                break;
            case CP1_BREATH_FAST:
                breathSpeed(color1, 1, brightness);
                break;
            case CP1_SHOT:
                shot(color1, ad2, brightness);
                break;
            case CP1_STROBE:
                strobe(color1, ad2, brightness);
                break;

            // ========== CP2 ==========
            case CP2_END_TO_END_BLEND_TO_BLACK:
                endToEndBlendToBlack(color2, ad2, brightness);
                break;
            case CP2_LARSON_SCANNER:
                larsonScanner(color2, ad1, ad2, brightness);
                break;
            case CP2_LIGHT_CHASE:
                lightChase(color2, ad1, ad2, brightness);
                break;
            case CP2_HEARTBEAT_SLOW:
                heartbeatSpeed(color2, 0, brightness);
                break;
            case CP2_HEARTBEAT_MEDIUM:
                heartbeatSpeed(color2, 1, brightness);
                break;
            case CP2_HEARTBEAT_FAST:
                heartbeatSpeed(color2, 2, brightness);
                break;
            case CP2_BREATH_SLOW:
                breathSpeed(color2, 0, brightness);
                break;
            case CP2_BREATH_FAST:
                breathSpeed(color2, 1, brightness);
                break;
            case CP2_SHOT:
                shot(color2, ad2, brightness);
                break;
            case CP2_STROBE:
                strobe(color2, ad2, brightness);
                break;

            // ========== CP1_2 ==========
            case CP1_2_SPARKLE_1_ON_2:
                sparkleTwoColor(color2, color1, ad2, brightness);
                break;
            case CP1_2_SPARKLE_2_ON_1:
                sparkleTwoColor(color1, color2, ad2, brightness);
                break;
            case CP1_2_COLOR_GRADIENT:
                colorGradient(color1, color2, brightness);
                break;
            case CP1_2_BEATS_PER_MINUTE:
                bpmTwoColor(color1, color2, ad1, ad2, brightness);
                break;
            case CP1_2_END_TO_END_BLEND_1_TO_2:
                endToEndBlendTwoColor(color1, color2, ad2, brightness);
                break;
            case CP1_2_END_TO_END_BLEND:
                endToEndBlendTwoColor(color1, color2, ad2, brightness);
                break;
            case CP1_2_NO_BLENDING:
                twoColorNoBlending(color1, color2, brightness);
                break;
            case CP1_2_TWINKLES:
                twinklesTwoColor(color1, color2, ad2, brightness);
                break;
            case CP1_2_COLOR_WAVES:
                colorWavesTwoColor(color1, color2, ad2, brightness);
                break;
            case CP1_2_SINELON:
                sinelonTwoColor(color1, color2, ad1, ad2, brightness);
                break;

            // ========== SOLID COLORS ==========
            case HOT_PINK:      fillSolid(Color.valueOf(0xFFFF69B4), brightness); break;
            case DARK_RED:      fillSolid(Color.valueOf(0xFF8B0000), brightness); break;
            case RED:           fillSolid(Color.valueOf(Color.RED), brightness); break;
            case RED_ORANGE:    fillSolid(Color.valueOf(0xFFFF4500), brightness); break;
            case ORANGE:        fillSolid(Color.valueOf(0xFFFFA500), brightness); break;      // 修正1
            case GOLD:          fillSolid(Color.valueOf(0xFFFFD700), brightness); break;
            case YELLOW:        fillSolid(Color.valueOf(Color.YELLOW), brightness); break;
            case LAWN_GREEN:    fillSolid(Color.valueOf(0xFF7CFC00), brightness); break;
            case LIME:          fillSolid(Color.valueOf(0xFF00FF00), brightness); break;
            case DARK_GREEN:    fillSolid(Color.valueOf(0xFF006400), brightness); break;
            case GREEN:         fillSolid(Color.valueOf(Color.GREEN), brightness); break;
            case BLUE_GREEN:    fillSolid(Color.valueOf(0xFF00FFFF), brightness); break;
            case AQUA:          fillSolid(Color.valueOf(0xFF00FFFF), brightness); break;
            case SKY_BLUE:      fillSolid(Color.valueOf(0xFF87CEEB), brightness); break;
            case DARK_BLUE:     fillSolid(Color.valueOf(0xFF00008B), brightness); break;
            case BLUE:          fillSolid(Color.valueOf(Color.BLUE), brightness); break;
            case BLUE_VIOLET:   fillSolid(Color.valueOf(0xFF8A2BE2), brightness); break;
            case VIOLET:        fillSolid(Color.valueOf(0xFFEE82EE), brightness); break;
            case WHITE:         fillSolid(Color.valueOf(Color.WHITE), brightness); break;
            case GRAY:          fillSolid(Color.valueOf(Color.GRAY), brightness); break;
            case DARK_GRAY:     fillSolid(Color.valueOf(Color.DKGRAY), brightness); break;  // 修正2
            case BLACK:         fillSolid(Color.valueOf(Color.BLACK), brightness); break;

            default:
                rainbowWithPalette(RAINBOW_PALETTE, 128, 128, brightness);
                break;
        }
    }

    // =========================================================================
    // 核心动画函数
    // =========================================================================

    private void applyBrightness(Color[] arr, int brightness) {
        float factor = brightness / 255f;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                float r = arr[i].red() * factor;
                float g = arr[i].green() * factor;
                float b = arr[i].blue() * factor;
                float a = arr[i].alpha();
                arr[i] = Color.valueOf(Math.min(1.0f, r), Math.min(1.0f, g), Math.min(1.0f, b), a);
            }
        }
    }

    private Color scaleColor(Color c, float factor) {
        return Color.valueOf(
                Math.min(1.0f, c.red() * factor),
                Math.min(1.0f, c.green() * factor),
                Math.min(1.0f, c.blue() * factor),
                c.alpha()
        );
    }

    private Color blend(Color a, Color b, float t) {
        if (t <= 0) return a;
        if (t >= 1) return b;
        return Color.valueOf(
                a.red() * (1 - t) + b.red() * t,
                a.green() * (1 - t) + b.green() * t,
                a.blue() * (1 - t) + b.blue() * t,
                a.alpha() * (1 - t) + b.alpha() * t
        );
    }

    // ---- beatsin8 重载（修正3） ----
    private int beatsin8(float bpm, int low, int high) {
        return beatsin8(bpm, low, high, 0, 0);
    }

    private int beatsin8(float bpm, int low, int high, int offset) {
        return beatsin8(bpm, low, high, offset, 0);
    }

    private int beatsin8(float bpm, int low, int high, int offset, int phase) {
        float period = 60.0f / bpm;
        float angle = (elapsedSeconds + offset) % period / period * 2 * (float) Math.PI;
        angle += phase * Math.PI / 180.0f;
        float sinVal = (float) Math.sin(angle);
        return low + (int) ((sinVal + 1) / 2 * (high - low));
    }

    private int beatsin16(float bpm, int low, int high) {
        return beatsin8(bpm, low, high);
    }

    // ---- 调色板 ----
    private Color colorFromPalette(Color[] palette, int index, int brightness) {
        int idx = index & 0xFF;
        Color c = palette[idx];
        return scaleColor(c, brightness / 255f);
    }

    private void fillPalette(Color[] palette, int startIndex, int step, int brightness) {
        for (int i = 0; i < numLeds; i++) {
            int idx = (startIndex + i * step) & 0xFF;
            leds[i] = colorFromPalette(palette, idx, brightness);
        }
    }

    private void fadeToBlackBy(int amount) {
        float factor = 1.0f - Math.min(amount, 255) / 255f;
        for (int i = 0; i < numLeds; i++) {
            if (leds[i] != null) {
                leds[i] = Color.valueOf(
                        leds[i].red() * factor,
                        leds[i].green() * factor,
                        leds[i].blue() * factor,
                        leds[i].alpha()
                );
            }
        }
    }

    // ===== 具体模式实现 =====

    private void rainbowWithPalette(Color[] palette, int density, int speed, int brightness) {
        int beat = beatsin8(speedToBeats(speed), 0, 255);
        fillPalette(palette, beat, density, brightness);
    }

    private void confetti(int speed, int brightness) {
        fadeToBlackBy(speedToDelay(speed) / 2 + 1);
        int pos = random.nextInt(numLeds);
        float hue = random.nextFloat() * 360;
        float sat = 1.0f;
        float val = brightness / 255f;
        int argb = Color.HSVToColor(0xFF, new float[]{hue, sat, val});
        leds[pos] = Color.valueOf(argb);
    }

    private void shot(Color color, int speed, int brightness) {
        fadeToBlackBy(map(speed, 0, 255, 1, 40));
        if (random.nextInt(100) < 20) {
            for (int i = 0; i < numLeds; i++) leds[i] = color;
            applyBrightness(leds, brightness);
        }
    }

    private void sinelonWithPalette(Color[] palette, int density, int speed, int brightness) {
        fadeToBlackBy(map(speed, 0, 255, 1, 30));
        int pos = beatsin16(speedToBeats(speed), 0, numLeds - 1);
        Color c = colorFromPalette(palette, (int) (elapsedSeconds * 10) % 256, brightness);
        leds[pos] = c;
        int width = map(density, 0, 255, 1, 8);
        for (int i = 1; i <= width; i++) {
            if (pos + i < numLeds) leds[pos + i] = scaleColor(c, 1.0f / (i + 1));
            if (pos - i >= 0) leds[pos - i] = scaleColor(c, 1.0f / (i + 1));
        }
        applyBrightness(leds, brightness);
    }

    private void bpmWithPalette(Color[] palette, int density, int speed, int brightness) {
        int beat = beatsin8(speedToBeats(speed), 64, 255);
        Color color = colorFromPalette(palette, (int) (elapsedSeconds * 20) % 256, brightness);
        int numLit = map(density, 0, 255, 1, numLeds);
        fadeToBlackBy(20);
        if (beat > 240) {
            for (int i = 0; i < numLit; i++) {
                leds[random.nextInt(numLeds)] = color;
            }
        }
        applyBrightness(leds, brightness);
    }

    private void fireEffect(int size, int speed, int brightness) {
        int cooling = map(speed, 0, 255, 20, 100);
        int sparking = map(size, 0, 255, 30, 120);

        for (int i = 0; i < numLeds; i++) {
            float r = leds[i].red();
            float g = leds[i].green();
            float b = leds[i].blue();
            int fade = random.nextInt(cooling);
            r = Math.max(0, r - fade / 255f);
            g = Math.max(0, g - fade / 255f);
            b = Math.max(0, b - fade / 255f);
            leds[i] = Color.valueOf(r, g, b, leds[i].alpha());
        }

        for (int k = numLeds - 1; k >= 2; k--) {
            Color c1 = leds[k - 1];
            Color c2 = leds[k - 2];
            float r = (c1.red() + c2.red() + c2.red()) / 3;
            float g = (c1.green() + c2.green() + c2.green()) / 3;
            float b = (c1.blue() + c2.blue() + c2.blue()) / 3;
            leds[k] = Color.valueOf(r, g, b, leds[k].alpha());
        }

        if (random.nextInt(100) < sparking) {
            int y = random.nextInt(3);
            leds[y] = Color.valueOf(
                    (160 + random.nextInt(96)) / 255f,
                    random.nextInt(80) / 255f,
                    0,
                    leds[y].alpha()
            );
        }

        for (int i = 0; i < numLeds; i++) {
            int idx = (int) (leds[i].red() * 255);
            idx = Math.min(240, idx);
            leds[i] = colorFromPalette(HEAT_PALETTE, idx, brightness);
        }
    }

    private void twinklesWithPalette(Color[] palette, int speed, int brightness) {
        int fadeAmount = map(speed, 0, 255, 5, 40);
        fadeToBlackBy(fadeAmount);
        if (random.nextInt(100) < map(speed, 0, 255, 10, 60)) {
            int idx = random.nextInt(numLeds);
            leds[idx] = colorFromPalette(palette, random.nextInt(256), brightness);
        }
        applyBrightness(leds, brightness);
    }

    private void colorWavesWithPalette(Color[] palette, int speed, int brightness) {
        int beat = beatsin8(speedToBeats(speed), 0, 255);
        for (int i = 0; i < numLeds; i++) {
            int idx = (beat + i * 3) & 0xFF;
            leds[i] = colorFromPalette(palette, idx, brightness);
        }
    }

    private void larsonScanner(Color color, int width, int speed, int brightness) {
        fadeToBlackBy(map(speed, 0, 255, 1, 25));
        int pos = beatsin16(speedToBeats(speed), 0, numLeds - 1);
        int halfW = width / 2;
        for (int i = pos - halfW; i <= pos + halfW; i++) {
            if (i >= 0 && i < numLeds) {
                int dist = Math.abs(i - pos);
                float bright = (dist < halfW) ? 1.0f - (float) dist / (halfW + 1) : 0;
                leds[i] = scaleColor(color, bright);
            }
        }
        applyBrightness(leds, brightness);
    }

    private void lightChase(Color color, int dimming, int speed, int brightness) {
        int spacing = map(dimming, 0, 255, 2, 8);
        int advanceRate = map(speed, 0, 255, 1, 20);
        lightChaseOffset = (lightChaseOffset + advanceRate) % spacing;

        fadeToBlackBy(map(speed, 0, 255, 5, 50));
        for (int i = lightChaseOffset; i < numLeds; i += spacing) {
            leds[i] = color;
        }
        applyBrightness(leds, brightness);
    }

    private void heartbeat(Color color, int speed, int brightness) {
        int bpm = speedToBeats(speed);
        int beat = beatsin8(bpm, 0, 255);
        int brightVal;
        if (beat < 40) brightVal = 0;
        else if (beat < 80) brightVal = map(beat, 40, 80, 0, 255);
        else if (beat < 100) brightVal = map(beat, 80, 100, 255, 50);
        else if (beat < 140) brightVal = map(beat, 100, 140, 50, 255);
        else if (beat < 160) brightVal = map(beat, 140, 160, 255, 0);
        else brightVal = 0;
        fillSolid(color, brightVal);
        applyBrightness(leds, brightness);
    }

    private void breath(Color color, int speed, int brightness) {
        int brightVal = beatsin8(speedToBeats(speed), 10, 255);
        fillSolid(color, brightVal);
        applyBrightness(leds, brightness);
    }

    private void strobe(Color color, int speed, int brightness) {
        int beat = beatsin8(speedToBeats(speed) * 2, 0, 255);
        if (beat > 200) fillSolid(color, brightness);
        else fillSolid(Color.valueOf(Color.BLACK), 0);
        applyBrightness(leds, brightness);
    }

    private void endToEndBlendToBlack(Color color, int speed, int brightness) {
        endToEndPos = (endToEndPos + 1) % numLeds;
        fadeToBlackBy(20);
        leds[endToEndPos] = color;
        applyBrightness(leds, brightness);
    }

    private void heartbeatSpeed(Color color, int preset, int brightness) {
        int bpm;
        switch (preset) {
            case 0: bpm = 30; break;
            case 1: bpm = 60; break;
            case 2: bpm = 90; break;
            default: bpm = 60;
        }
        int beat = beatsin8(bpm, 0, 255);
        int brightVal;
        if (beat < 40) brightVal = 0;
        else if (beat < 80) brightVal = map(beat, 40, 80, 0, 255);
        else if (beat < 100) brightVal = map(beat, 80, 100, 255, 50);
        else if (beat < 140) brightVal = map(beat, 100, 140, 50, 255);
        else if (beat < 160) brightVal = map(beat, 140, 160, 255, 0);
        else brightVal = 0;
        fillSolid(color, brightVal);
        applyBrightness(leds, brightness);
    }

    private void breathSpeed(Color color, int preset, int brightness) {
        int bpm = (preset == 0) ? 30 : 80;
        int brightVal = beatsin8(bpm, 10, 255);
        fillSolid(color, brightVal);
        applyBrightness(leds, brightness);
    }

    private void sparkleTwoColor(Color bg, Color fg, int speed, int brightness) {
        fillSolid(bg, brightness);
        if (random.nextInt(100) < map(speed, 0, 255, 10, 50)) {
            leds[random.nextInt(numLeds)] = fg;
        }
        applyBrightness(leds, brightness);
    }

    private void colorGradient(Color c1, Color c2, int brightness) {
        for (int i = 0; i < numLeds; i++) {
            float t = (float) i / (numLeds - 1);
            leds[i] = blend(c1, c2, t);
        }
        applyBrightness(leds, brightness);
    }

    private void bpmTwoColor(Color c1, Color c2, int density, int speed, int brightness) {
        int beat = beatsin8(speedToBeats(speed), 64, 255);
        int numLit = map(density, 0, 255, 1, numLeds);
        fadeToBlackBy(20);
        if (beat > 240) {
            for (int i = 0; i < numLit; i++) {
                leds[random.nextInt(numLeds)] = random.nextBoolean() ? c1 : c2;
            }
        }
        applyBrightness(leds, brightness);
    }

    private void endToEndBlendTwoColor(Color c1, Color c2, int speed, int brightness) {
        endToEndPos = (endToEndPos + 1) % numLeds;
        fadeToBlackBy(15);
        float t = (float) endToEndPos / (numLeds - 1);
        leds[endToEndPos] = blend(c1, c2, t);
        applyBrightness(leds, brightness);
    }

    private void twoColorNoBlending(Color c1, Color c2, int brightness) {
        for (int i = 0; i < numLeds; i++) {
            leds[i] = (i % 2 == 0) ? c1 : c2;
        }
        applyBrightness(leds, brightness);
    }

    private void twinklesTwoColor(Color c1, Color c2, int speed, int brightness) {
        fadeToBlackBy(map(speed, 0, 255, 5, 40));
        if (random.nextInt(100) < map(speed, 0, 255, 10, 60)) {
            int idx = random.nextInt(numLeds);
            leds[idx] = random.nextBoolean() ? c1 : c2;
        }
        applyBrightness(leds, brightness);
    }

    private void colorWavesTwoColor(Color c1, Color c2, int speed, int brightness) {
        int beat = beatsin8(speedToBeats(speed), 0, 255);
        for (int i = 0; i < numLeds; i++) {
            // 修正4：使用5参数重载，phase 参数为 i*8
            float t = (float) beatsin8(6, 0, 255, 0, i * 8) / 255f;
            Color c = blend(c1, c2, t);
            int bright = beatsin8(speedToBeats(speed), 128, 255, 0, i * 4);
            leds[i] = scaleColor(c, bright / 255f);
        }
        applyBrightness(leds, brightness);
    }

    private void sinelonTwoColor(Color c1, Color c2, int density, int speed, int brightness) {
        fadeToBlackBy(map(speed, 0, 255, 1, 30));
        int pos = beatsin16(speedToBeats(speed), 0, numLeds - 1);
        float t = (float) pos / (numLeds - 1);
        Color color = blend(c1, c2, t);
        leds[pos] = color;
        int width = map(density, 0, 255, 1, 8);
        for (int i = 1; i <= width; i++) {
            if (pos + i < numLeds) leds[pos + i] = scaleColor(color, 1.0f / (i + 1));
            if (pos - i >= 0) leds[pos - i] = scaleColor(color, 1.0f / (i + 1));
        }
        applyBrightness(leds, brightness);
    }

    private void fillSolid(Color color, int brightness) {
        Color scaled = scaleColor(color, brightness / 255f);
        for (int i = 0; i < numLeds; i++) leds[i] = scaled;
    }

    private void addGlitter(int chance, int brightness) {
        if (random.nextInt(100) < chance) {
            int idx = random.nextInt(numLeds);
            leds[idx] = Color.valueOf(Color.WHITE);
        }
        applyBrightness(leds, brightness);
    }

    // ---- 辅助映射 ----
    private int map(int x, int inMin, int inMax, int outMin, int outMax) {
        if (inMax == inMin) return outMin;
        return outMin + (x - inMin) * (outMax - outMin) / (inMax - inMin);
    }

    private int speedToBeats(int speed) {
        return map(speed, 0, 255, 1, 120);
    }

    private int speedToDelay(int speed) {
        return map(speed, 0, 255, 200, 5);
    }

    // ---------- 调色板构建（修正5：使用 HSVToColor） ----------
    private static Color[] buildRainbowPalette() {
        Color[] p = new Color[256];
        for (int i = 0; i < 256; i++) {
            float hue = i / 256f * 360;
            float sat = 1.0f;
            float val = 1.0f;
            int argb = Color.HSVToColor(0xFF, new float[]{hue, sat, val});
            p[i] = Color.valueOf(argb);
        }
        return p;
    }

    private static Color[] buildPartyPalette() {
        Color[] p = new Color[256];
        for (int i = 0; i < 256; i++) {
            float hue = (i % 64) / 64f * 0.8f * 360 + 36; // 偏移使颜色丰富
            float sat = 0.9f;
            float val = 1.0f;
            int argb = Color.HSVToColor(0xFF, new float[]{hue, sat, val});
            p[i] = Color.valueOf(argb);
        }
        return p;
    }

    private static Color[] buildOceanPalette() {
        Color[] p = new Color[256];
        for (int i = 0; i < 256; i++) {
            float hue = 180 + (i / 256f) * 72; // 180~252° 蓝绿
            float sat = 0.8f;
            float val = 1.0f;
            int argb = Color.HSVToColor(0xFF, new float[]{hue, sat, val});
            p[i] = Color.valueOf(argb);
        }
        return p;
    }

    private static Color[] buildLavaPalette() {
        Color[] p = new Color[256];
        for (int i = 0; i < 256; i++) {
            float hue = (i / 256f) * 36; // 0~36° 红橙
            float sat = 1.0f;
            float val = 1.0f;
            int argb = Color.HSVToColor(0xFF, new float[]{hue, sat, val});
            p[i] = Color.valueOf(argb);
        }
        return p;
    }

    private static Color[] buildForestPalette() {
        Color[] p = new Color[256];
        for (int i = 0; i < 256; i++) {
            float hue = 72 + (i / 256f) * 54; // 72~126° 绿
            float sat = 0.9f;
            float val = 1.0f;
            int argb = Color.HSVToColor(0xFF, new float[]{hue, sat, val});
            p[i] = Color.valueOf(argb);
        }
        return p;
    }

    private static Color[] buildHeatPalette() {
        Color[] p = new Color[256];
        for (int i = 0; i < 256; i++) {
            float t = i / 255f;
            float r, g, b;
            if (t < 0.2f) {
                r = 0; g = 0; b = 0;
            } else if (t < 0.4f) {
                r = (t - 0.2f) / 0.2f;
                g = 0; b = 0;
            } else if (t < 0.6f) {
                r = 1.0f;
                g = (t - 0.4f) / 0.2f;
                b = 0;
            } else if (t < 0.8f) {
                r = 1.0f;
                g = 1.0f;
                b = (t - 0.6f) / 0.2f;
            } else {
                r = 1.0f; g = 1.0f; b = 1.0f;
            }
            p[i] = Color.valueOf(r, g, b);
        }
        return p;
    }
}
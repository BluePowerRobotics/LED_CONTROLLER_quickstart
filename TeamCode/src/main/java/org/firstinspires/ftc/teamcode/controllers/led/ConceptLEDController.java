package org.firstinspires.ftc.teamcode.controllers.led;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controllers.uart.UsbUart;

/**
 * 演示 LEDController 的使用方法。
 * 可通过 FTC Dashboard 实时调整参数，也可用手柄切换模式。
 *
 * @see LEDController
 * @see RevBlinkinLedDriver.BlinkinPattern
 */
@TeleOp(name = "Concept: LED Control", group = "Concept")
@Config
public class ConceptLEDController extends LinearOpMode {

    // ---------- Dashboard 可调参数 ----------
    public static int patternIndex = 0;               // 0 ~ 99
    public static int color1Red   = 255, color1Green = 0,   color1Blue  = 0;   // 自定义颜色1 (用于 CP1/CP1_2 模式)
    public static int color2Red   = 0,   color2Green = 0,   color2Blue  = 255; // 自定义颜色2 (用于 CP2/CP1_2 模式)
    public static int ad1         = 128;               // 0~255，影响动画密度/宽度等
    public static int ad2         = 128;               // 0~255，影响动画速度/频率等
    public static double brightness = 1.0;            // 0.0 ~ 1.0，全局亮度
    public static int index       = 1;

    // ---------- 内部状态 ----------
    private LEDController ledController;
    private RevBlinkinLedDriver.BlinkinPattern[] patterns;
    private int lastPatternIndex = -1;
    private Color lastColor1 = null;
    private Color lastColor2 = null;
    private int lastAd1 = -1, lastAd2 = -1;
    private double lastBrightness = -1.0;

    // 手柄防抖
    private boolean lastA = false, lastB = false;

    @SuppressLint("DefaultLocale")
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
            ledController = new LEDController(UsbUart.getAllUsbDevices().get(0).getDeviceName());
            //ledController.ledAgreement = new LEDAgreement(UsbUart.getAllUsbDevices().get(0).getDeviceName());

            // 初始设置
            ledController.setColor1(Color.valueOf(color1Red / 255f, color1Green / 255f, color1Blue / 255f));
            ledController.setColor2(Color.valueOf(color2Red / 255f, color2Green / 255f, color2Blue / 255f));
            ledController.setAd1(ad1);
            ledController.setAd2(ad2);
            ledController.setGlobalBrightness(brightness);

            patterns = RevBlinkinLedDriver.BlinkinPattern.values();
            setPatternToAllPorts(patterns[patternIndex]);

            // 记录初始状态，用于检测 Dashboard 变化
            lastPatternIndex = patternIndex;
            lastColor1 = ledController.getColor1();
            lastColor2 = ledController.getColor2();
            lastAd1 = ad1;
            lastAd2 = ad2;
            lastBrightness = brightness;

            telemetry.addData("Status", "LED Controller initialized");
//            telemetry.addData("Error", "Init failed: " + e.getMessage());
            telemetry.update();

        waitForStart();

        // ---------- 主循环 ----------
        while (opModeIsActive()) {
            if (gamepad1.aWasReleased()) {
                patternIndex = (patternIndex + 1) % patterns.length;
            }
            if (gamepad1.bWasReleased()) {
                patternIndex = (patternIndex - 1 + patterns.length) % patterns.length;
            }

            // 2. 检测 Dashboard 参数变化并应用
            boolean changed = false;

            if (patternIndex != lastPatternIndex) {
                // 限制范围
                patternIndex = Math.max(0, Math.min(patternIndex, patterns.length - 1));
                setPatternToAllPorts(patterns[patternIndex]);
                lastPatternIndex = patternIndex;
                changed = true;
            }

            Color newColor1 = Color.valueOf(color1Red / 255f, color1Green / 255f, color1Blue / 255f);
            if (!newColor1.equals(lastColor1)) {
                ledController.setColor1(newColor1);
                lastColor1 = newColor1;
                changed = true;
            }

            Color newColor2 = Color.valueOf(color2Red / 255f, color2Green / 255f, color2Blue / 255f);
            if (!newColor2.equals(lastColor2)) {
                ledController.setColor2(newColor2);
                lastColor2 = newColor2;
                changed = true;
            }

            if (ad1 != lastAd1) {
                ledController.setAd1(ad1);
                lastAd1 = ad1;
                changed = true;
            }
            if (ad2 != lastAd2) {
                ledController.setAd2(ad2);
                lastAd2 = ad2;
                changed = true;
            }
            if (brightness != lastBrightness) {
                ledController.setGlobalBrightness(brightness);
                lastBrightness = brightness;
                changed = true;
            }
            // 3. 输出调试信息
            telemetry.addData("Pattern", patterns[patternIndex].name());
            telemetry.addData("Color1", String.format("#%02X%02X%02X", color1Red, color1Green, color1Blue));
            telemetry.addData("Color2", String.format("#%02X%02X%02X", color2Red, color2Green, color2Blue));
            telemetry.addData("AD1", ad1);
            telemetry.addData("AD2", ad2);
            telemetry.addData("Brightness", String.format("%.2f", brightness));
            telemetry.addData("Changed", changed);
            telemetry.update();

            // 4. 释放 CPU
            sleep(20);
        }

        // ---------- 清理 ----------
        if (ledController != null) {
            ledController.close();
        }
    }

    /**
     * 将相同模式应用到所有 7 个端口。
     */
    private void setPatternToAllPorts(RevBlinkinLedDriver.BlinkinPattern pattern) {
        if (ledController == null) return;
        for (int port = 0; port < 7; port++) {
            ledController.setPortPattern(port, pattern);
        }
    }
}
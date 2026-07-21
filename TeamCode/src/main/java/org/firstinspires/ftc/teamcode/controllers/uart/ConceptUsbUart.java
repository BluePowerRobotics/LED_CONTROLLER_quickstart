package org.firstinspires.ftc.teamcode.controllers.uart;

import android.hardware.usb.UsbDevice;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@TeleOp(name = "Concept: UsbUart", group = "Concept")
//@Disabled
@Config
public class ConceptUsbUart extends LinearOpMode {
    
    public static int baudRate = 115200;        // 波特率
    public static int dataBits = 8;             // 数据位 (5,6,7,8)
    public static int stopBits = 1;             // 停止位 (1,2)
    public static int parity = 0;               // 校验位: 0=无, 1=奇, 2=偶
    public static boolean flowControl = false;  // 流控制
    public static int serialNumber = 0;         // 使用的串口号
    public static String message = "gDAT:TIME;";

    private int selectedIndex = 0;
    private ArrayList<UsbDevice> deviceList = new ArrayList<>();
    private boolean agreementActive = false;
    private UsbUart usbUart = null;
    private SimpleUartAgreement agreement = null;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        while (opModeInInit() || opModeIsActive()) {

            if (!agreementActive) {
                //select device
                deviceList = UsbUart.getAllUsbDevices();
                if (deviceList == null) {
                    deviceList = new ArrayList<>();
                }

                if (!deviceList.isEmpty()) {
                    if (selectedIndex >= deviceList.size()) {
                        selectedIndex = deviceList.size() - 1;
                    }
                    if (selectedIndex < 0) {
                        selectedIndex = 0;
                    }
                } else {
                    selectedIndex = 0;
                }

                if (gamepad1.dpad_up) {
                    selectedIndex = Math.max(0, selectedIndex - 1);
                }
                if (gamepad1.dpad_down) {
                    if (!deviceList.isEmpty()) {
                        selectedIndex = Math.min(deviceList.size() - 1, selectedIndex + 1);
                    }
                }

                if ((gamepad1.y||opModeIsActive()) && !deviceList.isEmpty()) {
                    UsbDevice selectedDevice = deviceList.get(selectedIndex);
                    usbUart = new UsbUart(selectedDevice.getDeviceName(), serialNumber);
                    UsbUart.SerialParameters params = usbUart.getSerialParametersBuilder()
                            .setBaudRate(baudRate)
                            .setDataBits(dataBits)
                            .setStopBits(stopBits)
                            .setParity(parity)
                            .setFlowControl(flowControl)
                            .build();
                    agreement = new SimpleUartAgreement(usbUart, params);
                    agreementActive = true;
                }

                telemetry.addLine("Select USB Device (dpad up/down, Y to connect):");
                if (deviceList.isEmpty()) {
                    telemetry.addLine("No USB devices found.");
                } else {
                    for (int i = 0; i < deviceList.size(); i++) {
                        String prefix = (i == selectedIndex) ? "> " : "  ";
                        telemetry.addLine(prefix + deviceList.get(i).getDeviceName());
                    }
                }

            } else {
                //device selected

                if (gamepad1.x && agreement != null) {
                    agreement.usbUart.writeData(message.getBytes(StandardCharsets.UTF_8));
                }

                Map<String, List<String>> allValues = agreement != null ? agreement.getAllReceivedValues() : Collections.emptyMap();
                telemetry.addLine("--- Received Data ---");
                if (allValues.isEmpty()) {
                    telemetry.addLine("(no data)");
                } else {
                    for (Map.Entry<String, List<String>> entry : allValues.entrySet()) {
                        String key = entry.getKey();
                        List<String> values = entry.getValue();
                        telemetry.addLine(key + ": " + values.toString());
                    }
                }
                telemetry.addLine("Press X to send: " + message);
            }

            telemetry.update();
        }

        //clear uart after opmode ends
        if (agreement != null) {
            agreement.close();
        } else if (usbUart != null) {
            usbUart.disconnect();
        }
    }
}
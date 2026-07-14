package org.firstinspires.ftc.teamcode.controllers.led;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.teamcode.controllers.uart.SimpleUartAgreement;
import org.firstinspires.ftc.teamcode.controllers.uart.UsbUart;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wch.uartlib.chip.type.ChipType2;

@Config
public class LEDAgreement extends SimpleUartAgreement {
    public static int WAITING_SLEEP_MS = 5;
    public static Map<String, Integer> possibleValueNumber = Map.ofEntries(
            Map.entry("TIME",1),
            Map.entry("GLOBAL_BRIGHTNESS", 1),
            Map.entry("PORT_LED_NUMBER",7),
            Map.entry("PORT_PATTERN",7),
            Map.entry("LED_RGB",5)
    );
    public static int TIMEOUT_MS = 100;

    /**
     * Note that the path of the same device on the same port changes each time you plug it.
     * @param devicePath the device path in usbfs file system
     */
    @Deprecated
    public LEDAgreement(String devicePath){
        this(LEDAgreement.createUsbUartAndParams(new UsbUart(devicePath)));
        customizeAgreement();
    }
    public LEDAgreement() {
        this(LEDAgreement.createUsbUartAndParams(new UsbUart(ChipType2.CHIP_CH341)));
        customizeAgreement();
    }
    private LEDAgreement(UsbUartAndParams usbUartAndParams) {
        super(usbUartAndParams.uart, usbUartAndParams.params);
    }
    private static UsbUartAndParams createUsbUartAndParams(UsbUart usbUart) {
        UsbUart.SerialParameters params = usbUart.getSerialParametersBuilder()
                .setBaudRate(115200)
                .build();
        return new UsbUartAndParams(usbUart, params);
    }
    private void customizeAgreement(){
        possibleReceivingMessageTypeAndCode = new String[]{"rDAT","rSTG"};
        generalValueLength = 2;
    }
    private static class UsbUartAndParams {
        final UsbUart uart;
        final UsbUart.SerialParameters params;

        UsbUartAndParams(UsbUart uart, UsbUart.SerialParameters params) {
            this.uart = uart;
            this.params = params;
        }
    }
    public static class LEDCommand extends SimpleUartAgreement.CommandImpl{

        public LEDCommand(char commandType, String commandCode, int argumentNumber) {
            super(commandType, commandCode, argumentNumber);
        }

        public static Command getLEDCommand(LEDCommandList LEDCommandList) {
            return new LEDCommand(LEDCommandList.getCommandType(), LEDCommandList.getCommandCode(), LEDCommandList.getArgumentNumber());
        }
        public enum LEDCommandList {
            APPLY_SETTINGS('s',"APS",0),
            SET_UART_BAUD_RATE('s',"UBR",1),
            SET_GLOBAL_BRIGHTNESS('s',"GBN",1),
            SET_PORT_LED_NUMBER('s',"PLN",2),
            SET_PORT_PATTERN('s',"PPT",2),
            SET_RANGE_RGB('s', "RNG", 6),
            SET_SINGLE_RGB('s', "RGB", 5),
            GET_SETTING('g', "STG", 1),
            GET_DATA('g', "DAT", 1);
            private final char commandType;
            private final String commandCode;
            private final int argumentNumber;

            LEDCommandList(char commandType, String commandCode, int argumentNumber) {
                this.commandType = commandType;
                this.commandCode = commandCode;
                this.argumentNumber = argumentNumber;
            }

            public char getCommandType() {
                return commandType;
            }

            public String getCommandCode() {
                return commandCode;
            }

            public int getArgumentNumber() {
                return argumentNumber;
            }
        }
    }

    public List<String> askForSettingValue(String valueName) {
        sendCommand(LEDCommand.getLEDCommand(LEDCommand.LEDCommandList.GET_SETTING),valueName);
        long startTime = System.currentTimeMillis();
        int requiredTime =(int)estimateSendMS(getArgNumOfValue(valueName));
        try{
            Thread.sleep(requiredTime);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        List<String> result = null;
        while(System.currentTimeMillis()<startTime+requiredTime+TIMEOUT_MS){
            result = getReceivedValue(valueName);
            if(result != null)
                return result;
            try{
                Thread.sleep(WAITING_SLEEP_MS);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }
    public List<String> askForDataValue(String valueName){
        sendCommand(LEDCommand.getLEDCommand(LEDCommand.LEDCommandList.GET_DATA),valueName);
        long startTime = System.currentTimeMillis();
        int requiredTime =(int)estimateSendMS(getArgNumOfValue(valueName));
        try{
            Thread.sleep(requiredTime);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        List<String> result = null;
        while(System.currentTimeMillis()<startTime+requiredTime+TIMEOUT_MS){
            result = removeReceivedValue(valueName);
            if(result != null)
                return result;
            try{
                Thread.sleep(WAITING_SLEEP_MS);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }
    public static int getArgNumOfValue(String valueName){
        Integer result = possibleValueNumber.get(valueName);
        return result == null ? 0 : result;
    }
}

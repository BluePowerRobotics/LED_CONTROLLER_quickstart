package org.firstinspires.ftc.teamcode.controllers.uart;

import android.hardware.usb.UsbDevice;

import com.qualcomm.robotcore.util.RobotLog;

import java.util.ArrayList;

import cn.wch.uartlib.WCHUARTManager;
import cn.wch.uartlib.callback.IDataCallback;
import cn.wch.uartlib.chip.type.ChipType2;

public class UsbUart {
    public static int TIMEOUT = 100;
    private UsbDevice usedUsbDevice = null;
    private int serialNumber = 0;
    private SerialParameters serialParameters = null;
    public  UsbUart(ChipType2 chipType) {
        ArrayList<UsbDevice> usbDevices = null;
        try{
            usbDevices = WCHUARTManager.getInstance().enumDevice();
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        ArrayList<UsbDevice> matchedDevices = new ArrayList<>();
        if (usbDevices != null) {
            for (UsbDevice usbDevice : usbDevices) {
                boolean isMatch = chipType.equals(WCHUARTManager.getInstance().getChipType(usbDevice));
                if (isMatch) matchedDevices.add(usbDevice);
            }
        }
        if (matchedDevices.isEmpty()) {
            RobotLog.addGlobalWarningMessage("Found " + matchedDevices.size() + " USB devices of type " + chipType + ", nothing will be done");
            usedUsbDevice = null;
            return;
        }else if(matchedDevices.size() > 1) {
            RobotLog.addGlobalWarningMessage("Found " + matchedDevices.size() + " USB devices of type " + chipType + ", using first of them");
        }
        usedUsbDevice = matchedDevices.get(0);
    }
    public UsbUart(String DeviceName){
        ArrayList<UsbDevice> usbDevices = null;
        try{
            usbDevices = WCHUARTManager.getInstance().enumDevice();
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        ArrayList<UsbDevice> matchedDevices = new ArrayList<>();
        if (usbDevices != null) {
            for (UsbDevice usbDevice : usbDevices) {
                boolean isMatch = DeviceName.equals(usbDevice.getDeviceName());
                if (isMatch) matchedDevices.add(usbDevice);
            }
        }
        if (matchedDevices.isEmpty()) {
            RobotLog.addGlobalWarningMessage("Found " + matchedDevices.size() + " USB devices of name " + DeviceName + ", nothing will be done");
            usedUsbDevice = null;
            return;
        }else if(matchedDevices.size() > 1) {
            RobotLog.addGlobalWarningMessage("Found " + matchedDevices.size() + " USB devices of name " + DeviceName + ", using first of them");
        }
        usedUsbDevice = matchedDevices.get(0);
    }
    public UsbUart(ChipType2 chipType, int serialNumber){
        this(chipType);
        this.serialNumber = serialNumber;
    }
    public UsbUart(String DeviceName, int serialNumber){
        this(DeviceName);
        this.serialNumber = serialNumber;
    }

    public UsbDevice getUsedUsbDevice() {
        return usedUsbDevice;
    }
    public static ArrayList<UsbDevice> getAllUsbDevices() {
        ArrayList<UsbDevice> usbDevices = null;
        try{
            usbDevices = WCHUARTManager.getInstance().enumDevice();
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        return usbDevices;
    }
    public boolean setSerialParameters(SerialParameters serialParameters){
        if(usedUsbDevice == null) return false;
        if(!isConnected()) connect();
        boolean result = true;
        try {
            result = WCHUARTManager.getInstance().setSerialParameter(serialParameters.getUsbDevice(),
                    serialParameters.getSerialNumber(), serialParameters.getBaudRate(), serialParameters.getDataBits(),
                    serialParameters.getStopBits(), serialParameters.getParity(), serialParameters.isFlowControl());
        } catch (Exception e) {
            result = false;
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        if(result){
            this.serialParameters = serialParameters;
        }
        return result;
    }
    public SerialParameters getSerialParameters(){
        return serialParameters;
    }
    public boolean isConnected(){
        if(usedUsbDevice == null) return false;
        return WCHUARTManager.getInstance().isConnected(usedUsbDevice);
    }
    public boolean connect(){
        if(usedUsbDevice == null) return false;
        boolean result = true;
        try {
            WCHUARTManager.getInstance().openDevice(usedUsbDevice);
        } catch (Exception e) {
            result = false;
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        return result;
    }
    public boolean disconnect(){
        if(usedUsbDevice == null) return false;
        boolean result = true;
        try {
            WCHUARTManager.getInstance().disconnect(usedUsbDevice);
        } catch (Exception e) {
            result = false;
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        return result;
    }
    //IDataCallback: int serialNumber, byte[] data, int length
    private void registerDataCallback(IDataCallback iDataCallback){
        if(usedUsbDevice == null) return;
        if(!isConnected()) connect();
        try {
            WCHUARTManager.getInstance().registerDataCallback(usedUsbDevice, iDataCallback);
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
    }

    /**
     * 注册串口数据回调。此方法可代替 readData 方法。
     * 如果注册此回调，数据优先通过该回调方式回传，推荐使用该方法接收数据。
     * 解除注册使用removeDataCallback()方法。
     * @param dataCallback 数据回调
     */
    public void registerDataCallback(DataCallback dataCallback){
        registerDataCallback(new IDataCallback() {
            @Override
            public void onData(int serialNumber, byte[] data, int length) {
                if(serialNumber == UsbUart.this.serialNumber)
                    dataCallback.onDataReceived(data,length);
            }
        });
    }
    public void removeDataCallback(){
        if(usedUsbDevice == null) return;
        try {
            WCHUARTManager.getInstance().removeDataCallback(usedUsbDevice);
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
    }

    /**
     * 1. 当 vTime>0，vMin>0 时。read 调用将保持阻塞直到读取到第一个字符，读到了第一个字符之
     * 后开始计时，此后若时间到了vTime或者时间未到但已读够了vMin个字符则会返回;若在时
     * 间未到之前又读到了一个字符(但此时读到的总数仍不够 vMin)则计时重新开始。
     * 2. 当 vTime>0，vMin=0 时。read 调用读到数据则立即返回，否则将为每个字符最多等待 vTime
     * 时间。
     * 3. 当 vTime=0，vMin>0 时。read 调用一直阻塞，直到读到 vMin 个字符后立即返回。
     * @param vTime 等待时间
     * @param vMin 读取的最小数量
     * @return 读取到的数据
     */
    public byte[] readData(int vTime, int vMin){
        if(usedUsbDevice == null) return null;
        if(!isConnected()) connect();
        byte[] data = null;
        try {
            data = WCHUARTManager.getInstance().readData(usedUsbDevice, serialNumber, vTime, vMin);
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        return data;
    }
    public byte[] readData(){
        if(usedUsbDevice == null) return null;
        if(!isConnected()) connect();
        byte[] data = null;
        try {
            data = WCHUARTManager.getInstance().readData(usedUsbDevice, serialNumber);
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        return data;
    }
    public boolean writeData(byte[] data){
        if(usedUsbDevice == null) return false;
        if(!isConnected()) connect();
        int result = 0;
        try {
            result = WCHUARTManager.getInstance().syncWriteData(usedUsbDevice, serialNumber, data,data.length, TIMEOUT);
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
        return result == data.length;
    }
    public void writeDataAsync(byte[] data){
        if(usedUsbDevice == null) return;
        if(!isConnected()) connect();
        try {
            WCHUARTManager.getInstance().asyncWriteData(usedUsbDevice, serialNumber, data);
        } catch (Exception e) {
            RobotLog.addGlobalWarningMessage(e.getMessage());
        }
    }
    public interface DataCallback {
        void onDataReceived(byte[] data, int length);
    }
    public SerialParameters.Builder getSerialParametersBuilder(){
        return new SerialParameters.Builder(usedUsbDevice, serialNumber);
    }
    public static class SerialParameters {
        private SerialParameters(Builder builder){
            this.usbDevice=builder.usbDevice;
            this.serialNumber =builder.serialNumber;
            this.baudRate =builder.baudRate;
            this.dataBits =builder.dataBits;
            this.stopBits =builder.stopBits;
            this.parity =builder.parity;
            this.flowControl =builder.flowControl;
        }

        private UsbDevice usbDevice;
        private int serialNumber = 0;
        private int baudRate = 9600;
        private int dataBits = 8;
        private int stopBits = 1;
        private int parity = 0;
        private boolean flowControl = false;

        public UsbDevice getUsbDevice() {
            return usbDevice;
        }

        public int getSerialNumber() {
            return serialNumber;
        }

        public int getBaudRate() {
            return baudRate;
        }

        public int getDataBits() {
            return dataBits;
        }

        public int getStopBits() {
            return stopBits;
        }

        public int getParity() {
            return parity;
        }

        public boolean isFlowControl() {
            return flowControl;
        }

        public static class Builder {
            private UsbDevice usbDevice;
            private int serialNumber = 0;
            private int baudRate = 9600;
            private int dataBits = 8;
            private int stopBits = 1;
            private int parity = 0;
            private boolean flowControl = false;

            private Builder (UsbDevice usbDevice,int serialNumber){
                this.usbDevice = usbDevice;
                this.serialNumber = serialNumber;
            }

            public Builder setBaudRate(int baudRate) {
                this.baudRate = baudRate;
                return this;
            }

            public Builder setDataBits(int dataBits) {
                this.dataBits = dataBits;
                return this;
            }

            public Builder setStopBits(int stopBits) {
                this.stopBits = stopBits;
                return this;
            }

            public Builder setParity(int parity) {
                this.parity = parity;
                return this;
            }

            public Builder setFlowControl(boolean flowControl) {
                this.flowControl = flowControl;
                return this;
            }

            public SerialParameters build() {
                return new SerialParameters(this);
            }
        }
    }
}

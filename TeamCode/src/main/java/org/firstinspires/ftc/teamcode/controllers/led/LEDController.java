package org.firstinspires.ftc.teamcode.controllers.led;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.Arrays;


public class LEDController {
    LEDAgreement ledAgreement;
    private LEDControllerSettings ledControllerSettings = new LEDControllerSettings();
    private LEDDrawer[] ledDrawers = new LEDDrawer[7];
    private byte ad1 = 0;
    private byte ad2 = 0;
    private Color color1 = Color.valueOf(Color.RED);
    private Color color2 = Color.valueOf(Color.BLUE);
    private final boolean[][] lockFlags = new boolean[7][];   // 每个端口每个 LED 的锁定标志
    private final Color[][] colors = new Color[7][];
    private RevBlinkinLedDriver.BlinkinPattern[] currentPatterns = new RevBlinkinLedDriver.BlinkinPattern[7];
    private volatile boolean updating = false;
    private Thread updateThread = null;
    private long updatePeriodMs = 50;       // 默认 20Hz

    public void setAd1(int ad1){
        this.ad1 = (byte) (Range.clip(ad1,0,255)-128);
    }
    public void setAd2(int ad2){
        this.ad2= (byte) (Range.clip(ad2,0,255)-128);
    }
    public int getAd1(){
        return ad1+128;
    }
    public int getAd2(){
        return ad2+128;
    }
    public void setColor1(Color color1){
        this.color1 = color1;
    }
    public void setColor2(Color color2){
        this.color2 = color2;
    }
    public Color getColor1(){
        return Color.valueOf(color1.red(),color1.green(),color1.blue(),color1.alpha());
    }
    public Color getColor2(){
        return Color.valueOf(color2.red(),color2.green(),color2.blue(),color2.alpha());
    }
    public void setUpdatePeriod(long periodMs) {
        if (periodMs > 0) this.updatePeriodMs = periodMs;
    }

    public LEDController(){
        this.ledAgreement = new LEDAgreement();
        LEDControllerSettings.LEDControllerSettingsBuilder builder = LEDControllerSettings.getLEDControllerSettingsBuilder();
        if(ledAgreement.askForSettingValue("GLOBAL_BRIGHTNESS")!=null){
            int brightness = 0;
            try{
                brightness = Integer.parseInt(ledAgreement.getReceivedValue("GLOBAL_BRIGHTNESS").get(0),16);
                builder.setGlobalBrightness(brightness/255.0);

            } catch (NumberFormatException e) {
                RobotLog.addGlobalWarningMessage(e.getMessage());
            }
        }
        if(ledAgreement.askForSettingValue("PORT_LED_NUMBER")!=null){
            int[] pln = new int[7];
            try{
                for(int i = 0;i< pln.length;i++){
                    pln[i] = Integer.parseInt(ledAgreement.getReceivedValue("PORT_LED_NUMBER").get(i),16);
                }
                builder.setPortLedNum(pln);

            } catch (NumberFormatException e) {
                RobotLog.addGlobalWarningMessage(e.getMessage());
            }
        }
        ledControllerSettings = builder.build();
        for(int i = 0;i<ledControllerSettings.getPortLedNum().length;i++) {
            ledDrawers[i] = new LEDDrawer(ledControllerSettings.getPortLedNum()[i]);
            ledDrawers[i].update(RevBlinkinLedDriver.BlinkinPattern.BLACK,getAd1(),getAd2(), (int) (ledControllerSettings.getGlobalBrightness()*255));
            currentPatterns[i] = RevBlinkinLedDriver.BlinkinPattern.BLACK;
            lockFlags[i]=new boolean[ledControllerSettings.getPortLedNum()[i]];
            Arrays.fill(lockFlags[i], false);
            colors[i]=new Color[ledControllerSettings.getPortLedNum()[i]];
            Arrays.fill(colors[i],Color.valueOf(Color.BLACK));
        }
        startUpdating();
    }
    public LEDController(LEDControllerSettings ledControllerSettings){
        this.ledControllerSettings = ledControllerSettings;
        ledAgreement = new LEDAgreement();
        applySettings(this.ledControllerSettings);
        for(int i = 0;i<ledControllerSettings.getPortLedNum().length;i++) {
            ledDrawers[i] = new LEDDrawer(ledControllerSettings.getPortLedNum()[i]);
            ledDrawers[i].update(RevBlinkinLedDriver.BlinkinPattern.BLACK,getAd1(),getAd2(), (int) (ledControllerSettings.getGlobalBrightness()*255));
            currentPatterns[i] = RevBlinkinLedDriver.BlinkinPattern.BLACK;
            lockFlags[i]=new boolean[ledControllerSettings.getPortLedNum()[i]];
            Arrays.fill(lockFlags[i], false);
            colors[i]=new Color[ledControllerSettings.getPortLedNum()[i]];
            Arrays.fill(colors[i],Color.valueOf(Color.BLACK));
        }
        startUpdating();
    }
    public static class LEDControllerSettings{
        private byte globalBrightness  = 0;
        private byte[] portLedNum = {-64,-64,-64,-64,-64,-64,-64};
        private int uartBaudRate = 921600;
        public int getUartBaudRate(){
            return uartBaudRate;
        }
        public int[] getPortLedNum(){
            int[] portLedNum = {64,64,64,64,64,64,64};
            for(int index = 0;index<portLedNum.length;index++){
                portLedNum[index] = this.portLedNum[index]+128;
            }
            return portLedNum;
        }
        public double getGlobalBrightness(){
            return (double)(globalBrightness+128)/255;
        }
        public String getUartBaudRateHexString(){
            return String.format("%02x", uartBaudRate);
        }
        public String[] getPortLedNumHexStrings(){
            String[] strings = {"","","","","","",""};
            for(int index = 0;index<this.portLedNum.length;index++){
                strings[index] =String.format("%02x",portLedNum[index]+128);
            }
            return strings;
        }
        public String getPortLedNumHexString(int index){
            return String.format("%02x",portLedNum[index]+128);
        }
        public String getGlobalBrightnessHexString(){
            return String.format("%02x", globalBrightness+128);
        }
        public static LEDControllerSettingsBuilder getLEDControllerSettingsBuilder(){
            return new LEDControllerSettingsBuilder();
        }
        public static LEDControllerSettingsBuilder getLEdControllerSettingsBuilder(LEDControllerSettings settings){
            return new LEDControllerSettingsBuilder().setPortLedNum(settings.getPortLedNum()).setGlobalBrightness(settings.getGlobalBrightness());
        }
        public static class LEDControllerSettingsBuilder {
            private byte globalBrightness = 0;
            private byte[] portLedNum = {-64, -64, -64, -64, -64, -64, -64};
            private int uartBaudRate = 921600;

            private LEDControllerSettingsBuilder() {
            }

            /**
             * @param globalBrightness range: 0~1, the max brightness of LEDs on the controller
             */
            public LEDControllerSettingsBuilder setGlobalBrightness(double globalBrightness) {
                globalBrightness = Range.clip(globalBrightness,0,1);
                int intVal = (int) Math.round(globalBrightness * 255);
                this.globalBrightness = (byte)(intVal - 128);
                return this;
            }

            /**
             * @param portLedNum 7 port, max 255 each, max 448 in total, setting to 0 means to disable this port
             */
            public LEDControllerSettingsBuilder setPortLedNum(int... portLedNum) {
                int[] rPLN= new int[portLedNum.length];
                for(int index = 0;index<this.portLedNum.length&&index<portLedNum.length;index++){
                    rPLN[index] = Range.clip(portLedNum[index],0,255);
                    this.portLedNum[index] = (byte) (rPLN[index]-128);
                }
                return this;
            }

            /**
             * @param portIndex 0~6
             * @param ledNum  max 255, max 448 in all ports, setting to 0 means to disable this port
             */
            public LEDControllerSettingsBuilder setPortLedNum(int portIndex, int ledNum) {
                if (portIndex >= 0 && portIndex < this.portLedNum.length) {
                    ledNum = Range.clip(ledNum,0,255);
                    this.portLedNum[portIndex] = (byte) (ledNum - 128);
                }
                return this;
            }

//            /**
//             * @param uartBaudRate default 921600
//             */
//            public LEDControllerSettingsBuilder setUartBaudRate(int uartBaudRate) {
//                this.uartBaudRate = uartBaudRate;
//                return this;
//            }

            /**
             * build a LEDControllerSettings
             */
            public LEDController.LEDControllerSettings build() {
                LEDController.LEDControllerSettings settings = new LEDController.LEDControllerSettings();
                settings.globalBrightness = this.globalBrightness;
                settings.portLedNum = this.portLedNum;
                settings.uartBaudRate = this.uartBaudRate;
                return settings;
            }
        }
    }
    public boolean applySettings(LEDControllerSettings ledControllerSettings){
        boolean result = true;
        result = result&&ledAgreement.sendCommand(LEDAgreement.LEDCommand.getLEDCommand(LEDAgreement.LEDCommand.LEDCommandList.SET_GLOBAL_BRIGHTNESS),ledControllerSettings.getGlobalBrightnessHexString());
        for(int i = 0;i<=6;i++)
            result = result&&ledAgreement.sendCommand(LEDAgreement.LEDCommand.getLEDCommand(LEDAgreement.LEDCommand.LEDCommandList.SET_PORT_LED_NUMBER),i,ledControllerSettings.getPortLedNumHexString(i));
        result = result&&applySettings();
        return result;
    }
    public boolean applySettings(){
        return ledAgreement.sendCommand(LEDAgreement.LEDCommand.getLEDCommand(LEDAgreement.LEDCommand.LEDCommandList.APPLY_SETTINGS));
    }
    public synchronized boolean setPortLedNum(int p, int n) {
        if (p < 0 || p > 6) return false;
        stopUpdating();
        n = Range.clip(n, 0, 255);
        ledControllerSettings = LEDControllerSettings.getLEdControllerSettingsBuilder(ledControllerSettings).setPortLedNum(p,n).build();
        lockFlags[p] = new boolean[n];
        Arrays.fill(lockFlags[p],false);
        Color[] oldColors = colors[p];
        colors[p] = new Color[n];
        int copyLen = Math.min(oldColors.length, n);
        System.arraycopy(oldColors, 0, colors[p], 0, copyLen);
        if (n > oldColors.length) {
            Arrays.fill(colors[p], oldColors.length, n, Color.valueOf(Color.BLACK));
        }
        ledDrawers[p] = new LEDDrawer(colors[p]);
        boolean result = ledAgreement.sendCommand(
                LEDAgreement.LEDCommand.getLEDCommand(LEDAgreement.LEDCommand.LEDCommandList.SET_PORT_LED_NUMBER),
                p,
                String.format("%02x", n)
        );
        startUpdating();
        return result;
    }
    public synchronized boolean setGlobalBrightness(double b) {
        b = Range.clip(b, 0, 1);
        ledControllerSettings = LEDControllerSettings.getLEdControllerSettingsBuilder(ledControllerSettings).setGlobalBrightness(b).build();
        int brightness = (int) Math.round(b * 255);
        brightness = Range.clip(brightness, 0, 255);
        return ledAgreement.sendCommand(
                LEDAgreement.LEDCommand.getLEDCommand(LEDAgreement.LEDCommand.LEDCommandList.SET_GLOBAL_BRIGHTNESS),
                String.format("%02x", brightness)
        );
    }

    public boolean setPortPattern(int p, RevBlinkinLedDriver.BlinkinPattern pattern) {
        if (p < 0 || p > 6) return false;
        currentPatterns[p] = pattern;
        unlockAll(p);
        return true;
    }
    public void setSingleLEDColor(int port, int index, Color color){
        lockLED(port,index,color);
    }
    public void setMultipleLEDColor(int port, int first, int last, Color color){
        lockLEDs(port,first,last,color);
    }

    private boolean setLEDColor(int port, int index, Color color) {
        if (port < 0 || port > 6) return false;
        int r = 0, g = 0, b = 0;
        r = (int) (color.red() * 255);
        g = (int)(color.green()*255);
        b = (int)(color.blue()*255);
        return ledAgreement.sendCommand(
                LEDAgreement.LEDCommand.getLEDCommand(LEDAgreement.LEDCommand.LEDCommandList.SET_SINGLE_RGB),
                port,
                index,
                String.format("%02x", r),
                String.format("%02x", g),
                String.format("%02x", b)
        );
    }

    private boolean setRangeLEDColor(int port, int first, int last, Color color) {
        if (port < 0 || port > 6) return false;
        if(first>last){
            int temp=first;
            first = last;
            last  = temp;
        }else if(first==last){
            return setLEDColor(port,first,color);
        }
        int r = 0, g = 0, b = 0;
        r = (int) (color.red() * 255);
        g = (int)(color.green()*255);
        b = (int)(color.blue()*255);
        return ledAgreement.sendCommand(
                LEDAgreement.LEDCommand.getLEDCommand(LEDAgreement.LEDCommand.LEDCommandList.SET_RANGE_RGB),
                port,
                first,
                last,
                String.format("%02x", r),
                String.format("%02x", g),
                String.format("%02x", b)
        );
    }

    public boolean setLEDColors(int port, Color... colors) {
        if (port < 0 || port > 6) return false;
        int portLedCount = ledControllerSettings.getPortLedNum()[port];
        if (colors.length == 0) return true;

        int len = Math.min(colors.length, portLedCount);
        boolean success = true;
        int i = 0;

        while (i < len) {
            Color current = colors[i];
            int start = i;
            int currentArgb = current.toArgb();

            while (i < len && colors[i].toArgb() == currentArgb) {
                i++;
            }
            int end = i - 1;
            int count = end - start + 1;

            if (count >= 2) {
                success &= setRangeLEDColor(port, start, end, current);
            } else {
                success &= setLEDColor(port, start, current);
            }
        }
        return success;
    }

    public synchronized void lockLED(int port, int index, Color color) {
        lockLEDs(port,index,index,color);
    }

    public synchronized void lockLEDs(int port, int start, int end, Color color) {
        if (port < 0 || port >= 7) return;
        int len = ledControllerSettings.getPortLedNum()[port];
        if (start < 0) start = 0;
        if (end >= len) end = len - 1;
        if (start > end) return;
        for (int i = start; i <= end; i++) {
            lockFlags[port][i] = true;
            colors[port][i] = color;
        }
    }

    public synchronized void unlockAll(int port) {
        if (port < 0 || port >= 7) return;
        int len = ledControllerSettings.getPortLedNum()[port];
        Arrays.fill(lockFlags[port], false);
    }

    public void startUpdating() {
        if (updating) return;
        updating = true;
        updateThread = new Thread(() -> {
            while (updating) {
                long start = System.currentTimeMillis();
                // 遍历 7 个端口
                for (int i = 0; i < ledDrawers.length; i++) {
                    synchronized (this) {
                        LEDDrawer drawer = ledDrawers[i];
                        if (drawer == null) continue;
                        RevBlinkinLedDriver.BlinkinPattern pattern = currentPatterns[i];
                        if (pattern == null) pattern = RevBlinkinLedDriver.BlinkinPattern.BLACK;
                        int brightness = (int) (ledControllerSettings.getGlobalBrightness() * 255);
                        drawer.setColor1(getColor1());
                        drawer.setColor2(getColor2());
                        drawer.update(pattern, getAd1(), getAd2(), brightness);
                        Color[] leds = drawer.getLeds();
                        for (int j = 0; j < lockFlags[i].length; j++) {
                            if (!lockFlags[i][j]) colors[i][j] = leds[j];
                        }
                    }
                    // 批量上传
                    setLEDColors(i, colors[i]);
                }

                long elapsed = System.currentTimeMillis() - start;
                long sleep = updatePeriodMs - elapsed;
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        updateThread.start();
    }

    public void stopUpdating() {
        updating = false;
        if (updateThread != null) {
            updateThread.interrupt();
            try {
                updateThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            updateThread = null;
        }
    }

    public void close(){
        stopUpdating();
        ledAgreement.close();
    }
}
package org.firstinspires.ftc.teamcode.controllers.uart;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleUartAgreement {
    protected String[] possibleReceivingMessageTypeAndCode = {"rDAT"};
    protected int generalValueLength = 4;
    protected UsbUart usbUart;
    private final ConcurrentLinkedQueue<String> receivedMessages = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> receivedValues = new ConcurrentHashMap<>();
    public List<String> getReceivedValue(String valueName){
        if (valueName == null) {
            return Collections.emptyList();
        }
        List<String> list = receivedValues.get(valueName);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }
    public List<String> removeReceivedValue(String valueName){
        return receivedValues.remove(valueName);
    }
    public Map<String, List<String>> getAllReceivedValues() {
        Map<String, List<String>> unmodifiableMap = new HashMap<>();
        for (Map.Entry<String, CopyOnWriteArrayList<String>> entry : receivedValues.entrySet()) {
            unmodifiableMap.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(unmodifiableMap);
    }
    public SimpleUartAgreement (UsbUart usbUart){
        this.usbUart = usbUart;
        this.usbUart.setSerialParameters(this.usbUart.getSerialParametersBuilder()
                .setBaudRate(115200)
                .build()
        );
        usbUart.registerDataCallback(new SimpleDataCallback());
    }
    public SimpleUartAgreement(UsbUart usbUart, UsbUart.SerialParameters serialParameters){
        this.usbUart = usbUart;
        this.usbUart.setSerialParameters(serialParameters);
        usbUart.registerDataCallback(new SimpleDataCallback());
    }
    public synchronized boolean sendCommand(Command command, Object... args){
        if (args.length < command.getArgumentNumber()){
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(command.getCommandType());
        stringBuilder.append(command.getCommandCode());
        stringBuilder.append(":");
        for (int i = 0; i < command.getArgumentNumber(); i++) {
            stringBuilder.append(args[i]);
            if (i != command.getArgumentNumber() - 1) {
                stringBuilder.append(",");
            } else {
                stringBuilder.append(";\r\n");
            }
        }
        return usbUart.writeData(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }
    public void sendCommandAsync(Command command, Object... args){
        if (args.length < command.getArgumentNumber()){
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(command.getCommandType());
        stringBuilder.append(command.getCommandCode());
        stringBuilder.append(":");
        for (int i = 0; i < command.getArgumentNumber(); i++) {
            stringBuilder.append(args[i]);
            if (i != command.getArgumentNumber() - 1) {
                stringBuilder.append(",");
            } else {
                stringBuilder.append(";\r\n");
            }
        }
        usbUart.writeDataAsync(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }

    public interface Command {
        char getCommandType();
        String getCommandCode();
        int getArgumentNumber();
    }
    public static class CommandImpl implements Command {
        private final char commandType;
        private final String commandCode;
        private final int argumentNumber;

        public CommandImpl(char commandType, String commandCode, int argumentNumber) {
            this.commandType = commandType;
            this.commandCode = commandCode;
            this.argumentNumber = argumentNumber;
        }

        @Override
        public char getCommandType() {
            return commandType;
        }

        @Override
        public String getCommandCode() {
            return commandCode;
        }

        @Override
        public int getArgumentNumber() {
            return argumentNumber;
        }
        public static Command getCommand(CommandList commandList) {
            return new CommandImpl(commandList.getCommandType(), commandList.getCommandCode(), commandList.getArgumentNumber());
        }
        public enum CommandList {
            EXAMPLE_COMMAND('s', "EXA", 2),
            GET_DATA('g', "DAT", 1);

            private final char commandType;
            private final String commandCode;
            private final int argumentNumber;

            CommandList(char commandType, String commandCode, int argumentNumber) {
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
    public class SimpleDataCallback implements UsbUart.DataCallback {
        private ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        @Override
        public synchronized void onDataReceived(byte[] data, int length) {
            if(length-byteBuffer.remaining()+ byteBuffer.capacity()>2048){
                // For safety, If the new data plus the existing data exceeds 2048 bytes(hard to reach), clear the buffer to avoid overflow
                byteBuffer.clear();
            }
            // Check if the ByteBuffer has enough space to accommodate the new data
            if (byteBuffer.remaining() < length) {
                // If not enough space, create a new ByteBuffer with double the capacity or enough to hold the new data
                ByteBuffer newBuffer = ByteBuffer.allocate(Math.max(
                        byteBuffer.capacity() * 2,
                        length - byteBuffer.remaining() + byteBuffer.capacity()
                ));
                byteBuffer.flip(); // Prepare the old buffer for reading
                newBuffer.put(byteBuffer); // Copy the old data to the new buffer
                byteBuffer = newBuffer; // Replace the old buffer with the new one
            }
            // Append the received data to the ByteBuffer
            byteBuffer.put(data, 0, length);

            // Check if we have a complete message (ending with ';')
            int i = 0;
            while(i < byteBuffer.position()) {
                if (byteBuffer.get(i) == ';') {
                    // We found a complete message
                    byte[] messageBytes = new byte[i + 1];
                    byteBuffer.flip();
                    byteBuffer.get(messageBytes, 0, i + 1);
                    String message = new String(messageBytes, StandardCharsets.UTF_8);
                    receivedMessages.add(message.trim());
                    byteBuffer.compact();
                    i=-1; // Reset the index to start checking for the next complete message
                }
                i++;
            }

            // Process the received messages to extract values
            processReceivedMessages();
        }
    }

    public void processReceivedMessages() {
        String message;
        while ((message = receivedMessages.poll()) != null) {
            // 1. 从后往前找分号（消息末尾应该有分号，但以防万一）
            int semiIndex = message.lastIndexOf(';');
            if (semiIndex == -1) {
                continue; // 没有分号，忽略
            }

            // 2. 从分号位置往前找冒号
            int colonIndex = message.lastIndexOf(':', semiIndex);
            if (colonIndex == -1) {
                continue; // 没有冒号，忽略
            }

            // 3. 确保冒号前至少有4个字符，提取类型标识
            if (colonIndex < 4) {
                continue;
            }
            String type = message.substring(colonIndex - 4, colonIndex);
            boolean isValidType = false;
            for(String validType : getPossibleReceivingMessageTypeAndCode()){
                if(validType.equals(type)){
                    isValidType = true;
                    break;
                }
            }
            if (!isValidType) {
                continue; // 类型不匹配，忽略
            }

            // 4. 提取冒号后的数据部分（到分号前）
            String dataPart = message.substring(colonIndex + 1, semiIndex);
            if (!dataPart.contains(",")) {
                continue; // 没有逗号，说明没有数据，忽略
            }

            // 5. 按逗号分割数据
            String[] parts = dataPart.split(",");
            if (parts.length < 2) {
                continue; // 至少需要名称 + 一个值，否则忽略
            }

            // 6. 第一个字段为名称，剩余为值列表
            String name = parts[0];
            List<String> values = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));

            // 7. 存入 Map（有重复则替换）
            receivedValues.put(name, new CopyOnWriteArrayList<>(values));
        }
    }
    private String[] getPossibleReceivingMessageTypeAndCode(){
        return possibleReceivingMessageTypeAndCode.clone();
    }
    public double estimateSendMS(int argNum){
        UsbUart.SerialParameters serialParameters = usbUart.getSerialParameters();
        int totalBytes = 4 + 1 + argNum * generalValueLength + argNum - 1 + 1 + 2; // = 7 + 5*argNum
        int bitsPerByte = 1 + serialParameters.getDataBits() + serialParameters.getStopBits(); // = 10
        double timePerByteMs = (double) bitsPerByte / serialParameters.getBaudRate() * 1000; // 毫秒
        return totalBytes * timePerByteMs;
    }
    public double estimateSentMS(String... strings){
        UsbUart.SerialParameters serialParameters = usbUart.getSerialParameters();
        int totalBytes = 4 + 1 + strings.length - 1 + 1 + 2;
        for(String string: strings){
            totalBytes += string.length();
        }
        int bitsPerByte = 1 + serialParameters.getDataBits() + serialParameters.getStopBits(); // = 10
        double timePerByteMs = (double) bitsPerByte / serialParameters.getBaudRate() * 1000; // 毫秒
        return totalBytes * timePerByteMs;
    }

    public void close(){
        usbUart.removeDataCallback();
        usbUart.disconnect();

    }
}

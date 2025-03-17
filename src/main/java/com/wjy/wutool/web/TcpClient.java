package com.wjy.wutool.web;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * tcp客户端，基于java.net.Socket实现。支持自定义编解码、优雅处理读响应超时、多包响应拆解处理
 *
 * @author weijiayu
 * @date 2025/3/12 11:49
 */
@Slf4j
public class TcpClient {

    private final String host;
    private final int port;

    // 请求连接超时时间
    private int connectTimeoutMs = 2000;

    // 读取响应内容超时时间
    @Setter
    private int readTimeoutMs = 3000;

    // 单包消息最大接收容量
    private int maxReceiveByteSize = 1024;

    // 是否忽略读响应超时异常。适用于服务端没有手动关闭响应的场景
    private boolean isCloseReadTimeOutGracefully = false;

    // 消息发送前自定义编码，eg. (s) -> HexUtil.encodeHexStr(s).getBytes(StandardCharsets.UTF_8);
    @Setter
    private Function<String, byte[]> applyEncodeFunc;

    // 消息接收自定义解码，eg. (bytes) -> HexUtil.decodeHexStr(new String(bytes), StandardCharsets.UTF_8);
    private Function<byte[], String> applyDecodeFunc;

    public TcpClient(String host, int port, boolean isCloseReadTimeOutGracefully) {
        this.host = host;
        this.port = port;
        this.isCloseReadTimeOutGracefully = isCloseReadTimeOutGracefully;
    }

    public TcpClient(String host, int port, int connectTimeoutMs, int readTimeoutMs, int maxReceiveByteSize,
                     boolean isCloseReadTimeOutGracefully,
                     Function<String, byte[]> applyEncodeFunc, Function<byte[], String> applyDecodeFunc) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.maxReceiveByteSize = maxReceiveByteSize;
        this.isCloseReadTimeOutGracefully = isCloseReadTimeOutGracefully;
        this.applyEncodeFunc = applyEncodeFunc;
        this.applyDecodeFunc = applyDecodeFunc;
    }

    /**
     * 发送消息并接收响应
     *
     * @param message 发送消息
     * @return 响应消息
     */
    public String sendAndReceive(String message) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), connectTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);
            log.trace("tcp req data={}", message);
            OutputStream outputStream = socket.getOutputStream();
            byte[] requestBytes = applyEncodeFunc != null ? applyEncodeFunc.apply(message) :
                    message.getBytes(StandardCharsets.UTF_8);
            outputStream.write(requestBytes);
            outputStream.flush();
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            try (InputStream inputStream = socket.getInputStream()) {
                byte[] buffer = new byte[maxReceiveByteSize];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    responseBuffer.write(buffer, 0, bytesRead);
                }
            } catch (SocketTimeoutException e) {
                if (!this.isCloseReadTimeOutGracefully) {
                    throw e;
                }
            }
            if (responseBuffer.size() == 0) {
                return null;
            } else {
                return applyDecodeFunc != null ? applyDecodeFunc.apply(responseBuffer.toByteArray()) :
                        responseBuffer.toString(StandardCharsets.UTF_8.name());
            }
        }
    }

    /**
     * 发送消息并接收响应，响应内容按行整理。针对响应数据分多个包的场景
     *
     * @param message 发送消息
     * @return 响应消息
     */
    public List<String> sendAndReceiveLines(String message) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), connectTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);
            log.trace("tcp req data={}", message);
            OutputStream outputStream = socket.getOutputStream();
            byte[] requestBytes = applyEncodeFunc != null ? applyEncodeFunc.apply(message) :
                    message.getBytes(StandardCharsets.UTF_8);
            outputStream.write(requestBytes);
            outputStream.flush();
            List<String> lineList = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.trace("tcp rsp line data={}", line);
                    lineList.add(line);
                }
            } catch (SocketTimeoutException e) {
                if (!this.isCloseReadTimeOutGracefully) {
                    throw e;
                }
            }
            return lineList;
        }
    }

    /**
     * 发送消息，无需接收响应
     *
     * @param message 发送消息
     */
    public void send(String message) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), connectTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);
            OutputStream outputStream = socket.getOutputStream();
            System.out.println("req=" + message);
            log.trace("tcp req data={}", message);
            byte[] requestBytes = applyEncodeFunc != null ? applyEncodeFunc.apply(message) :
                    message.getBytes(StandardCharsets.UTF_8);
            outputStream.write(requestBytes);
            outputStream.flush();
        }
    }

    /**
     * 发送消息和接收响应，判断响应内容满足条件时，关闭响应读，直接返回条件判断结果
     *
     * @param message                 发送消息
     * @param testCheckRspIsCloseFunc eg. Predicate<String> isRspOkFunc = "OK"::equals
     */
    public boolean sendAndCheckReceiveByLine(String message, Predicate<String> testCheckRspIsCloseFunc) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), connectTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);
            OutputStream outputStream = socket.getOutputStream();
            System.out.println("req=" + message);
            log.trace("tcp req data={}", message);
            byte[] requestBytes = applyEncodeFunc != null ? applyEncodeFunc.apply(message) :
                    message.getBytes(StandardCharsets.UTF_8);
            outputStream.write(requestBytes);
            outputStream.flush();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("rsp line=" + line);
                    log.trace("tcp rsp line data={}", line);
                    if (testCheckRspIsCloseFunc.test(line)) {
                        // 满足条件，直接中断读，并返回
                        return true;
                    }
                }
            } catch (SocketTimeoutException e) {
                if (!this.isCloseReadTimeOutGracefully) {
                    throw e;
                }
            }
            return false;
        }
    }

    /**
     * 发送消息和接收响应，判断响应内容满足条件时，关闭响应读，直接返回满足判断结果的内容
     *
     * @param message 发送消息
     */
    public String sendAndReceiveTargetLine(String message, Predicate<String> testCheckRspIsCloseFunc) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), connectTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);
            OutputStream outputStream = socket.getOutputStream();
            System.out.println("req=" + message);
            log.trace("tcp req data={}", message);
            byte[] requestBytes = applyEncodeFunc != null ? applyEncodeFunc.apply(message) :
                    message.getBytes(StandardCharsets.UTF_8);
            outputStream.write(requestBytes);
            outputStream.flush();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("rsp line=" + line);
                    log.trace("tcp rsp line data={}", line);
                    if (testCheckRspIsCloseFunc.test(line)) {
                        // 满足条件，直接中断读，并返回
                        return line;
                    }
                }
            } catch (SocketTimeoutException e) {
                if (!this.isCloseReadTimeOutGracefully) {
                    throw e;
                }
            }
            return null;
        }
    }
}
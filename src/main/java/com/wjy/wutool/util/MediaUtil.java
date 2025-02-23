package com.wjy.wutool.util;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.http.HttpStatus;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * 视频处理：图片合成mp4（支持文件、链接等形式的图片）、视频http附件下载
 *
 * @author weijiayu
 * @date 2024/10/10 17:08
 */
@Slf4j
public class MediaUtil {

    // 图片合成mp4（支持文件、链接等形式的图片）
    public static Boolean compositeMp4(String outPutFilePath, List<String> picUrlList) {
        FFmpegFrameRecorder recorder = null;
        try {
            // 视频宽高，常见有16:9或者9:16
            int imageWidth = 1920;
            int imageHeight = 1080;
            // 视频帧率，为x帧每秒
            int frameRate = 30;
            // 视频时长，单位秒，1秒播放一张图片
            int playSecond = picUrlList.size();
            recorder = new FFmpegFrameRecorder(outPutFilePath, imageWidth, imageHeight);
            // 设置视频编码层模式
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFrameRate(frameRate);
            // 设置视频图像数据格式
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setFormat("mp4");
            recorder.start();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            // 录制一个x秒的视频
            for (int i = 0; i < playSecond; i++) {
                URL url = new URL(picUrlList.get(i));
                if (picUrlList.get(i).indexOf("https://") != -1) {
                    final SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, getTrustingManager(), new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    InputStream connection = url.openStream();
                    BufferedImage read = ImageIO.read(url);
                    // 一秒是x帧，所以要记录x次
                    for (int j = 0; j < frameRate; j++) {
                        recorder.record(converter.getFrame(read));
                    }
                    connection.close();
                } else {
                    BufferedImage read = ImageIO.read(url);
                    // 一秒是x帧，所以要记录x次
                    for (int j = 0; j < frameRate; j++) {
                        recorder.record(converter.getFrame(read));
                    }
                }
            }
            return true;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    // 视频http附件下载
    public static void rspAttachment(HttpServletResponse response, File mediaFile) {
        try {
            try (InputStream fis = new BufferedInputStream(new FileInputStream(mediaFile));
                OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());) {
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                response.addHeader("Content-Disposition",
                    "attachment;filename=" + new String(mediaFile.getName().getBytes("gbk"), "iso-8859-1"));
                response.addHeader("Content-Length", "" + mediaFile.length());
                response.setContentType("application/octet-stream");
                outputStream.write(buffer);
                outputStream.flush();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private static TrustManager[] getTrustingManager() {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }};
        return trustAllCerts;
    }
}

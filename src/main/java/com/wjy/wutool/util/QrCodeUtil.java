package com.wjy.wutool.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 二维码图片生成、编码、解码。基于google的zxing库
 */
public class QrCodeUtil {

    /** 默认二维码宽度 */
    private static final int width = 300;
    /** 默认二维码高度 */
    private static final int height = 300;
    /** 默认二维码文件格式 */
    private static final String format = "png";
    /** 二维码参数 */
    private static final Map<EncodeHintType, Object> hints = new HashMap();
    /** 随机种子数 */
    private static final String sources = "0123456789";

    static {
        /** 字符编码 */
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        /** 容错等级 L、M、Q、H 其中 L 为最低, H 为最高 */
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        /** 二维码与图片边距 */
        hints.put(EncodeHintType.MARGIN, 2);
    }

    /**
     * 返回一个BufferedImage对象
     * 
     * @param content
     * @param width
     * @param height
     */
    public static BufferedImage toBufferedImage(String content, int width, int height)
        throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * 将二维码图片输出到一个流中
     * 
     * @param content
     * @param stream
     * @param width
     * @param height
     */
    public static void writeToStream(String content, OutputStream stream, int width, int height)
        throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToStream(bitMatrix, format, stream);
    }

    /**
     * 将二维码图片输出到一个流中
     *
     * @param content
     * @param stream
     */
    public static void writeToStream(String content, OutputStream stream) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToStream(bitMatrix, format, stream);
    }

    /**
     * 生成二维码图片文件
     * 
     * @param content
     * @param path
     * @param width
     * @param height
     */
    public static void createQRCode(String content, String path, int width, int height)
        throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToPath(bitMatrix, format, new File(path).toPath());
    }

    /**
     * 生成二维码图片文件
     *
     * @param content
     * @param path
     */
    public static void createQRCode(String content, String path) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToPath(bitMatrix, format, new File(path).toPath());
    }

    /**
     * 二维码图片解码
     *
     * @param filePath
     */
    public static String decodeQRCode(String filePath) {
        String retStr = "";
        try {
            BufferedImage bufferedImage = ImageIO.read(new FileInputStream(filePath));
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap bitmap = new BinaryBitmap(binarizer);
            HashMap<DecodeHintType, Object> hintTypeObjectHashMap = new HashMap<>();
            hintTypeObjectHashMap.put(DecodeHintType.CHARACTER_SET, "utf-8");
            Result result = new MultiFormatReader().decode(bitmap, hintTypeObjectHashMap);
            retStr = result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retStr;
    }

    private static String generator6Content() {
        Random rd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(sources.charAt(rd.nextInt(9)));
        }
        return sb.toString();
    }

    private static String generator32UUIDContent() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void main(String[] args) throws Exception {
        String content = generator6Content();
        System.out.println(content);
        String filePath = System.getProperty("user.dir") + "\\" + "test-" + content + ".png";
        QrCodeUtil.createQRCode(content, filePath);
        System.out.println(QrCodeUtil.decodeQRCode(filePath));
    }

}

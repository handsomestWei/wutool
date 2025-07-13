package com.wjy.wutool.util;

import cn.hutool.core.codec.Base62;

import java.nio.ByteBuffer;

/**
 * 唯一短id生成工具类
 *
 * @author weijiayu
 * @date 2025/7/10 23:24
 */
public class ShortIdUtil {

    private static final long OBFUSCATION_PRIME = (4861877399L * 4861877399L * 4861877399L + 3L);

    /**
     * 唯一短Id生成
     *
     * @return java.lang.String
     * @date 2025/6/16 23:53
     */
    public static String generateShortId() {
        long unionId = System.currentTimeMillis();
        return generateShortId(unionId);
    }

    /**
     * 唯一短Id生成
     *
     * @param unionId 唯一Id，如自增主键id
     * @return java.lang.String
     * @date 2025/6/16 23:53
     */
    public static String generateShortId(long unionId) {
        long obfuscatedId = unionId * OBFUSCATION_PRIME;
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(obfuscatedId);
        byte[] fullBytes = buffer.array();
        // 使用6个字节来缩短ID长度.
        // 这会带来极低的碰撞风险，但长度会缩短到9位.
        byte[] shortBytes = new byte[6];
        System.arraycopy(fullBytes, 2, shortBytes, 0, 6); // 取后6个字节
        return Base62.encode(shortBytes);
    }
}

package cn.sparrowmini.owl.solr.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SolrIdGenerator {
    public static String generateRestrictionId(String classUri, String propertyUri) {
        if (classUri == null || propertyUri == null) {
            throw new IllegalArgumentException("Class URI 和 Property URI 不能为空");
        }

        // 1. 将两个核心天然键锚定在一起，形成唯一确定性元字符串
        String rawKey = classUri + "|" + propertyUri;

        try {
            // 2. 执行标准 MD5 哈希
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));

            // 3. 转为 32 位无污染的十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // 🚀 返回形如 "b6f8a2c4e1d3f5a7b9c1d3e5f7a9b1c3" 的完美主键
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // 理论上不可能发生，JVM 标准自带 MD5
            return String.valueOf(rawKey.hashCode());
        }
    }
}

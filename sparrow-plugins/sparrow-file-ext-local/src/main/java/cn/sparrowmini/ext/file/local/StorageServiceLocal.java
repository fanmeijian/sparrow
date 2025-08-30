package cn.sparrowmini.ext.file.local;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;


@Service
public class StorageServiceLocal {
    private final Path rootLocation;
    private final String rootDir = System.getProperty("user.dir");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public StorageServiceLocal() {
        this.rootLocation = Paths.get(System.getProperty("user.dir"));
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    /**
     * key1: path, key2: checksum, k3: size, k4:mimeType
     * @param inputStream
     * @return
     */
    public Map<String, Object> store(InputStream inputStream) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 使用 DigestInputStream 来一边计算 MD5 校验和，一边写文件
            Path path = this.getFilePath(UUID.randomUUID().toString());
            try (DigestInputStream dis = new DigestInputStream(inputStream, md)) {
                Files.copy(dis, path, StandardCopyOption.REPLACE_EXISTING); // 写入文件
            }

            // 计算 MD5 校验和
            byte[] hash = md.digest();
            String checksum = new BigInteger(1, hash).toString(16);  // 转换为十六进制字符串

            // 获取文件的大小
            long fileSize = Files.size(path);  // 获取文件大小

            // 获取文件的 MIME 类型
            String mimeType = Files.probeContentType(path);  // 获取 MIME 类型

            // 返回文件路径、校验和、大小和类型
            return Map.of(
                    "path", path.toUri().toString(),
                    "checksum", checksum,
                    "size", fileSize,
                    "fileName", path.getFileName().toString(),
                    "mimeType", mimeType != null ? mimeType : "unknown"  // 处理返回为空的情况
            );
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readFile(OutputStream outputStream, String path_){
        Path path = Paths.get(this.rootDir + "/" + path_);
        try(InputStream inputStream = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192]; // 8KB 缓冲区
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1).filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }

    }


    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }







    /**
     * 在上传文件的时候，获取到文件的全路径，根据日起
     *
     * @param fileName
     * @return
     */
    private Path getFilePath(String fileName) {
        String[] d = sdf.format(new Date()).split("-");
        try {
            Files.createDirectories(Paths.get(this.rootDir, d));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Paths.get(this.rootDir, d).resolve(fileName);
    }

}

package cn.sparrowmini.ext.oss.txcos;

import lombok.Value;

@Value
public class TempKeyRequestBody {
    String fileName;
    String path;
    String bucket;
    String region;
}

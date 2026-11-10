package cn.sparrowmini.storage.api;

import java.io.*;
public interface StorageProvider {
    String code();
    /** The caller owns the supplied stream. */
    StoredObject upload(InputStream input) throws IOException;
    /** The caller must close the returned stream. */
    InputStream open(String key) throws IOException;
    void delete(String key) throws IOException;
}

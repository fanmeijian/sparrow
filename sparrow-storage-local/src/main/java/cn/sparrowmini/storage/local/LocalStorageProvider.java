package cn.sparrowmini.storage.local;

import cn.sparrowmini.storage.api.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public final class LocalStorageProvider implements StorageProvider {
    private final Path root;private final long maxBytes;
    public LocalStorageProvider(Path root,long maxBytes)throws IOException{
        if(maxBytes<1)throw new IllegalArgumentException("Positive upload limit required");
        Files.createDirectories(root);this.root=root.toRealPath();this.maxBytes=maxBytes;
    }
    public String code(){return "local";}
    public StoredObject upload(InputStream input)throws IOException{
        Objects.requireNonNull(input);String key=UUID.randomUUID().toString();Path file=path(key);
        try{
            MessageDigest digest=MessageDigest.getInstance("SHA-256");long total=0;
            try(var out=Files.newOutputStream(file,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE,LinkOption.NOFOLLOW_LINKS)){
                byte[] buffer=new byte[8192];int n;
                while((n=input.read(buffer))!=-1){if(n==0)continue;total=Math.addExact(total,n);if(total>maxBytes)throw new IOException("Upload limit exceeded");out.write(buffer,0,n);digest.update(buffer,0,n);}
            }
            return new StoredObject(key,total,HexFormat.of().formatHex(digest.digest()));
        }catch(IOException|RuntimeException|NoSuchAlgorithmException e){
            try{Files.deleteIfExists(file);}catch(IOException cleanup){e.addSuppressed(cleanup);}
            if(e instanceof IOException io)throw io;throw new IOException("Object upload failed",e);
        }
    }
    public InputStream open(String key)throws IOException{return Files.newInputStream(path(key),LinkOption.NOFOLLOW_LINKS);}
    public void delete(String key)throws IOException{Files.deleteIfExists(path(key));}
    private Path path(String key)throws IOException{
        try{if(!UUID.fromString(key).toString().equals(key))throw new IllegalArgumentException();}catch(RuntimeException e){throw new IOException("Invalid object key");}
        Path path=root.resolve(key);if(Files.isSymbolicLink(path))throw new IOException("Symbolic object forbidden");return path;
    }
}

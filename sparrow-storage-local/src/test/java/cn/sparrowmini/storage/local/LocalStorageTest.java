package cn.sparrowmini.storage.local;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;
class LocalStorageTest {
    @TempDir Path root;
    @Test void roundtripAndIdempotentDeletion()throws Exception{
        var store=new LocalStorageProvider(root,100);var object=store.upload(new ByteArrayInputStream("hello".getBytes()));
        assertEquals(5,object.size());assertEquals(64,object.sha256().length());
        try(var in=store.open(object.key())){assertEquals("hello",new String(in.readAllBytes()));}
        store.delete(object.key());store.delete(object.key());assertThrows(IOException.class,()->store.open(object.key()));
    }
    @Test void invalidKeysAndOversizeStreamsLeaveNoPartialObjects()throws Exception{
        var store=new LocalStorageProvider(root,2);assertThrows(IOException.class,()->store.open("../../secret"));
        assertThrows(IOException.class,()->store.upload(new ByteArrayInputStream(new byte[3])));
        try(var paths=Files.list(root)){assertEquals(0,paths.count());}
    }
    @Test void symbolicObjectsAreRejected()throws Exception{
        var store=new LocalStorageProvider(root,100);var key=java.util.UUID.randomUUID().toString();
        var target=Files.writeString(root.resolve("target"),"private");Files.createSymbolicLink(root.resolve(key),target);
        assertThrows(IOException.class,()->store.open(key));assertThrows(IOException.class,()->store.delete(key));assertEquals("private",Files.readString(target));
    }
}

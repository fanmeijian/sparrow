package cn.sparrowmini.file;
import cn.sparrowmini.storage.api.*;
import cn.sparrowmini.storage.local.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
class FileServiceTest {
    static EntityManagerFactory emf;EntityManager em;TransactionTemplate transactions;FileService service;LocalStorageProvider local;
    @TempDir Path root;
    @BeforeAll static void open(){emf=Persistence.createEntityManagerFactory("file-test");}
    @AfterAll static void close(){emf.close();}
    @BeforeEach void setup()throws Exception{
        em=SharedEntityManagerCreator.createSharedEntityManager(emf);transactions=new TransactionTemplate(new JpaTransactionManager(emf));local=new LocalStorageProvider(root,100);
        service=new FileService(em,new StorageRegistry(List.of(local)),UUID.randomUUID().toString(),(n,s,id,c)->{},id->true);
    }
    <T>T tx(Supplier<T> action){return transactions.execute(s->action.get());}
    FileService.Info upload(){return tx(()->{try{return service.upload("local","hello.txt",new ByteArrayInputStream("hello".getBytes()),"order:1");}catch(IOException e){throw new UncheckedIOException(e);}});}
    @Test void uploadDownloadAndRetryableDelete()throws Exception{
        var f=upload();var out=new ByteArrayOutputStream();service.download(f.id(),"order:1",out);assertEquals("hello",out.toString());
        var pending=tx(()->service.requestDelete(f.id(),f.version(),"order:1"));assertEquals(FileMetadata.State.DELETE_PENDING,pending.state());
        assertThrows(IllegalStateException.class,()->service.download(f.id(),"order:1",out));
        var removed=tx(()->{try{return service.completeDelete(f.id(),"order:1");}catch(IOException e){throw new UncheckedIOException(e);}});assertEquals(FileMetadata.State.DELETED,removed.state());
        tx(()->{try{return service.completeDelete(f.id(),"order:1");}catch(IOException e){throw new UncheckedIOException(e);}});
        try(var paths=Files.list(root)){assertEquals(0,paths.count());}
    }
    @Test void rollbackCleansUploadedObject()throws Exception{
        assertThrows(IllegalStateException.class,()->transactions.execute(s->{try{service.upload("local","x",new ByteArrayInputStream(new byte[]{1}),null);}catch(IOException e){throw new UncheckedIOException(e);}throw new IllegalStateException("abort");}));
        try(var paths=Files.list(root)){assertEquals(0,paths.count());}
    }
    @Test void fileGuardProtectsDirectDownload()throws Exception{
        var f=upload();var denied=new FileService(em,new StorageRegistry(List.of(local)),"any",(n,s,id,c)->{throw new SecurityException("Parent object unavailable");},id->true);
        var out=new ByteArrayOutputStream();assertThrows(SecurityException.class,()->denied.download(f.id(),"forged-context",out));assertEquals(0,out.size());
    }
}

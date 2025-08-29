package cn.sparrowmini.bpm.server.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 捕获数据库插入等抛出的 Hibernate 外键/唯一约束异常
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleHibernateConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getSQLState().equals("23505")
                ? "存在重复项！" + ex.getSQLException().getLocalizedMessage()
                : "数据库约束错误：" + ex.getSQLException().getLocalizedMessage();
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", msg));
    }

    // 捕获 Spring 框架包装后的数据库异常（比如外键约束失败）
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleSpringDataIntegrityViolation(DataIntegrityViolationException ex) {
        Throwable rootCause = getRootCause(ex);
        String message = rootCause.getMessage();

        if (message != null && message.contains("violates foreign key constraint")) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "外键约束错误，请检查相关数据是否存在。" + message));
        }
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", "数据完整性异常：" + message));
    }

    // 兜底
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> defaultExceptionHandler(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", "服务器异常：" + ex.getMessage()));
    }

    private Throwable getRootCause(Throwable ex) {
        Throwable cause = ex.getCause();
        return (cause == null || cause == ex) ? ex : getRootCause(cause);
    }

    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Broken pipe")) {
            // 通常忽略，因为是客户端断开连接导致的非服务端问题
            return;
        }
        // 其他 IOException 可记录或处理
        log.error("IOException occurred", ex);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<String> handleTransactionSystemException(TransactionSystemException ex) {
        Throwable root = unwrap(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("事务提交失败: " + root.getMessage());
    }

    private Throwable unwrap(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause;
    }
}
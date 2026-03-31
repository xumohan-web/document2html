package org.mohan.docimport.exception;

/**
 * 文档导入业务异常基类。
 */
public class DocumentImportException extends RuntimeException {

    private final int code;

    public DocumentImportException(int code, String message) {
        super(message);
        this.code = code;
    }

    public DocumentImportException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

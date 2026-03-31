package org.mohan.docimport.exception;

/**
 * 文件内容非法或元数据不一致时抛出。
 */
public class InvalidFileException extends DocumentImportException {

    public InvalidFileException(String message) {
        super(400, message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(400, message, cause);
    }
}

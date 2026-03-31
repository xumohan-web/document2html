package org.mohan.docimport.exception;

/**
 * 文件解析失败异常。
 */
public class FileConversionException extends DocumentImportException {

    public FileConversionException(String message, Throwable cause) {
        super(422, message, cause);
    }
}

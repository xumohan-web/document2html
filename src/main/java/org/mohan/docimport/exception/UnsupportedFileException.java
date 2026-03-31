package org.mohan.docimport.exception;

/**
 * 不支持的文件类型异常。
 */
public class UnsupportedFileException extends DocumentImportException {

    public UnsupportedFileException(String message) {
        super(415, message);
    }
}

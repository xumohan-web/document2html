package org.mohan.docimport.exception;

/**
 * 远程下载失败异常。
 */
public class FileDownloadException extends DocumentImportException {

    public FileDownloadException(String message) {
        super(422, message);
    }

    public FileDownloadException(String message, Throwable cause) {
        super(422, message, cause);
    }
}

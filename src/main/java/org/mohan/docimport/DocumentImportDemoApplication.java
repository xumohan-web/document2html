package org.mohan.docimport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类，用于启动 document2html 服务。
 */
@SpringBootApplication
public class DocumentImportDemoApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DocumentImportDemoApplication.class, args);
    }
}

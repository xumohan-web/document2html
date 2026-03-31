package org.mohan.docimport.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档导入相关配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "docimport")
public class DocumentImportProperties {

    private Download download = new Download();

    @Data
    public static class Download {

        private Duration connectTimeout = Duration.ofSeconds(5);

        private Duration readTimeout = Duration.ofSeconds(20);

        @DataSizeUnit(DataUnit.BYTES)
        private DataSize maxSize = DataSize.ofMegabytes(20);

        /**
         * 允许访问的主机白名单；为空时表示不启用白名单，仅使用私网限制。
         */
        private List<String> allowedHosts = new ArrayList<>();

        /**
         * 是否允许访问本地回环、私网和链路本地地址。
         */
        private boolean allowPrivateHosts = false;
    }
}

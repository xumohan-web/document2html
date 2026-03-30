package org.mohan.docimport.controller;

import jakarta.annotation.Resource;
import org.mohan.docimport.common.CommonResult;
import org.mohan.docimport.model.ResolveContentRequest;
import org.mohan.docimport.service.DocumentImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档导入相关接口，提供上传文件解析和正文回填两类入口。
 */
@RestController
@RequestMapping("/api")
public class DocumentImportController {

    @Resource
    private DocumentImportService documentImportService;

    /**
     * 接收上传文件并转换为 HTML。
     *
     * @param file 上传文件
     * @return 转换后的 HTML 内容
     */
    @PostMapping(value = "/import-content", consumes = "multipart/form-data")
    public CommonResult<String> importContent(@RequestParam("file") MultipartFile file) {
        return CommonResult.success(documentImportService.importFile(file));
    }

    /**
     * 当正文为空时，尝试通过远程文件地址解析正文内容。
     *
     * @param request 正文解析请求
     * @return 最终可用的正文 HTML
     */
    @PostMapping("/resolve-content")
    public CommonResult<String> resolveContent(@RequestBody ResolveContentRequest request) {
        return CommonResult.success(
                documentImportService.resolveContent(request.getContent(), request.getContentFileUrl())
        );
    }
}

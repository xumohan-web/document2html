package org.mohan.docimport.converter;

import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;

/**
 * 文件内容转换器统一接口，不同文件类型各自实现对应的解析逻辑。
 */
public interface FileContentConverter {

    /**
     * 判断当前转换器是否支持指定文件类型。
     *
     * @param fileType 文件类型
     * @return 是否支持
     */
    boolean supports(FileTypeEnum fileType);

    /**
     * 将文件内容转换为系统内部统一的导入结果。
     *
     * @param context 文档转换上下文
     * @return 导入结果
     * @throws Exception 转换过程中出现的异常
     */
    ImportResult convert(DocumentConvertContext context) throws Exception;
}

package org.mohan.docimport.converter;

import org.apache.poi.ss.usermodel.*;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.util.HtmlBuilderUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 转换器，按 sheet、行、列读取内容并输出为 HTML 表格。
 */
@Component
public class ExcelFileContentConverter implements FileContentConverter {

    @Override
    public boolean supports(FileTypeEnum fileType) {
        return FileTypeEnum.XLS == fileType || FileTypeEnum.XLSX == fileType;
    }

    @Override
    public ImportResult convert(DocumentConvertContext context) throws Exception {
        ImportResult result = new ImportResult();
        try (Workbook workbook = WorkbookFactory.create(context.newInputStream())) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                List<List<String>> rows = new ArrayList<>();
                int maxColumn = 0;
                for (Row row : sheet) {
                    maxColumn = Math.max(maxColumn, row.getLastCellNum());
                }
                maxColumn = Math.max(maxColumn, 1);
                for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    List<String> cells = new ArrayList<>();
                    for (int columnIndex = 0; columnIndex < maxColumn; columnIndex++) {
                        Cell cell = row == null ? null : row.getCell(columnIndex);
                        cells.add(cell == null ? "" : formatter.formatCellValue(cell, evaluator));
                    }
                    rows.add(cells);
                }
                result.getTableHtmlList().add(HtmlBuilderUtils.table(rows));
            }
        }
        return result;
    }
}

package org.mohan.docimport.converter;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.*;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.util.HtmlBuilderUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        StringBuilder html = new StringBuilder("<div>");
        try (Workbook workbook = WorkbookFactory.create(context.newInputStream())) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                int maxColumn = 0;
                for (Row row : sheet) {
                    maxColumn = Math.max(maxColumn, row.getLastCellNum());
                }
                for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
                    maxColumn = Math.max(maxColumn, mergedRegion.getLastColumn() + 1);
                }
                maxColumn = Math.max(maxColumn, 1);
                html.append("<section data-sheet=\"")
                        .append(HtmlBuilderUtils.escape(sheet.getSheetName()))
                        .append("\">");
                html.append("<p><strong>")
                        .append(HtmlBuilderUtils.escape(sheet.getSheetName()))
                        .append("</strong></p>");
                html.append(buildTable(sheet, maxColumn, formatter, evaluator));
                html.append("</section>");
            }
        }
        html.append("</div>");
        return ImportResult.ofHtml(html.toString());
    }

    private String buildTable(Sheet sheet, int maxColumn, DataFormatter formatter, FormulaEvaluator evaluator) {
        int firstRow = sheet.getFirstRowNum();
        int lastRow = Math.max(sheet.getLastRowNum(), firstRow);
        for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
            lastRow = Math.max(lastRow, mergedRegion.getLastRow());
        }

        Map<String, CellRangeAddress> mergedRegionByAnchor = new HashMap<>();
        Map<String, CellRangeAddress> mergedRegionByCoveredCell = new HashMap<>();
        for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
            mergedRegionByAnchor.put(key(mergedRegion.getFirstRow(), mergedRegion.getFirstColumn()), mergedRegion);
            for (int rowIndex = mergedRegion.getFirstRow(); rowIndex <= mergedRegion.getLastRow(); rowIndex++) {
                for (int columnIndex = mergedRegion.getFirstColumn(); columnIndex <= mergedRegion.getLastColumn(); columnIndex++) {
                    mergedRegionByCoveredCell.put(key(rowIndex, columnIndex), mergedRegion);
                }
            }
        }

        StringBuilder builder = new StringBuilder("<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\">");
        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            builder.append("<tr>");
            for (int columnIndex = 0; columnIndex < maxColumn; columnIndex++) {
                String cellKey = key(rowIndex, columnIndex);
                CellRangeAddress mergedRegion = mergedRegionByCoveredCell.get(cellKey);
                if (mergedRegion != null && !cellKey.equals(key(mergedRegion.getFirstRow(), mergedRegion.getFirstColumn()))) {
                    continue;
                }

                Cell cell = row == null ? null : row.getCell(columnIndex);
                String value = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
                builder.append("<td");
                if (mergedRegion != null) {
                    int colspan = mergedRegion.getLastColumn() - mergedRegion.getFirstColumn() + 1;
                    int rowspan = mergedRegion.getLastRow() - mergedRegion.getFirstRow() + 1;
                    if (colspan > 1) {
                        builder.append(" colspan=\"").append(colspan).append("\"");
                    }
                    if (rowspan > 1) {
                        builder.append(" rowspan=\"").append(rowspan).append("\"");
                    }
                }
                builder.append(">")
                        .append(HtmlBuilderUtils.escape(value))
                        .append("</td>");
            }
            builder.append("</tr>");
        }
        builder.append("</table>");
        return builder.toString();
    }

    private String key(int rowIndex, int columnIndex) {
        return rowIndex + ":" + columnIndex;
    }
}

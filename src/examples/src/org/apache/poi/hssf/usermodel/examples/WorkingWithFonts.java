package org.apache.poi.hssf.usermodel.examples;

import org.apache.poi.hssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates how to create and use fonts.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class WorkingWithFonts
{
    public static void main(String[] args)
            throws IOException
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");

        // Create a row and put some cells in it. Rows are 0 based.
        HSSFRow row = sheet.createRow((short) 1);

        // Create a new font and alter it.
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short)24);
        font.setFontName("Courier New");
        font.setItalic(true);
        font.setStrikeout(true);

        // Fonts are set into a style so create a new one to use.
        HSSFCellStyle style = wb.createCellStyle();
        style.setFont(font);

        // Create a cell and put a value in it.
        HSSFCell cell = row.createCell((short) 1);
        cell.setCellValue("This is a test of fonts");
        cell.setCellStyle(style);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();

    }
}

package org.apache.poi.hssf.usermodel.examples;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.Region;

import java.io.IOException;
import java.io.FileOutputStream;

/**
 * An example of how to merge regions of cells.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class MergedCells
{
   public static void main(String[] args)
        throws IOException
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");

        HSSFRow row = sheet.createRow((short) 1);
        HSSFCell cell = row.createCell((short) 1);
        cell.setCellValue("This is a test of merging");

        sheet.addMergedRegion(new Region(1,(short)1,1,(short)2));

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();

    }
}

package org.apache.poi.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Dump out the aggregated escher records
 */
public class DrawingDump
{
    public static void main( String[] args ) throws IOException
    {
        POIFSFileSystem fs      =
                new POIFSFileSystem(new FileInputStream(args[0]));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        sheet.dumpDrawingRecords();
    }
}

package org.apache.poi.xssf.usermodel.examples;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FitSheetToOnePage {


    public static void main(String[]args) throws Exception{
	Workbook wb = new XSSFWorkbook();
	Sheet sheet = wb.createSheet("format sheet");
	PrintSetup ps = sheet.getPrintSetup();

	sheet.setAutobreaks(true);

	ps.setFitHeight((short)1);
	ps.setFitWidth((short)1);


	// Create various cells and rows for spreadsheet.

	FileOutputStream fileOut = new FileOutputStream("fitSheetToOnePage.xlsx");
	wb.write(fileOut);
	fileOut.close();

    }
}

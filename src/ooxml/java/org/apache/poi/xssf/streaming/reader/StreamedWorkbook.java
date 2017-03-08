/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.xssf.streaming.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;

/**
 * Represents the excel workbook
 *
 */
public class StreamedWorkbook implements Workbook{
    
    private static final POILogger logger = POILogFactory.getLogger(StreamedWorkbook.class);
	
	private File inputFile;
	
	private Iterator<InputStream> sheetIterator;
    private SharedStringsTable sharedStringsTable;
    private StylesTable stylesTable;
	
	/**
	 * <pre>
	 * Accepts the file path and return an instance of StreamedWorkBook
	 *</pre>
	 * @param filePath
	 * @throws Exception 
	 */
	public StreamedWorkbook(String filePath) throws Exception{
		if((filePath != null) && !(filePath.trim().isEmpty())){
			inputFile = new File(filePath);
			
			if(inputFile != null){
			    XSSFReader reader = getExcelReader(inputFile);
	            if(reader != null){
	                sheetIterator = reader.getSheetsData();
	                sharedStringsTable = reader.getSharedStringsTable();
	                stylesTable = reader.getStylesTable();
	            }
	        }
			
		}else{
            throw new Exception("No sheets found");
        }
	}
	
	/**
	 * <pre>
	 * Fetch all sheets from given excel file
	 * </pre>
	 * @return
	 * @throws Exception
	 */
	public Iterator<StreamedSheet> getSheetIterator() throws Exception{
	    
	    return getAllSheets();
	}
	
	
	 /**
     * Returns the list of sheets for the given excel file
     * @param file
     * @return
     * @throws Exception 
     */
    private Iterator<StreamedSheet>  getAllSheets() throws Exception{
        
        return getStreamedSheetIterator();
    }
    
    private Iterator<StreamedSheet> getStreamedSheetIterator() throws Exception{
        List<StreamedSheet> sheetList = null;
        XMLInputFactory factory = null; 
        
        if(sheetIterator != null){
            factory = XMLInputFactory.newInstance();
            sheetList =  new ArrayList<StreamedSheet>();
            int sheetNumber = 0;
            while(sheetIterator.hasNext()){
                InputStream sheetInputStream = sheetIterator.next();
                StreamedSheet sheet = createStreamedSheet(factory.createXMLEventReader(sheetInputStream), sheetNumber);
                sheetList.add(sheet);
                sheetNumber++;
            }
        }else{
            throw new Exception("Workbook already closed");
        }
        
        return sheetList.iterator();
        
    }
    
    /**
     * Creates and returns the instance of StreamedSheet
     *
     * @param parser
     * @param sheetNumber
     * @return
     */
    private StreamedSheet createStreamedSheet(XMLEventReader parser,int sheetNumber){
        StreamedSheet sheet = new StreamedSheet();
        sheet.setXmlParser(parser);
        sheet.setSharedStringsTable(sharedStringsTable);
        sheet.setStylesTable(stylesTable);
        sheet.setSheetNumber(sheetNumber);
        sheet.createEventHandler();
        
        return sheet;
    }
    
    
    
    /**
     * Receives the excel file and returns the file excel file reader
     * @param inputStream
     * @return
     * @throws Exception
     */
    private XSSFReader getExcelReader(File file) throws Exception{
        XSSFReader reader = null;
        OPCPackage pkg = null;
        pkg = OPCPackage.open(file);
        reader = new XSSFReader(pkg);
        return reader;
    }
    
    
    
    /*
     *  TO DO 
     */

    
    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public int getActiveSheetIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void setActiveSheet(int sheetIndex) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public int getFirstVisibleTab() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public void setFirstVisibleTab(int sheetIndex) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public void setSheetOrder(String sheetname, int pos) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public void setSelectedTab(int index) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void setSheetName(int sheet, String name) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */    
    public String getSheetName(int sheet) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public int getSheetIndex(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public int getSheetIndex(Sheet sheet) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */    
    public Sheet createSheet() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public Sheet createSheet(String sheetname) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public Sheet cloneSheet(int sheetNum) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Use Iterator<StreamedSheet> getSheetIterator() instead.
     * </pre>
     * 
     */
    public Iterator<Sheet> sheetIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     *  Returns the number of sheets,
     * </pre>
     */
    public int getNumberOfSheets() {
        int sheetCount = 0;
        
        if(this.sheetIterator != null){
            while(sheetIterator.hasNext()){
                sheetIterator.next();
                sheetCount++;
            }
        }else{
            logger.log(POILogger.ERROR, "Workbook already closed");
        }
        
        
        return sheetCount;
    }

    /**
     * <pre>
     *  Currently not supported due to memory footprint.
     *  Will be supported in future.
     * </pre>
     */
    public Sheet getSheetAt(int index) {
        
/*        StreamedSheet sheet = null;
        int sheetCount = 0;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        
        if(inputFile != null && inputFile.exists()){
            try {
                XSSFReader reader = getExcelReader(inputFile);
                
                
                
                while(reader.getSheetsData().hasNext()){
                    
                    if(index == sheetCount){
                        sheet = createStreamedSheet(factory.createXMLEventReader(reader.getSheetsData().next()), sheetCount);
                    }else{
                        reader.getSheetsData().next();
                    }
                    
                    sheetCount++;
                }
                
            } catch (Exception e) {
                logger.log(POILogger.ERROR, "No sheets found !!");
            }
        }
        return sheet;*/
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public Sheet getSheet(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public void removeSheetAt(int index) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public Font createFont() {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public Font findFont(short boldWeight, short color, short fontHeight,
            String name, boolean italic, boolean strikeout, short typeOffset,
            byte underline) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public Font findFont(boolean bold, short color, short fontHeight,
            String name, boolean italic, boolean strikeout, short typeOffset,
            byte underline) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public short getNumberOfFonts() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public Font getFontAt(short idx) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */
    public CellStyle createCellStyle() {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public int getNumCellStyles() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public CellStyle getCellStyleAt(int idx) {
        // TODO Auto-generated method stub
        return null;
    }

    
    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */    
    public void write(OutputStream stream) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Close the workbook
     * </pre>
     * 
     */    
    public void close() throws IOException {
        if(sheetIterator != null){
            while(sheetIterator.hasNext()){
                sheetIterator.next().close();
            }
            sheetIterator = null;
        }
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */    
    public int getNumberOfNames() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public Name getName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public List<? extends Name> getNames(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public List<? extends Name> getAllNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */
    public Name getNameAt(int nameIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    
    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */       
    public Name createName() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */    
    public int getNameIndex(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */   
    public void removeName(int index) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */   
    public void removeName(String name) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */   
    public void removeName(Name name) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */      
    public int linkExternalWorkbook(String name, Workbook workbook) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */   
    public void setPrintArea(int sheetIndex, String reference) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */   
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn,
            int startRow, int endRow) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */ 
    public String getPrintArea(int sheetIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */      
    public void removePrintArea(int sheetIndex) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */ 
    public MissingCellPolicy getMissingCellPolicy() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */  
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */  
    public DataFormat createDataFormat() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */  
    public int addPicture(byte[] pictureData, int format) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * <pre>
     * Not supported due to memory footprint.
     * </pre>
     * 
     */ 
    public List<? extends PictureData> getAllPictures() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */ 
    public CreationHelper getCreationHelper() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */ 
    public boolean isHidden() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */  
    public void setHidden(boolean hiddenFlag) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */ 
    public boolean isSheetHidden(int sheetIx) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */ 
    public boolean isSheetVeryHidden(int sheetIx) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setSheetHidden(int sheetIx, boolean hidden) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setSheetHidden(int sheetIx, int hidden) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void addToolPack(UDFFinder toopack) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     */ 
    public void setForceFormulaRecalculation(boolean value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */     
    public boolean getForceFormulaRecalculation() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */ 
    public SpreadsheetVersion getSpreadsheetVersion() {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * <pre>
     * Will be supported in the future.
     * </pre>
     * 
     */ 
    public Iterator<Sheet> iterator() {
        return null;
    }
}

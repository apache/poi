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

package org.apache.poi.ss.excelant.util;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.DefaultUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Typedef;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A general utility class that abstracts the POI details of loading the
 * workbook, accessing and updating cells.
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 * 
 */
public class ExcelAntWorkbookUtil extends Typedef {

    private String excelFileName;

    private Workbook workbook;

    private HashMap<String, FreeRefFunction> xlsMacroList;
    
    /**
     * Constructs an instance using a String that contains the fully qualified
     * path of the Excel file. This constructor initializes a Workbook instance
     * based on that file name.
     * 
     * @param fName
     */
    protected ExcelAntWorkbookUtil(String fName) {
        excelFileName = fName;
        xlsMacroList = new HashMap<String, FreeRefFunction>() ;
        loadWorkbook();
        
    }

    /**
     * Constructs an instance based on a Workbook instance.
     * 
     * @param wb
     */
    protected ExcelAntWorkbookUtil(Workbook wb) {
        workbook = wb;
        xlsMacroList = new HashMap<String, FreeRefFunction>() ;
    }

    /**
     * Loads the member variable workbook based on the fileName variable.
     * @return
     */
    private Workbook loadWorkbook() {

        File workbookFile = new File(excelFileName);
        try {
            FileInputStream fis = new FileInputStream(workbookFile);
            workbook = WorkbookFactory.create(fis);
        } catch(Exception e) {
            throw new BuildException("Cannot load file " + excelFileName
                    + ". Make sure the path and file permissions are correct.", e);
        }

        return workbook ;
    }
    
    /**
     * Used to add a UDF to the evaluator.  
     * @param name
     * @param clazzName
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void addFunction( String name, String clazzName ) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class clazzInst = Class.forName( clazzName ) ;
        Object newInst = clazzInst.newInstance() ;
        if( newInst instanceof FreeRefFunction ) {
            addFunction( name, (FreeRefFunction)newInst ) ;
        }
        
    }
 
    /**
     * Updates the internal HashMap of functions with instance and alias passed
     * in.
     * 
     * @param name
     * @param func
     */
    protected void addFunction(String name, FreeRefFunction func) {
        xlsMacroList.put(name, func);
    }

    /**
     * returns a UDFFinder that contains all of the functions added.
     * 
     * @return
     */
    protected UDFFinder getFunctions() {

        String[] names = new String[xlsMacroList.size()];
        FreeRefFunction[] functions = new FreeRefFunction[xlsMacroList.size()];

        Iterator<String> keysIt = xlsMacroList.keySet().iterator();
        int x = 0;
        while (keysIt.hasNext()) {
            String name = keysIt.next();
            FreeRefFunction function = xlsMacroList.get(name);
            names[x] = name;
            functions[x] = function;
        }

        UDFFinder udff1 = new DefaultUDFFinder(names, functions);
        UDFFinder udff = new AggregatingUDFFinder(udff1);

        return udff;

    }
    
    /**
     * Returns a formula evaluator that is loaded with the functions that
     * have been supplied.
     * 
     * @param excelFileName
     * @return
     */
    protected FormulaEvaluator getEvaluator( String excelFileName ) {
        FormulaEvaluator evaluator ;
        if (excelFileName.endsWith(".xlsx")) {
            if( xlsMacroList != null && xlsMacroList.size() > 0 ) {
                evaluator = XSSFFormulaEvaluator.create( (XSSFWorkbook) workbook,
                                                         null,
                                                         getFunctions() ) ;
            }
            evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) workbook);
        } else {
            if( xlsMacroList != null && xlsMacroList.size() > 0 ) {
                evaluator = HSSFFormulaEvaluator.create( (HSSFWorkbook)workbook,
                                                         null,
                                                         getFunctions() ) ;
            }

            evaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
        }

        return evaluator ;

    }

    /**
     * Returns the Workbook instance associated with this WorkbookUtil.
     * 
     * @return
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * Returns the fileName that was used to initialize this instance. May
     * return null if the instance was constructed from a Workbook object.
     * 
     * @return
     */
    public String getFileName() {
        return excelFileName;
    }
    
    /**
     * Returns the list of sheet names.
     * 
     * @return
     */
    public ArrayList<String> getSheets() {
    	ArrayList<String> sheets = new ArrayList<String>() ;
    	
    	int sheetCount = workbook.getNumberOfSheets() ;
    	
    	for( int x=0; x<sheetCount; x++ ) {
    		sheets.add( workbook.getSheetName( x ) ) ;
    	}
    	
    	return sheets ;
    }

    /**
     * This method uses a String in standard Excel format (SheetName!CellId) to
     * locate the cell and set it to the value of the double in value.
     * 
     * @param cellName
     * @param value
     */
    public void setDoubleValue(String cellName, double value) {
        log("starting setCellValue()", Project.MSG_DEBUG);
        Cell cell = getCell(cellName);
        log("working on cell: " + cell, Project.MSG_DEBUG);
        cell.setCellValue(value);
        log("after cell.setCellValue()", Project.MSG_DEBUG);

        log("set cell " + cellName + " to value " + value, Project.MSG_DEBUG);
    }

    /**
     * Utility method for setting the value of a Cell with a String.
     * 
     * @param cellName
     * @param value
     */
    public void setStringValue( String cellName, String value ) {
        Cell cell = getCell(cellName);
        cell.setCellValue(value);        
    }
    
    /**
     * Utility method for setting the value of a Cell with a Formula.
     * 
     * @param cellName
     * @param formula
     */
    public void setFormulaValue( String cellName, String formula ) {
        Cell cell = getCell(cellName);
        cell.setCellFormula( formula );
    }
    
    /**
     * Utility method for setting the value of a Cell with a Date.
     * @param cellName
     * @param date
     */
    public void setDateValue( String cellName, Date date  ) {
        Cell cell = getCell(cellName);
        cell.setCellValue( date ) ;
    }
    /**
     * Uses a String in standard Excel format (SheetName!CellId) to locate a
     * cell and evaluate it.
     * 
     * @param cellName
     * @param expectedValue
     * @param precision
     */
    public ExcelAntEvaluationResult evaluateCell(String cellName, double expectedValue,
            double precision) {

        ExcelAntEvaluationResult evalResults = null;

        Cell cell = getCell(cellName);

        FormulaEvaluator evaluator = getEvaluator( excelFileName );


        CellValue resultOfEval = evaluator.evaluate(cell);

        if (resultOfEval.getErrorValue() == 0) {
            // the evaluation did not encounter errors
            double result = resultOfEval.getNumberValue();
            double delta = Math.abs(result - expectedValue);
            if (delta > precision) {
                evalResults = new ExcelAntEvaluationResult(false, false,
                        resultOfEval.getNumberValue(),
                        "Results was out of range based on precision " + " of "
                                + precision + ".  Delta was actually " + delta, delta, cellName );
            } else {
                evalResults = new ExcelAntEvaluationResult(false, true,
                        resultOfEval.getNumberValue(),
                        "Evaluation passed without error within in range.", delta, cellName );
            }
        } else {
            String errorMeaning = null ;
            try {
                errorMeaning = ErrorConstants.getText( resultOfEval
                        .getErrorValue() ) ;
            } catch( IllegalArgumentException iae ) {
                errorMeaning =  "unknown error code: " + 
                                Byte.toString( resultOfEval.getErrorValue() ) ; 
            }
            
            evalResults = new ExcelAntEvaluationResult(false, false,
                    resultOfEval.getNumberValue(),
                    "Evaluation failed due to an evaluation error of "
                            + resultOfEval.getErrorValue()
                            + " which is "
                            + errorMeaning, 0, cellName );
        }

        return evalResults;
    }

    /**
     * Returns a Cell as a String value.
     * 
     * @param cellName
     * @return
     */
    public String getCellAsString( String cellName ) {
    	Cell cell = getCell( cellName ) ;
    	if( cell != null ) {
    		return cell.getStringCellValue() ;
    	}
    	return "" ;
    }
    
    
    /**
     * Returns the value of the Cell as a double.
     * 
     * @param cellName
     * @return
     */
    public double getCellAsDouble( String cellName ) {
    	Cell cell = getCell( cellName ) ;
    	if( cell != null ) {
    		return cell.getNumericCellValue() ;
    	}
    	return 0.0 ;
    }
    /**
     * Returns a cell reference based on a String in standard Excel format
     * (SheetName!CellId).  This method will create a new cell if the
     * requested cell isn't initialized yet.
     * 
     * @param cellName
     * @return
     */
    private Cell getCell(String cellName) {
        
        CellReference cellRef = new CellReference(cellName);
        String sheetName = cellRef.getSheetName();
        Sheet sheet = workbook.getSheet(sheetName);
        if(sheet == null) {
            throw new BuildException("Sheet not found: " + sheetName);
        }

        int rowIdx = cellRef.getRow();
        int colIdx = cellRef.getCol();
        Row row = sheet.getRow(rowIdx);
        
        if( row == null ) {
        	row = sheet.createRow( rowIdx ) ;
        }
        
        Cell cell = row.getCell(colIdx);
        
        if( cell == null ) {
        	cell = row.createCell( colIdx ) ;
        }

        return cell;
    }

}

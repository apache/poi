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

package org.apache.poi.ss.excelant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.poi.ss.excelant.util.ExcelAntWorkbookUtil;
import org.apache.poi.ss.excelant.util.ExcelAntWorkbookUtilFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Ant task class for testing Excel workbook cells.
 */
public class ExcelAntTask extends Task {

    public static final String VERSION = "0.5.0" ;

	private String excelFileName ;

	private boolean failOnError;

	private ExcelAntWorkbookUtil workbookUtil ;

	private ExcelAntPrecision precision ;

	private LinkedList<ExcelAntTest> tests ;
	private LinkedList<ExcelAntUserDefinedFunction> functions ;

	public ExcelAntTask() {
		tests = new LinkedList<>() ;
		functions = new LinkedList<>() ;
	}

	public void addPrecision( ExcelAntPrecision prec ) {
		precision = prec ;
	}

	public void setFailOnError( boolean value ) {
		failOnError = value ;
	}
	public void setFileName( String fileName ) {
		excelFileName = fileName ;
	}

	public void addTest( ExcelAntTest testElement ) {
		tests.add( testElement ) ;
	}

	public void addUdf( ExcelAntUserDefinedFunction def ) {
		functions.add( def ) ;
	}

	@Override
    public void execute() throws BuildException {
        checkClassPath();

		int totalCount = 0 ;
		int successCount = 0 ;

		StringBuilder versionBffr = new StringBuilder() ;
		versionBffr.append(  "ExcelAnt version " ) ;
		versionBffr.append( VERSION ) ;
		versionBffr.append( " Copyright 2011" ) ;
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy", Locale.ROOT ) ;
		double currYear = Double.parseDouble( sdf.format( new Date() ) );
		if( currYear > 2011 ) {
		    versionBffr.append( "-" ) ;
		    versionBffr.append( currYear ) ;
		}
		log( versionBffr.toString(), Project.MSG_INFO ) ;

		log( "Using input file: " + excelFileName, Project.MSG_INFO ) ;

        workbookUtil = ExcelAntWorkbookUtilFactory.getInstance(excelFileName);

		for (ExcelAntTest test : tests) {
			log("executing test: " + test.getName(), Project.MSG_DEBUG);

			if (workbookUtil == null) {
			    workbookUtil = ExcelAntWorkbookUtilFactory.getInstance(excelFileName);
			}

			for (ExcelAntUserDefinedFunction eaUdf : functions) {
				try {
					workbookUtil.addFunction(eaUdf.getFunctionAlias(), eaUdf.getClassName());
				} catch (Exception e) {
					throw new BuildException(e.getMessage(), e);
				}
			}
			test.setWorkbookUtil(workbookUtil);

			if (precision != null && precision.getValue() > 0) {
				log("setting precision for the test " + test.getName(), Project.MSG_VERBOSE);
				test.setPrecision(precision.getValue());
			}

			test.execute();

			if (test.didTestPass()) {
				successCount++;
			} else {
				if (failOnError) {
					throw new BuildException("Test " + test.getName() + " failed.");
				}
			}
			totalCount++;

			workbookUtil = null;
		}

		if( !tests.isEmpty() ) {
		    log( successCount + "/" + totalCount + " tests passed.", Project.MSG_INFO );
		}
        workbookUtil = null;
	}


    /**
     * ExcelAnt depends on external libraries not included in the Ant distribution.
     * Give user a sensible message if any if the required jars are missing.
     */
    private void checkClassPath(){
        try {
            Class.forName("org.apache.poi.hssf.usermodel.HSSFWorkbook");
            Class.forName("org.apache.poi.ss.usermodel.WorkbookFactory");
        } catch (Exception e) {
            throw new BuildException(
                    "The <classpath> for <excelant> must include poi.jar and poi-ooxml.jar " +
                    "if not in Ant's own classpath. Processing .xlsx spreadsheets requires " +
                    "additional poi-ooxml-lite.jar, xmlbeans.jar" ,
                    e, getLocation());
        }

    }
}

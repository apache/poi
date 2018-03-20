/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.poi.ss.excelant;


/**
 *  JUnit test for the ExcelAnt tasks.
 *  Leverages Ant's test framework.
 *
 * @see <a href="http://svn.apache.org/repos/asf/ant/core/trunk/src/tests/junit/org/apache/tools/ant/BuildFileTest.java">
 *  http://svn.apache.org/repos/asf/ant/core/trunk/src/tests/junit/org/apache/tools/ant/BuildFileTest.java</a>
 */
public class TestBuildFile extends BuildFileTest {

    @Override
    public void setUp() {
        configureProject(BuildFileTest.getDataDir() + "/../src/excelant/testcases/org/apache/poi/ss/excelant/tests.xml");
    }

    public void testMissingFilename() {
        expectSpecificBuildException("test-nofile", "required argument not specified",
                "fileName attribute must be set!");
     }

    public void testFileNotFound() {
        expectSpecificBuildException("test-filenotfound", "required argument not specified",
                "Cannot load file invalid.xls. Make sure the path and file permissions are correct.");
     }

    public void testEvaluate() {
        executeTarget("test-evaluate");
        assertLogContaining("Using input file: " + BuildFileTest.getDataDir() + "/spreadsheet/excelant.xls");
        assertLogContaining("Succeeded when evaluating 'MortgageCalculator'!$B$4.");
    }

    public void testEvaluateNoDetails() {
        executeTarget("test-evaluate-nodetails");
        assertLogContaining("Using input file: " + BuildFileTest.getDataDir() + "/spreadsheet/excelant.xls");
        assertLogNotContaining("Succeeded when evaluating 'MortgageCalculator'!$B$4.");
    }

    public void testPrecision() {
        executeTarget("test-precision");

        assertLogContaining("Using input file: " + BuildFileTest.getDataDir() + "/spreadsheet/excelant.xls");
        assertLogContaining("Succeeded when evaluating 'MortgageCalculator'!$B$4.  " +
                "It evaluated to 2285.5761494145563 when the value of 2285.576149 with precision of 1.0E-4");
        assertLogContaining("Succeeded when evaluating 'MortgageCalculator'!$B$4.  " +
                "It evaluated to 2285.5761494145563 when the value of 2285.576149 with precision of 1.0E-5");
        assertLogContaining("Failed to evaluate cell 'MortgageCalculator'!$B$4.  " +
                "It evaluated to 2285.5761494145563 when the value of 2285.576149 with precision of 1.0E-10 was expected.");
        assertLogContaining("2/3 tests passed");
    }

    public void testPrecisionFail() {
        expectSpecificBuildException("test-precision-fails", "precision not matched",
                "\tFailed to evaluate cell 'MortgageCalculator'!$B$4.  It evaluated to 2285.5761494145563 when the value of 2285.576149 with precision of 1.0E-10 was expected.");
    }

    public void testPassOnError() {
        executeTarget("test-passonerror");
    }

    public void testFailOnError() {
        expectBuildException("test-failonerror", "fail on error");
        assertLogContaining("Using input file: " + BuildFileTest.getDataDir() + "/spreadsheet/excelant.xls");
        assertLogNotContaining("failed because 1 of 0 evaluations failed to evaluate correctly. Failed to evaluate cell 'MortageCalculatorFunction'!$D$3");
    }

    public void testFailOnErrorNoDetails() {
        expectBuildException("test-failonerror-nodetails", "fail on error");
        assertLogNotContaining("Using input file: " + BuildFileTest.getDataDir() + "/spreadsheet/excelant.xls");
        assertLogNotContaining("failed because 1 of 0 evaluations failed to evaluate correctly. Failed to evaluate cell 'MortageCalculatorFunction'!$D$3");
    }

    public void testUdf() {
        executeTarget("test-udf");
        assertLogContaining("1/1 tests passed");
    }
    
    public void testSetText() {
        executeTarget("test-settext");
        assertLogContaining("1/1 tests passed");
    }
    
    public void testAddHandler() {
        executeTarget("test-addhandler");
        assertLogContaining("Using input file: " + BuildFileTest.getDataDir() + "/spreadsheet/excelant.xls");
        assertLogContaining("Succeeded when evaluating 'MortgageCalculator'!$B$4.");
        
        assertNotNull("The workbook should have been passed to the handler", MockExcelAntWorkbookHandler.workbook);
        assertTrue("The handler should have been executed", MockExcelAntWorkbookHandler.executed);
    }
    
    public void testAddHandlerWrongClass() {
        executeTarget("test-addhandler-wrongclass");
        assertLogContaining("Using input file: " + BuildFileTest.getDataDir() + "/spreadsheet/excelant.xls");
        assertLogContaining("Succeeded when evaluating 'MortgageCalculator'!$B$4.");
    }
    
    public void testAddHandlerFails() {
        expectSpecificBuildException("test-addhandler-fails", "NullPointException", null);
    }
}

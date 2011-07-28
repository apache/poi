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

import org.apache.poi.ss.excelant.util.ExcelAntWorkbookUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * This is the class that backs the <handler> tag in the Ant task.
 * <p>
 * Its purpose is to provide a way to manipulate a workbook in the course
 * of an ExcelAnt task.  The idea being to model a way for test writers to
 * simulate the behaviors of the workbook. 
 * <p>
 * Suppose, for example, you have a workbook that has a worksheet that
 * reacts to values entered or selected by the user.  It's possible in
 * Excel to change other cells based on this but this isn't easily possible
 * in POI.  In ExcelAnt we handle this using the Handler, which is a Java
 * class you write to manipulate the workbook. 
 * <p>
 * In order to use this tag you must write a class that implements the 
 * <code>IExcelAntWorkbookHandler</code> interface.  After writing the
 * class you should package it and it's dependencies into a jar file to 
 * add as library in your Ant build file.
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 *
 */
public class ExcelAntHandlerTask extends Task {
    
    private String className ;
    
    private ExcelAntWorkbookUtil wbUtil ;

    public void setClassName( String cName ) {
        className = cName ;
    }
    
    protected void setEAWorkbookUtil( ExcelAntWorkbookUtil wkbkUtil ) {
        wbUtil = wkbkUtil ;
    }
    
    public void execute() throws BuildException {
        log( "handling the workbook with class " + className, Project.MSG_INFO ) ;
        try {
            Class clazz = Class.forName( className ) ;
            Object handlerObj = clazz.newInstance() ;
            if( handlerObj instanceof IExcelAntWorkbookHandler ) {
                IExcelAntWorkbookHandler iHandler = (IExcelAntWorkbookHandler)handlerObj ;
                iHandler.setWorkbook( wbUtil.getWorkbook() ) ;
                iHandler.execute() ;
             }
        } catch( Exception e ) {
            throw new BuildException( e.getMessage(), e ) ;
        }
    }
 }

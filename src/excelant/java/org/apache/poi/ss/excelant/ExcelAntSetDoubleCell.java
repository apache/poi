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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Class for use in an Ant build script that sets the value of an Excel
 * sheet cell using the cell id ('Sheet Name'!cellId).
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 * 
 */
public class ExcelAntSetDoubleCell extends ExcelAntSet {
	
	
	private double cellValue ;
	
	public ExcelAntSetDoubleCell() {}
	
	/**
	 * Set the value of the specified cell as the double passed in.
	 * @param value
	 */
	public void setValue( double value ) {
		cellValue = value ;
	}

	/**
	 * Return the cell value as a double.
	 * @return
	 */
	public double getCellValue() {
		return cellValue;
	}
	
	public void execute() throws BuildException {

		wbUtil.setDoubleValue(cellStr, cellValue ) ;
		
		log( "set cell " + cellStr + " to value " + cellValue + " as double.", Project.MSG_DEBUG ) ;
	}
}

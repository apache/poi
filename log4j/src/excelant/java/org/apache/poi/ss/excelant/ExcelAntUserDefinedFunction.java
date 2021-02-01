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

import org.apache.tools.ant.taskdefs.Typedef;

/**
 * This class encapsulates the Strings necessary to create the User Defined
 * Function instances that will be passed to POI's Evaluator instance.
 *
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 *
 */
public class ExcelAntUserDefinedFunction extends Typedef {


	private String functionAlias ;

	private String className ;


	public ExcelAntUserDefinedFunction() {}

	protected String getFunctionAlias() {
		return functionAlias;
	}

	public void setFunctionAlias(String functionAlias) {
		this.functionAlias = functionAlias;
	}

	protected String getClassName() {
	    // workaround for IBM JDK assigning the classname to the lowercase instance provided by Definer!?!
	    // I could not find out why that happens, the wrong assignment seems to be done somewhere deep inside Ant itself
	    // or even in IBM JDK as Oracle JDK does not have this problem.
	    if(className == null) {
	        return getClassname();
	    }

		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}

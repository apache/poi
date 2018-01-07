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

package org.apache.poi.ss.formula.eval;

import org.apache.poi.ss.usermodel.FormulaEvaluator;

/**
 * An exception thrown by implementors of {@link FormulaEvaluator},
 *  when attempting to evaluate a formula which requires features
 *   that POI does not (yet) support.
 * 
 * <p>Where possible, a subclass of this should be thrown, to provide
 *  more detail of what part of the formula couldn't be processed due
 *  to a missing implementation
 */
public class NotImplementedException extends RuntimeException {
    private static final long serialVersionUID = -5840703336495141301L;
    
    public NotImplementedException(String message) {
		super(message);
	}
	public NotImplementedException(String message, NotImplementedException cause) {
		super(message, cause);
	}
}

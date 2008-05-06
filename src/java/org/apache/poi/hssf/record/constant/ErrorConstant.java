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

package org.apache.poi.hssf.record.constant;

import org.apache.poi.hssf.usermodel.HSSFErrorConstants;
/**
 * Represents a constant error code value as encoded in a constant values array. <p/>
 * 
 * This class is a type-safe wrapper for a 16-bit int value performing a similar job to 
 * <tt>ErrorEval</tt>.
 * 
 * @author Josh Micich
 */
public class ErrorConstant {
	// convenient access to name space
	private static final HSSFErrorConstants EC = null;

	private static final ErrorConstant NULL = new ErrorConstant(EC.ERROR_NULL);
	private static final ErrorConstant DIV_0 = new ErrorConstant(EC.ERROR_DIV_0);
	private static final ErrorConstant VALUE = new ErrorConstant(EC.ERROR_VALUE);
	private static final ErrorConstant REF = new ErrorConstant(EC.ERROR_REF);
	private static final ErrorConstant NAME = new ErrorConstant(EC.ERROR_NAME);
	private static final ErrorConstant NUM = new ErrorConstant(EC.ERROR_NUM);
	private static final ErrorConstant NA = new ErrorConstant(EC.ERROR_NA);

	private final int _errorCode;

	private ErrorConstant(int errorCode) {
		_errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return _errorCode;
	}
	public String getText() {
		if(HSSFErrorConstants.isValidCode(_errorCode)) {
			return HSSFErrorConstants.getText(_errorCode);
		}
		return "unknown error code (" + _errorCode + ")";
	}

	public static ErrorConstant valueOf(int errorCode) {
		switch (errorCode) {
			case HSSFErrorConstants.ERROR_NULL:  return NULL;
			case HSSFErrorConstants.ERROR_DIV_0: return DIV_0;
			case HSSFErrorConstants.ERROR_VALUE: return VALUE;
			case HSSFErrorConstants.ERROR_REF:   return REF;
			case HSSFErrorConstants.ERROR_NAME:  return NAME;
			case HSSFErrorConstants.ERROR_NUM:   return NUM;
			case HSSFErrorConstants.ERROR_NA:	return NA;
		}
		System.err.println("Warning - unexpected error code (" + errorCode + ")");
		return new ErrorConstant(errorCode);
	}
	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(getText());
		sb.append("]");
		return sb.toString();
	}
}

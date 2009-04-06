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

package org.apache.poi.ss.formula;

import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.ControlPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.MemAreaPtg;
import org.apache.poi.hssf.record.formula.MemFuncPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RangePtg;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.record.formula.ValueOperatorPtg;

/**
 * This class performs 'operand class' transformation. Non-base tokens are classified into three 
 * operand classes:
 * <ul>
 * <li>reference</li> 
 * <li>value</li> 
 * <li>array</li> 
 * </ul>
 * <p/>
 * 
 * The final operand class chosen for each token depends on the formula type and the token's place
 * in the formula. If POI gets the operand class wrong, Excel <em>may</em> interpret the formula
 * incorrectly.  This condition is typically manifested as a formula cell that displays as '#VALUE!',
 * but resolves correctly when the user presses F2, enter.<p/>
 * 
 * The logic implemented here was partially inspired by the description in
 * "OpenOffice.org's Documentation of the Microsoft Excel File Format".  The model presented there
 * seems to be inconsistent with observed Excel behaviour (These differences have not been fully
 * investigated). The implementation in this class has been heavily modified in order to satisfy
 * concrete examples of how Excel performs the same logic (see TestRVA).<p/>
 * 
 * Hopefully, as additional important test cases are identified and added to the test suite, 
 * patterns might become more obvious in this code and allow for simplification.
 * 
 * @author Josh Micich
 */
final class OperandClassTransformer {

	private final int _formulaType;

	public OperandClassTransformer(int formulaType) {
		_formulaType = formulaType;
	}

	/**
	 * Traverses the supplied formula parse tree, calling <tt>Ptg.setClass()</tt> for each non-base
	 * token to set its operand class.
	 */
	public void transformFormula(ParseNode rootNode) {
		byte rootNodeOperandClass;
		switch (_formulaType) {
			case FormulaType.CELL:
				rootNodeOperandClass = Ptg.CLASS_VALUE;
				break;
            case FormulaType.NAMEDRANGE:
			case FormulaType.DATAVALIDATION_LIST:
				rootNodeOperandClass = Ptg.CLASS_REF;
				break;
			default:
				throw new RuntimeException("Incomplete code - formula type (" 
						+ _formulaType + ") not supported yet");
		
		}
		transformNode(rootNode, rootNodeOperandClass, false);
	}

	/**
	 * @param callerForceArrayFlag <code>true</code> if one of the current node's parents is a 
	 * function Ptg which has been changed from default 'V' to 'A' type (due to requirements on
	 * the function return value).
	 */
	private void transformNode(ParseNode node, byte desiredOperandClass,
			boolean callerForceArrayFlag) {
		Ptg token = node.getToken();
		ParseNode[] children = node.getChildren();
		boolean isSimpleValueFunc = isSimpleValueFunction(token);
		
		if (isSimpleValueFunc) {
			boolean localForceArray = desiredOperandClass == Ptg.CLASS_ARRAY;
			for (int i = 0; i < children.length; i++) {
				transformNode(children[i], desiredOperandClass, localForceArray);
			}
			setSimpleValueFuncClass((AbstractFunctionPtg) token, desiredOperandClass, callerForceArrayFlag);
			return;
		}
		
		if (isSingleArgSum(token)) {
			// Need to process the argument of SUM with transformFunctionNode below
			// so make a dummy FuncVarPtg for that call.
			token = new FuncVarPtg("SUM", (byte)1);
			// Note - the tAttrSum token (node.getToken()) is a base 
			// token so does not need to have its operand class set
		}
		if (token instanceof ValueOperatorPtg || token instanceof ControlPtg
				|| token instanceof MemFuncPtg
				|| token instanceof MemAreaPtg
				|| token instanceof UnionPtg) {
			// Value Operator Ptgs and Control are base tokens, so token will be unchanged
			// but any child nodes are processed according to desiredOperandClass and callerForceArrayFlag
			
			// As per OOO documentation Sec 3.2.4 "Token Class Transformation", "Step 1"
			// All direct operands of value operators that are initially 'R' type will 
			// be converted to 'V' type.
			byte localDesiredOperandClass = desiredOperandClass == Ptg.CLASS_REF ? Ptg.CLASS_VALUE : desiredOperandClass;
			for (int i = 0; i < children.length; i++) {
				transformNode(children[i], localDesiredOperandClass, callerForceArrayFlag);
			}
			return;
		}
		if (token instanceof AbstractFunctionPtg) {
			transformFunctionNode((AbstractFunctionPtg) token, children, desiredOperandClass, callerForceArrayFlag);
			return;
		}
		if (children.length > 0) {
			if (token == RangePtg.instance) {
				// TODO is any token transformation required under the various ref operators?
				return;
			}
			throw new IllegalStateException("Node should not have any children");
		}

		if (token.isBaseToken()) {
			// nothing to do
			return;
		}
		token.setClass(transformClass(token.getPtgClass(), desiredOperandClass, callerForceArrayFlag));
	}

	private static boolean isSingleArgSum(Ptg token) {
		if (token instanceof AttrPtg) {
			AttrPtg attrPtg = (AttrPtg) token;
			return attrPtg.isSum(); 
		}
		return false;
	}

	private static boolean isSimpleValueFunction(Ptg token) {
		if (token instanceof AbstractFunctionPtg) {
			AbstractFunctionPtg aptg = (AbstractFunctionPtg) token;
			if (aptg.getDefaultOperandClass() != Ptg.CLASS_VALUE) {
				return false;
			}
			int numberOfOperands = aptg.getNumberOfOperands();
			for (int i=numberOfOperands-1; i>=0; i--) {
				if (aptg.getParameterClass(i) != Ptg.CLASS_VALUE) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private byte transformClass(byte currentOperandClass, byte desiredOperandClass,
			boolean callerForceArrayFlag) {
		switch (desiredOperandClass) {
			case Ptg.CLASS_VALUE:
				if (!callerForceArrayFlag) {
					return Ptg.CLASS_VALUE;
				}
				// else fall through
			case Ptg.CLASS_ARRAY:
				return Ptg.CLASS_ARRAY; 
			case Ptg.CLASS_REF:
				if (!callerForceArrayFlag) {
					return currentOperandClass;
				}
				return Ptg.CLASS_REF; 
		}
		throw new IllegalStateException("Unexpected operand class (" + desiredOperandClass + ")");
	}

	private void transformFunctionNode(AbstractFunctionPtg afp, ParseNode[] children,
			byte desiredOperandClass, boolean callerForceArrayFlag) {

		boolean localForceArrayFlag;
		byte defaultReturnOperandClass = afp.getDefaultOperandClass();

		if (callerForceArrayFlag) {
			switch (defaultReturnOperandClass) {
				case Ptg.CLASS_REF:
					if (desiredOperandClass == Ptg.CLASS_REF) {
						afp.setClass(Ptg.CLASS_REF);
					} else {
						afp.setClass(Ptg.CLASS_ARRAY);
					}
					localForceArrayFlag = false;
					break;
				case Ptg.CLASS_ARRAY:
					afp.setClass(Ptg.CLASS_ARRAY);
					localForceArrayFlag = false;
					break;
				case Ptg.CLASS_VALUE:
					afp.setClass(Ptg.CLASS_ARRAY);
					localForceArrayFlag = true;
					break;
				default:
					throw new IllegalStateException("Unexpected operand class ("
							+ defaultReturnOperandClass + ")");
			}
		} else {
			if (defaultReturnOperandClass == desiredOperandClass) {
				localForceArrayFlag = false;
				// an alternative would have been to for non-base Ptgs to set their operand class 
				// from their default, but this would require the call in many subclasses because
				// the default OC is not known until the end of the constructor
				afp.setClass(defaultReturnOperandClass); 
			} else {
				switch (desiredOperandClass) {
					case Ptg.CLASS_VALUE:
						// always OK to set functions to return 'value'
						afp.setClass(Ptg.CLASS_VALUE); 
						localForceArrayFlag = false;
						break;
					case Ptg.CLASS_ARRAY:
						switch (defaultReturnOperandClass) {
							case Ptg.CLASS_REF:
								afp.setClass(Ptg.CLASS_REF);
//								afp.setClass(Ptg.CLASS_ARRAY);
								break;
							case Ptg.CLASS_VALUE:
								afp.setClass(Ptg.CLASS_ARRAY);
								break;
							default:
								throw new IllegalStateException("Unexpected operand class ("
										+ defaultReturnOperandClass + ")");
						}
						localForceArrayFlag = (defaultReturnOperandClass == Ptg.CLASS_VALUE);
						break;
					case Ptg.CLASS_REF:
						switch (defaultReturnOperandClass) {
							case Ptg.CLASS_ARRAY:
								afp.setClass(Ptg.CLASS_ARRAY);
								break;
							case Ptg.CLASS_VALUE:
								afp.setClass(Ptg.CLASS_VALUE);
								break;
							default:
								throw new IllegalStateException("Unexpected operand class ("
										+ defaultReturnOperandClass + ")");
						}
						localForceArrayFlag = false;
						break;
					default:
						throw new IllegalStateException("Unexpected operand class ("
								+ desiredOperandClass + ")");
				}

			}
		}

		for (int i = 0; i < children.length; i++) {
			ParseNode child = children[i];
			byte paramOperandClass = afp.getParameterClass(i);
			transformNode(child, paramOperandClass, localForceArrayFlag);
		}
	}

	private void setSimpleValueFuncClass(AbstractFunctionPtg afp,
			byte desiredOperandClass, boolean callerForceArrayFlag) {

		if (callerForceArrayFlag  || desiredOperandClass == Ptg.CLASS_ARRAY) {
			afp.setClass(Ptg.CLASS_ARRAY);
		} else {
			afp.setClass(Ptg.CLASS_VALUE); 
		}
	}
}

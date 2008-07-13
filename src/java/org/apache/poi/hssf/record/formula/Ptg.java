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

package org.apache.poi.hssf.record.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * <tt>Ptg</tt> represents a syntactic token in a formula.  'PTG' is an acronym for 
 * '<b>p</b>arse <b>t</b>hin<b>g</b>'.  Originally, the name referred to the single 
 * byte identifier at the start of the token, but in POI, <tt>Ptg</tt> encapsulates
 * the whole formula token (initial byte + value data).
 * <p/>
 * 
 * <tt>Ptg</tt>s are logically arranged in a tree representing the structure of the
 * parsed formula.  However, in BIFF files <tt>Ptg</tt>s are written/read in 
 * <em>Reverse-Polish Notation</em> order. The RPN ordering also simplifies formula
 * evaluation logic, so POI mostly accesses <tt>Ptg</tt>s in the same way.
 *
 * @author  andy
 * @author avik
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public abstract class Ptg implements Cloneable {

	/* convert infix order ptg list to rpn order ptg list
	 * @return List ptgs in RPN order
	 * @param infixPtgs List of ptgs in infix order
	 */

	/* DO NOT REMOVE
	 *we keep this method in case we wish to change the way we parse
	 *It needs a getPrecedence in OperationsPtg

	public static List ptgsToRpn(List infixPtgs) {
		java.util.Stack operands = new java.util.Stack();
		java.util.List retval = new java.util.Stack();

		java.util.ListIterator i = infixPtgs.listIterator();
		Object p;
		OperationPtg o ;
		boolean weHaveABracket = false;
		while (i.hasNext()) {
			p=i.next();
			if (p instanceof OperationPtg) {
				if (p instanceof ParenthesisPtg) {
					if (!weHaveABracket) {
						operands.push(p);
						weHaveABracket = true;
					} else {
						o = (OperationPtg) operands.pop();
						while (!(o instanceof ParenthesisPtg)) {
							retval.add(o);
						}
						weHaveABracket = false;
					}
				} else {

					while  (!operands.isEmpty() && ((OperationPtg) operands.peek()).getPrecedence() >= ((OperationPtg) p).getPrecedence() ) { //TODO handle ^ since it is right associative
						retval.add(operands.pop());
					}
					operands.push(p);
				}
			} else {
				retval.add(p);
			}
		}
		while (!operands.isEmpty()) {
			if (operands.peek() instanceof ParenthesisPtg ){
				//throw some error
			} else {
				retval.add(operands.pop());
			}
		}
		return retval;
	}
	*/
	
	/**
	 * Reads <tt>size</tt> bytes of the input stream, to create an array of <tt>Ptg</tt>s.
	 * Extra data (beyond <tt>size</tt>) may be read if and <tt>ArrayPtg</tt>s are present.
	 */
	public static Ptg[] readTokens(int size, RecordInputStream in) {
		Stack temp = createParsedExpressionTokens((short)size, in);
		return toPtgArray(temp);
	}

	/**
	 * @deprecated - use readTokens()
	 */
	public static Stack createParsedExpressionTokens(short size, RecordInputStream in)
	{
		Stack stack = new Stack();
		int pos = 0;
		List arrayPtgs = null;
		while ( pos < size )
		{
			Ptg ptg = Ptg.createPtg( in );
			if (ptg instanceof ArrayPtg) {
				if (arrayPtgs == null)
					arrayPtgs = new ArrayList(5);
				arrayPtgs.add(ptg);
				pos += 8;
			} else pos += ptg.getSize();
			stack.push( ptg );
		}
		if(pos != size) {
			throw new RuntimeException("Ptg array size mismatch");
		}
		if (arrayPtgs != null) {
			for (int i=0;i<arrayPtgs.size();i++) {
				ArrayPtg p = (ArrayPtg)arrayPtgs.get(i);
				p.readTokenValues(in);
			}
		}
		return stack;
	}

	public static Ptg createPtg(RecordInputStream in) {
		byte id = in.readByte();
		
		if (id < 0x20) {
			return createBasePtg(id, in);
		}
		
		Ptg  retval = createClassifiedPtg(id, in);

		if (id > 0x60) {
			retval.setClass(CLASS_ARRAY);
		} else if (id > 0x40) {
			retval.setClass(CLASS_VALUE);
		} else {
			retval.setClass(CLASS_REF);
		}

	   return retval;
	}

	private static Ptg createClassifiedPtg(byte id, RecordInputStream in) {
		
		int baseId = id & 0x1F | 0x20;
		
		switch (baseId) {
			case ArrayPtg.sid:     return new ArrayPtg(in);    // 0x20, 0x40, 0x60
			 case FuncPtg.sid:     return new FuncPtg(in);     // 0x21, 0x41, 0x61
			 case FuncVarPtg.sid:  return new FuncVarPtg(in);  // 0x22, 0x42, 0x62
			 case NamePtg.sid:     return new NamePtg(in);     // 0x23, 0x43, 0x63
			 case RefPtg.sid:      return new RefPtg(in);      // 0x24, 0x44, 0x64
			 case AreaPtg.sid:     return new AreaPtg(in);     // 0x25, 0x45, 0x65
			 case MemAreaPtg.sid:  return new MemAreaPtg(in);  // 0x26, 0x46, 0x66
			 case MemErrPtg.sid:   return new MemErrPtg(in);   // 0x27, 0x47, 0x67
			 case MemFuncPtg.sid:  return new MemFuncPtg(in);  // 0x29, 0x49, 0x69
			 case RefErrorPtg.sid: return  new RefErrorPtg(in);// 0x2a, 0x4a, 0x6a
			 case AreaErrPtg.sid:  return new AreaErrPtg(in);  // 0x2b, 0x4b, 0x6b
			 case RefNPtg.sid:     return new RefNPtg(in);     // 0x2c, 0x4c, 0x6c
			 case AreaNPtg.sid:    return new AreaNPtg(in);    // 0x2d, 0x4d, 0x6d

			 case NameXPtg.sid:    return new NameXPtg(in);    // 0x39, 0x49, 0x79
			 case Ref3DPtg.sid:    return  new Ref3DPtg(in);   // 0x3a, 0x5a, 0x7a
			 case Area3DPtg.sid:   return new Area3DPtg(in);   // 0x3b, 0x5b, 0x7b
			 case DeletedRef3DPtg.sid:  return new DeletedRef3DPtg(in);   // 0x3c, 0x5c, 0x7c
			 case DeletedArea3DPtg.sid: return  new DeletedArea3DPtg(in); // 0x3d, 0x5d, 0x7d
		}
		throw new UnsupportedOperationException(" Unknown Ptg in Formula: 0x"+
				   Integer.toHexString(id) + " (" + ( int ) id + ")");
	}

	private static Ptg createBasePtg(byte id, RecordInputStream in) {
		switch(id) {
			case 0x00:                return new UnknownPtg(); // TODO - not a real Ptg
			case ExpPtg.sid:          return new ExpPtg(in);          // 0x01
			case TblPtg.sid:          return new TblPtg(in);          // 0x02
			case AddPtg.sid:          return AddPtg.instance;         // 0x03
			case SubtractPtg.sid:     return SubtractPtg.instance;    // 0x04
			case MultiplyPtg.sid:     return MultiplyPtg.instance;    // 0x05
			case DividePtg.sid:       return DividePtg.instance;      // 0x06
			case PowerPtg.sid:        return PowerPtg.instance;       // 0x07
			case ConcatPtg.sid:       return ConcatPtg.instance;      // 0x08
			case LessThanPtg.sid:     return LessThanPtg.instance;    // 0x09
			case LessEqualPtg.sid:    return LessEqualPtg.instance;   // 0x0a
			case EqualPtg.sid:        return EqualPtg.instance;       // 0x0b
			case GreaterEqualPtg.sid: return GreaterEqualPtg.instance;// 0x0c
			case GreaterThanPtg.sid:  return GreaterThanPtg.instance; // 0x0d
			case NotEqualPtg.sid:     return NotEqualPtg.instance;    // 0x0e
			case IntersectionPtg.sid: return IntersectionPtg.instance;// 0x0f
			case UnionPtg.sid:        return UnionPtg.instance;       // 0x10
			case RangePtg.sid:        return RangePtg.instance;       // 0x11
			case UnaryPlusPtg.sid:    return UnaryPlusPtg.instance;   // 0x12
			case UnaryMinusPtg.sid:   return UnaryMinusPtg.instance;  // 0x13
			case PercentPtg.sid:      return PercentPtg.instance;     // 0x14
			case ParenthesisPtg.sid:  return ParenthesisPtg.instance; // 0x15
			case MissingArgPtg.sid:   return MissingArgPtg.instance;  // 0x16

			case StringPtg.sid:       return new StringPtg(in);       // 0x17
			case AttrPtg.sid:                
			case 0x1a:        return new AttrPtg(in); // 0x19
			case ErrPtg.sid:          return new ErrPtg(in);          // 0x1c
			case BoolPtg.sid:         return new BoolPtg(in);         // 0x1d
			case IntPtg.sid:          return new IntPtg(in);          // 0x1e
			case NumberPtg.sid:       return new NumberPtg(in);       // 0x1f
		}
		throw new RuntimeException("Unexpected base token id (" + id + ")");
	}
	/**
	 * 
	 * 
	 */
	public static int getEncodedSize(Stack ptgs) {
		return getEncodedSize(toPtgArray(ptgs));
	}
	/**
	 * @return a distinct copy of this <tt>Ptg</tt> if the class is mutable, or the same instance
	 * if the class is immutable.
	 */
	public final Ptg copy() {
		// TODO - all base tokens are logically immutable, but AttrPtg needs some clean-up 
		if (this instanceof ValueOperatorPtg) {
			return this;
		}
		if (this instanceof ScalarConstantPtg) {
			return this;
		}
		return (Ptg) clone();
	}

	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	private static Ptg[] toPtgArray(List l) {
		Ptg[] result = new Ptg[l.size()];
		l.toArray(result);
		return result;
	}
	private static Stack createStack(Ptg[] formulaTokens) {
		Stack result = new Stack();
		for (int i = 0; i < formulaTokens.length; i++) {
			result.add(formulaTokens[i]);
		} 
		return result;
	}
	// TODO - several duplicates of this code should be refactored here
	public static int getEncodedSize(Ptg[] ptgs) {
		int result = 0;
		for (int i = 0; i < ptgs.length; i++) {
			result += ptgs[i].getSize();
		}
		return result;
	}
	/**
	 * Writes the ptgs to the data buffer, starting at the specified offset.  
	 *
	 * <br/>
	 * The 2 byte encode length field is <b>not</b> written by this method.
	 * @return number of bytes written
	 */
	public static int serializePtgs(Ptg[] ptgs, byte[] data, int offset) {
		return serializePtgStack(createStack(ptgs), data, offset);
	}

	/**
	 * @deprecated use serializePtgs()
	 */
	public static int serializePtgStack(Stack expression, byte[] array, int offset) {
		int pos = 0;
		int size = 0;
		if (expression != null)
			size = expression.size();

		List arrayPtgs = null;

		for (int k = 0; k < size; k++) {
			Ptg ptg = ( Ptg ) expression.get(k);

			ptg.writeBytes(array, pos + offset);
			if (ptg instanceof ArrayPtg) {
			  if (arrayPtgs == null)
				  arrayPtgs = new ArrayList(5);
			  arrayPtgs.add(ptg);
			  pos += 8;
			} else pos += ptg.getSize();
		}
		if (arrayPtgs != null) {
			for (int i=0;i<arrayPtgs.size();i++) {
				ArrayPtg p = (ArrayPtg)arrayPtgs.get(i);
				pos += p.writeTokenValueBytes(array, pos + offset);
			}
		}
		return pos;
	}

	/**
	 * @return the encoded length of this Ptg, including the initial Ptg type identifier byte. 
	 */
	public abstract int getSize();
	
	/**
	 * @return the encoded length of this Ptg, not including the initial Ptg type identifier byte. 
	 */
//    public abstract int getDataSize();

	public final byte [] getBytes()
	{
		int    size  = getSize();
		byte[] bytes = new byte[ size ];

		writeBytes(bytes, 0);
		return bytes;
	}
	/** write this Ptg to a byte array*/
	public abstract void writeBytes(byte [] array, int offset);

	/**
	 * return a string representation of this token alone
	 */
	public abstract String toFormulaString(Workbook book);
	/**
	 * dump a debug representation (hexdump) to a string
	 */
	public final String toDebugString() {
		byte[] ba = new byte[getSize()];
		String retval=null;
		writeBytes(ba,0);
		try {
			retval = org.apache.poi.util.HexDump.dump(ba,0,0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	/** Overridden toString method to ensure object hash is not printed.
	 * This helps get rid of gratuitous diffs when comparing two dumps
	 * Subclasses may output more relevant information by overriding this method
	 **/
	public String toString(){
		return this.getClass().toString();
	}

	public static final byte CLASS_REF = 0x00;
	public static final byte CLASS_VALUE = 0x20;
	public static final byte CLASS_ARRAY = 0x40;

	private byte ptgClass = CLASS_REF; //base ptg

	public final void setClass(byte thePtgClass) {
		if (isBaseToken()) {
			throw new RuntimeException("setClass should not be called on a base token");
		}
		ptgClass = thePtgClass;
	}

	/**
	 *  @return the 'operand class' (REF/VALUE/ARRAY) for this Ptg
	 */
	public final byte getPtgClass() {
		return ptgClass;
	}

	public abstract byte getDefaultOperandClass();

	/**
	 * @return <code>false</code> if this token is classified as 'reference', 'value', or 'array'
	 */
	public abstract boolean isBaseToken();
}

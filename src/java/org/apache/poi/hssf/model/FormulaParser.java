/* ====================================================================
   Copyright 2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */



package org.apache.poi.hssf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//import PTG's .. since we need everything, import *
import org.apache.poi.hssf.record.formula.*;



/**
 * This class parses a formula string into a List of tokens in RPN order.
 * Inspired by 
 *           Lets Build a Compiler, by Jack Crenshaw
 * BNF for the formula expression is :
 * <expression> ::= <term> [<addop> <term>]*
 * <term> ::= <factor>  [ <mulop> <factor> ]*
 * <factor> ::= <number> | (<expression>) | <cellRef> | <function>
 * <function> ::= <functionName> ([expression [, expression]*])
 *
 *  @author Avik Sengupta <avik AT Avik Sengupta DOT com>
 *  @author Andrew C. oliver (acoliver at apache dot org)
 *  @author Eric Ladner (eladner at goldinc dot com)
 *  @author Cameron Riley (criley at ekmail.com)
 *  @author Peter M. Murray (pete at quantrix dot com)
 */
public class FormulaParser {
    
    public static int FORMULA_TYPE_CELL = 0;
    public static int FORMULA_TYPE_SHARED = 1;
    public static int FORMULA_TYPE_ARRAY =2;
    public static int FORMULA_TYPE_CONDFOMRAT = 3;
    public static int FORMULA_TYPE_NAMEDRANGE = 4;
    
    private String formulaString;
    private int pointer=0;
    private int formulaLength;
    
    private List tokens = new java.util.Stack();
    
    /**
     * Using an unsynchronized linkedlist to implement a stack since we're not multi-threaded.
     */
    private List functionTokens = new LinkedList();
    
    //private Stack tokens = new java.util.Stack();
    private List result = new ArrayList();
    private int numParen;
    
    private static char TAB = '\t';
    private static char CR = '\n';
    
   private char look;              // Lookahead Character
   private boolean inFunction = false;
   
   private Workbook book;
    
    
    /** create the parser with the string that is to be parsed
     *    later call the parse() method to return ptg list in rpn order
     *    then call the getRPNPtg() to retrive the parse results
     *  This class is recommended only for single threaded use
     */
    public FormulaParser(String formula, Workbook book){
        formulaString = formula;
        pointer=0;
        this.book = book;
    	formulaLength = formulaString.length();
    }
    

    /** Read New Character From Input Stream */
    private void GetChar() {
        // Check to see if we've walked off the end of the string.
	// Just return if so and reset Look to smoething to keep 
	// SkipWhitespace from spinning
        if (pointer == formulaLength) {
            look = (char)0;
	    return;
	}
        look=formulaString.charAt(pointer++);
        //System.out.println("Got char: "+ look);
    }
    

    /** Report an Error */
    private void Error(String s) {
        System.out.println("Error: "+s);
    }
    
    
 
    /** Report Error and Halt */
    private void Abort(String s) {
        Error(s);
        //System.exit(1);  //throw exception??
        throw new RuntimeException("Cannot Parse, sorry : "+s);
    }
    
    

    /** Report What Was Expected */
    private void Expected(String s) {
        Abort(s + " Expected");
    }
    
    
 
    /** Recognize an Alpha Character */
    private boolean IsAlpha(char c) {
        return Character.isLetter(c) || c == '$';
    }
    
    
 
    /** Recognize a Decimal Digit */
    private boolean IsDigit(char c) {
        //System.out.println("Checking digit for"+c);
        return Character.isDigit(c);
    }
    
    

    /** Recognize an Alphanumeric */
    private boolean  IsAlNum(char c) {
        return  (IsAlpha(c) || IsDigit(c));
    }
    
    

    /** Recognize an Addop */
    private boolean IsAddop( char c) {
        return (c =='+' || c =='-');
    }
    

    /** Recognize White Space */
    private boolean IsWhite( char c) {
        return  (c ==' ' || c== TAB);
    }
    
    /**
     * Determines special characters;primarily in use for definition of string literals
     * @param c
     * @return boolean
     */
    private boolean IsSpecialChar(char c) {
    	return (c == '>' || c== '<' || c== '=' || c=='&' || c=='[' || c==']');
    }
    

    /** Skip Over Leading White Space */
    private void SkipWhite() {
        while (IsWhite(look)) {
            GetChar();
        }
    }
    
    

    /** Match a Specific Input Character */
    private void Match(char x) {
        if (look != x) {
            Expected("" + x + "");
        }else {
            GetChar();
            SkipWhite();
        }
    }
    
    /** Get an Identifier */
    private String GetName() {
        StringBuffer Token = new StringBuffer();
        if (!IsAlpha(look) && look != '\'') {
            Expected("Name");
        }
        if(look == '\'')
        {
        	Match('\'');
        	boolean done = look == '\'';
        	while(!done)
        	{
        		Token.append(Character.toUpperCase(look));
        		GetChar();
        		if(look == '\'')
        		{
        			Match('\'');
        			done = look != '\'';
        		}
        	}
        }
        else
        {
	        while (IsAlNum(look)) {
	            Token.append(Character.toUpperCase(look));
	            GetChar();
	        }
		}
        SkipWhite();
        return Token.toString();
    }
    
    /**Get an Identifier AS IS, without stripping white spaces or 
       converting to uppercase; used for literals */
    private String GetNameAsIs() {
        StringBuffer Token = new StringBuffer();
		
		while (IsAlNum(look) || IsWhite(look) || IsSpecialChar(look)) {
            Token = Token.append(look);
            GetChar();
        }
        return Token.toString();
    }
    
    
    /** Get a Number */
    private String GetNum() {
        String Value ="";
        if  (!IsDigit(look)) Expected("Integer");
        while (IsDigit(look)){
            Value = Value + look;
            GetChar();
        }
        SkipWhite();
        return Value;
    }

    /** Output a String with Tab */
    private void  Emit(String s){
        System.out.print(TAB+s);
    }

    /** Output a String with Tab and CRLF */
    private void EmitLn(String s) {
        Emit(s);
        System.out.println();;
    }
    
    /** Parse and Translate a String Identifier */
    private void Ident() {
        String name;
        name = GetName();
        if (look == '('){
            //This is a function
            function(name);
        } else if (look == ':') { // this is a AreaReference
            String first = name;
            Match(':');
            String second = GetName();
            tokens.add(new AreaPtg(first+":"+second));
        } else if (look == '!') {
            Match('!');
            String sheetName = name;
            String first = GetName();
            short externIdx = book.checkExternSheet(book.getSheetIndex(sheetName));
            if (look == ':') {
                Match(':');
                String second=GetName();
                
                tokens.add(new Area3DPtg(first+":"+second,externIdx));
            } else {
                tokens.add(new Ref3DPtg(first,externIdx));
            }
        } else {
            //this can be either a cell ref or a named range !!
            boolean cellRef = true ; //we should probably do it with reg exp??
            boolean boolLit = (name.equals("TRUE") || name.equals("FALSE"));
            if (boolLit) {
                tokens.add(new BoolPtg(name));
            } else if (cellRef) {
                tokens.add(new ReferencePtg(name));
            }else {
                //handle after named range is integrated!!
            }
        }
    }
    
    /**
     * Adds a pointer to the last token to the latest function argument list.
     * @param obj
     */
    private void addArgumentPointer() {
		if (this.functionTokens.size() > 0) {
			//no bounds check because this method should not be called unless a token array is setup by function()
			List arguments = (List)this.functionTokens.get(0);
			arguments.add(tokens.get(tokens.size()-1));
		}
    }
    
    private void function(String name) {
    	//average 2 args per function
    	this.functionTokens.add(0, new ArrayList(2));
    	
        Match('(');
        int numArgs = Arguments();
        Match(')');
                
        AbstractFunctionPtg functionPtg = getFunction(name,(byte)numArgs);
        
		tokens.add(functionPtg);
 
        if (functionPtg.getName().equals("externalflag")) {
            tokens.add(new NamePtg(name, this.book));
        }

 		//remove what we just put in
		this.functionTokens.remove(0);
    }
    
    /**
     * Adds the size of all the ptgs after the provided index (inclusive).
     * <p>
     * Initially used to count a goto
     * @param index
     * @return int
     */
    private int getPtgSize(int index) {
    	int count = 0;
    	
    	Iterator ptgIterator = tokens.listIterator(index);
    	while (ptgIterator.hasNext()) {
    		Ptg ptg = (Ptg)ptgIterator.next();
    		count+=ptg.getSize();
    	}
    	
    	return count;
    }
    
    private int getPtgSize(int start, int end) {
        int count = 0;
        int index = start;
        Iterator ptgIterator = tokens.listIterator(index);
        while (ptgIterator.hasNext() && index <= end) {
            Ptg ptg = (Ptg)ptgIterator.next();
            count+=ptg.getSize();
            index++;
        }
        
        return count;
    }
    /**
     * Generates the variable function ptg for the formula.
     * <p>
     * For IF Formulas, additional PTGs are added to the tokens 
     * @param name
     * @param numArgs
     * @return Ptg a null is returned if we're in an IF formula, it needs extreme manipulation and is handled in this function
     */
    private AbstractFunctionPtg getFunction(String name, byte numArgs) {
        AbstractFunctionPtg retval = null;
        
        if (name.equals("IF")) {
            retval = new FuncVarPtg(AbstractFunctionPtg.ATTR_NAME, numArgs);
            
            //simulated pop, no bounds checking because this list better be populated by function()
            List argumentPointers = (List)this.functionTokens.get(0);
            
            
            AttrPtg ifPtg = new AttrPtg();
            ifPtg.setData((short)7); //mirroring excel output
            ifPtg.setOptimizedIf(true);
            
            if (argumentPointers.size() != 2  && argumentPointers.size() != 3) {
                throw new IllegalArgumentException("["+argumentPointers.size()+"] Arguments Found - An IF formula requires 2 or 3 arguments. IF(CONDITION, TRUE_VALUE, FALSE_VALUE [OPTIONAL]");
            }
            
            //Biffview of an IF formula record indicates the attr ptg goes after the condition ptgs and are
            //tracked in the argument pointers
            //The beginning first argument pointer is the last ptg of the condition
            int ifIndex = tokens.indexOf(argumentPointers.get(0))+1;
            tokens.add(ifIndex, ifPtg);
            
            //we now need a goto ptgAttr to skip to the end of the formula after a true condition
            //the true condition is should be inserted after the last ptg in the first argument
            
            int gotoIndex = tokens.indexOf(argumentPointers.get(1))+1;
            
            AttrPtg goto1Ptg = new AttrPtg();
            goto1Ptg.setGoto(true);
            
            
            tokens.add(gotoIndex, goto1Ptg);
            
            
            if (numArgs > 2) { //only add false jump if there is a false condition
                
                //second goto to skip past the function ptg
                AttrPtg goto2Ptg = new AttrPtg();
                goto2Ptg.setGoto(true);
                goto2Ptg.setData((short)(retval.getSize()-1));
                //Page 472 of the Microsoft Excel Developer's kit states that:
                //The b(or w) field specifies the number byes (or words to skip, minus 1
                
                tokens.add(goto2Ptg); //this goes after all the arguments are defined
            }
            
            //data portion of the if ptg points to the false subexpression (Page 472 of MS Excel Developer's kit)
            //count the number of bytes after the ifPtg to the False Subexpression
            //doesn't specify -1 in the documentation
            ifPtg.setData((short)(getPtgSize(ifIndex+1, gotoIndex)));
            
            //count all the additional (goto) ptgs but dont count itself
            int ptgCount = this.getPtgSize(gotoIndex)-goto1Ptg.getSize()+retval.getSize();
            if (ptgCount > (int)Short.MAX_VALUE) {
                throw new RuntimeException("Ptg Size exceeds short when being specified for a goto ptg in an if");
            }
            
            goto1Ptg.setData((short)(ptgCount-1));
            
        } else {
            
            retval = new FuncVarPtg(name,numArgs);
        }
        
        return retval;
    }
    
    /** get arguments to a function */
    private int Arguments() {
        int numArgs = 0;
        if (look != ')')  {
            numArgs++; 
            Expression();
			   addArgumentPointer();
        }
        while (look == ','  || look == ';') { //TODO handle EmptyArgs
            if(look == ',') {
              Match(',');
            }
            else {
              Match(';');
            }
            Expression();
			   addArgumentPointer();
            numArgs++;
        }
        return numArgs;
    }

   /** Parse and Translate a Math Factor  */
    private void Factor() {
    	if (look == '-')
    	{
    		Match('-');
    		Factor();
    		tokens.add(new UnaryMinusPtg());
    	}
        else if (look == '(' ) {
            Match('(');
            Expression();
            Match(')');
            tokens.add(new ParenthesisPtg());
        } else if (IsAlpha(look) || look == '\''){
            Ident();
        } else if(look == '"') {
           StringLiteral();
        } else {
             
            String number = GetNum();
            if (look=='.') {
                Match('.');
                String decimalPart = null;
                if (IsDigit(look)) number = number +"."+ GetNum(); //this also takes care of someone entering "1234."
                tokens.add(new NumberPtg(number));
            } else {
                tokens.add(new IntPtg(number));  //TODO:what if the number is too big to be a short? ..add factory to return Int or Number!
            }
        }
    }
    
    private void StringLiteral() 
	{
		// Can't use match here 'cuz it consumes whitespace
		// which we need to preserve inside the string.
		// - pete
		// Match('"');
		if (look != '"')
			Expected("\"");
		else
		{
			GetChar();
			StringBuffer Token = new StringBuffer();
			for (;;)
			{
				if (look == '"')
				{
					GetChar();
					SkipWhite(); //potential white space here since it doesnt matter up to the operator
					if (look == '"')
						Token.append("\"");
					else
						break;
				}
				else if (look == 0)
				{
					break;
				}
				else
				{
					Token.append(look);
					GetChar();
				}
			}
			tokens.add(new StringPtg(Token.toString()));
		}
	}
    
    /** Recognize and Translate a Multiply */
    private void Multiply(){
        Match('*');
        Factor();
        tokens.add(new MultiplyPtg());
  
    }
    
    
    /** Recognize and Translate a Divide */
    private void Divide() {
        Match('/');
        Factor();
        tokens.add(new DividePtg());

    }
    
    
    /** Parse and Translate a Math Term */
    private void  Term(){
        Factor();
		 while (look == '*' || look == '/' || look == '^' || look == '&') {
        
            ///TODO do we need to do anything here??
            if (look == '*') Multiply();
            else if (look == '/') Divide();
            else if (look == '^') Power();
            else if (look == '&') Concat();
        }
    }
    
    /** Recognize and Translate an Add */
    private void Add() {
        Match('+');
        Term();
        tokens.add(new AddPtg());
    }
    
    /** Recognize and Translate a Concatination */
    private void Concat() {
        Match('&');
        Term();
        tokens.add(new ConcatPtg());
    }
    
    /** Recognize and Translate a test for Equality  */
    private void Equal() {
        Match('=');
        Expression();
        tokens.add(new EqualPtg());
    }
    
    /** Recognize and Translate a Subtract */
    private void Subtract() {
        Match('-');
        Term();
        tokens.add(new SubtractPtg());
    }    

    private void Power() {
        Match('^');
        Term();
        tokens.add(new PowerPtg());
    }
    
    
    /** Parse and Translate an Expression */
    private void Expression() {
        Term();
        while (IsAddop(look)) {
            if (look == '+' )  Add();
            else if (look == '-') Subtract();
        }
        
		/*
		 * This isn't quite right since it would allow multiple comparison operators.
		 */
		
		  if(look == '=' || look == '>' || look == '<') {
		  		if (look == '=') Equal();
		      else if (look == '>') GreaterThan();
		      else if (look == '<') LessThan();
		      return;
		  }        
        
        
    }
    
    /** Recognize and Translate a Greater Than  */
    private void GreaterThan() {
		Match('>');
		if(look == '=')
		    GreaterEqual();
		else {
		    Expression();
		    tokens.add(new GreaterThanPtg());
		}
    }
    
    /** Recognize and Translate a Less Than  */
    private void LessThan() {
		Match('<');
		if(look == '=')
		    LessEqual();
		else if(look == '>')
		    NotEqual();
		else {
		    Expression();
		    tokens.add(new LessThanPtg());
		}

	}  
   
   /**
    * Recognize and translate Greater than or Equal
    *
    */ 
	private void GreaterEqual() {
	    Match('=');
	    Expression();
	    tokens.add(new GreaterEqualPtg());
	}    

	/**
	 * Recognize and translate Less than or Equal
	 *
	 */ 

	private void LessEqual() {
	    Match('=');
	    Expression();
	    tokens.add(new LessEqualPtg());
	}
	
	/**
	 * Recognize and not Equal
	 *
	 */ 

	private void NotEqual() {
	    Match('>');
	    Expression();
	    tokens.add(new NotEqualPtg());
	}    
    
    //{--------------------------------------------------------------}
    //{ Parse and Translate an Assignment Statement }
    /**
procedure Assignment;
var Name: string[8];
begin
   Name := GetName;
   Match('=');
   Expression;

end;
     **/
    
 
    /** Initialize */
    
    private void  init() {
        GetChar();
        SkipWhite();
    }
    
    /** API call to execute the parsing of the formula
     *
     */
    public void parse() {
        synchronized (tokens) {
            init();
            Expression();
        }
    }
    
    
    /*********************************
     * PARSER IMPLEMENTATION ENDS HERE
     * EXCEL SPECIFIC METHODS BELOW
     *******************************/
    
    /** API call to retrive the array of Ptgs created as 
     * a result of the parsing
     */
    public Ptg[] getRPNPtg() {
     return getRPNPtg(FORMULA_TYPE_CELL);
    }
    
    public Ptg[] getRPNPtg(int formulaType) {
        Node node = createTree();
        setRootLevelRVA(node, formulaType);
        setParameterRVA(node,formulaType);
        return (Ptg[]) tokens.toArray(new Ptg[0]);
    }
    
    private void setRootLevelRVA(Node n, int formulaType) {
        //Pg 16, excelfileformat.pdf @ openoffice.org
        Ptg p = (Ptg) n.getValue();
            if (formulaType == FormulaParser.FORMULA_TYPE_NAMEDRANGE) {
                if (p.getDefaultOperandClass() == Ptg.CLASS_REF) {
                    setClass(n,Ptg.CLASS_REF);
                } else {
                    setClass(n,Ptg.CLASS_ARRAY);
                }
            } else {
                setClass(n,Ptg.CLASS_VALUE);
            }
        
    }
    
    private void setParameterRVA(Node n, int formulaType) {
        Ptg p = n.getValue();
        int numOperands = n.getNumChildren();
        if (p instanceof AbstractFunctionPtg) {
            for (int i =0;i<numOperands;i++) {
                setParameterRVA(n.getChild(i),((AbstractFunctionPtg)p).getParameterClass(i),formulaType);
//                if (n.getChild(i).getValue() instanceof AbstractFunctionPtg) {
//                    setParameterRVA(n.getChild(i),formulaType);
//                }
                setParameterRVA(n.getChild(i),formulaType);
            }
        } else {
            for (int i =0;i<numOperands;i++) {
                setParameterRVA(n.getChild(i),formulaType);
            }
        } 
    }
    private void setParameterRVA(Node n, int expectedClass,int formulaType) {
        Ptg p = (Ptg) n.getValue();
        if (expectedClass == Ptg.CLASS_REF) { //pg 15, table 1 
            if (p.getDefaultOperandClass() == Ptg.CLASS_REF ) {
                setClass(n, Ptg.CLASS_REF);
            }
            if (p.getDefaultOperandClass() == Ptg.CLASS_VALUE) {
                if (formulaType==FORMULA_TYPE_CELL || formulaType == FORMULA_TYPE_SHARED) {
                    setClass(n,Ptg.CLASS_VALUE);
                } else {
                    setClass(n,Ptg.CLASS_ARRAY);
                }
            }
            if (p.getDefaultOperandClass() == Ptg.CLASS_ARRAY ) {
                setClass(n, Ptg.CLASS_ARRAY);
            }
        } else if (expectedClass == Ptg.CLASS_VALUE) { //pg 15, table 2
            if (formulaType == FORMULA_TYPE_NAMEDRANGE) {
                setClass(n,Ptg.CLASS_ARRAY) ;
            } else {
                setClass(n,Ptg.CLASS_VALUE);
            }
        } else { //Array class, pg 16. 
            if (p.getDefaultOperandClass() == Ptg.CLASS_VALUE &&
                 (formulaType==FORMULA_TYPE_CELL || formulaType == FORMULA_TYPE_SHARED)) {
                 setClass(n,Ptg.CLASS_VALUE);
            } else {
                setClass(n,Ptg.CLASS_ARRAY);
            }
        }
    }
    
     private void setClass(Node n, byte theClass) {
        Ptg p = (Ptg) n.getValue();
        if (p instanceof AbstractFunctionPtg || !(p instanceof OperationPtg)) {
            p.setClass(theClass);
        } else {
            for (int i =0;i<n.getNumChildren();i++) {
                setClass(n.getChild(i),theClass);
            }
        }
     }
    /**
     * Convience method which takes in a list then passes it to the other toFormulaString
     * signature. 
     * @param book   workbook for 3D and named references
     * @param lptgs  list of Ptg, can be null or empty
     * @return a human readable String
     */
    public static String toFormulaString(Workbook book, List lptgs) {
        String retval = null;
        if (lptgs == null || lptgs.size() == 0) return "#NAME";
        Ptg[] ptgs = new Ptg[lptgs.size()];
        ptgs = (Ptg[])lptgs.toArray(ptgs);
        retval = toFormulaString(book, ptgs);
        return retval;
    }
    
    /**
     * Static method to convert an array of Ptgs in RPN order
     * to a human readable string format in infix mode.
     * @param book  workbook for named and 3D references
     * @param ptgs  array of Ptg, can be null or empty
     * @return a human readable String
     */
    public static String toFormulaString(Workbook book, Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) return "#NAME";
        java.util.Stack stack = new java.util.Stack();
        AttrPtg ifptg = null;

           // Excel allows to have AttrPtg at position 0 (such as Blanks) which
           // do not have any operands. Skip them.
        stack.push(ptgs[0].toFormulaString(book));
                  
        for (int i = 1; i < ptgs.length; i++) {
            if (! (ptgs[i] instanceof OperationPtg)) {
                stack.push(ptgs[i].toFormulaString(book));
                continue;
            }
                      
            if (ptgs[i] instanceof AttrPtg && ((AttrPtg) ptgs[i]).isOptimizedIf()) {
                ifptg = (AttrPtg) ptgs[i];
                continue;
            }
                      
            final OperationPtg o = (OperationPtg) ptgs[i];
            final String[] operands = new String[o.getNumberOfOperands()];

            for (int j = operands.length; j > 0; j--) {
                //TODO: catch stack underflow and throw parse exception.
                operands[j - 1] = (String) stack.pop();
                      }  

            stack.push(o.toFormulaString(operands));
            if (!(o instanceof AbstractFunctionPtg)) continue;

            final AbstractFunctionPtg f = (AbstractFunctionPtg) o;
            final String fname = f.getName();
            if (fname == null) continue;

            if ((ifptg != null) && (fname.equals("specialflag"))) {
                             // this special case will be way different.
                stack.push(ifptg.toFormulaString(new String[]{(String) stack.pop()}));
                continue;
                      }
            if (fname.equals("externalflag")) {
                final String top = (String) stack.pop();
                final int paren = top.indexOf('(');
                final int comma = top.indexOf(',');
                if (comma == -1) {
                    final int rparen = top.indexOf(')');
                    stack.push(top.substring(paren + 1, rparen) + "()");
                  }
                else {
                    stack.push(top.substring(paren + 1, comma) + '(' +
                               top.substring(comma + 1));
            }
        }
    }
        // TODO: catch stack underflow and throw parse exception.
        return (String) stack.pop();
    }


    /** Create a tree representation of the RPN token array
     *used to run the class(RVA) change algo
     */
    private Node createTree() {
        java.util.Stack stack = new java.util.Stack();
        int numPtgs = tokens.size();
        OperationPtg o;
        int numOperands;
        Node[] operands;
        for (int i=0;i<numPtgs;i++) {
            if (tokens.get(i) instanceof OperationPtg) {
                
                o = (OperationPtg) tokens.get(i);
                numOperands = o.getNumberOfOperands();
                operands = new Node[numOperands];
                for (int j=0;j<numOperands;j++) {
                    operands[numOperands-j-1] = (Node) stack.pop(); 
                }
                Node result = new Node(o);
                result.setChildren(operands);
                stack.push(result);
            } else {
                stack.push(new Node((Ptg)tokens.get(i)));
            }
        }
        return (Node) stack.pop();
    }
   
    /** toString on the parser instance returns the RPN ordered list of tokens
     *   Useful for testing
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
           for (int i=0;i<tokens.size();i++) {
            buf.append( ( (Ptg)tokens.get(i)).toFormulaString(book));
            buf.append(' ');
        } 
        return buf.toString();
    }
    
}   
    /** Private helper class, used to create a tree representation of the formula*/
    class Node {
        private Ptg value=null;
        private Node[] children=new Node[0];
        private int numChild=0;
        public Node(Ptg val) {
            value = val; 
        }
        public void setChildren(Node[] child) {children = child;numChild=child.length;}
        public int getNumChildren() {return numChild;}
        public Node getChild(int number) {return children[number];}
        public Ptg getValue() {return value;}
    }

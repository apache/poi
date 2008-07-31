/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

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

package org.apache.poi.hssf.util;

/**
 * <p>Title: HSSFDataValidation</p>
 * <p>Description: Utilty class for creating data validation cells</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 * @version 2.0-pre
 */

public class HSSFDataValidation
{
  /**
   * Validation data type constants
   */
  /**
   * Any type
   */
  public static final int DATA_TYPE_ANY              = 0x00;
  /**
   * Integer type
   */
  public static final int DATA_TYPE_INTEGER          = 0x01;
  /**
   * Decimal type
   */
  public static final int DATA_TYPE_DECIMAL          = 0x02;
  /**
   * List type ( combo box type )
   */
  public static final int DATA_TYPE_LIST             = 0x03;
  /**
   * Date type
   */
  public static final int DATA_TYPE_DATE             = 0x04;
  /**
   * Time type
   */
  public static final int DATA_TYPE_TIME             = 0x05;
  /**
   * String length type
   */
  public static final int DATA_TYPE_TEXT_LENGTH      = 0x06;
  /**
   * Formula ( custom ) type
   */
  public static final int DATA_TYPE_FORMULA          = 0x07;

  /**
   * Error style constants for error box
   */
  /**
   * STOP style like
   */
  public static final int ERROR_STYLE_STOP           = 0x00;
  /**
   * WARNING style like
   */
  public static final int ERROR_STYLE_WARNING        = 0x01;
  /**
   * INFO style like
   */
  public static final int ERROR_STYLE_INFO           = 0x02;

  /**
   * Condition operator
   */
  public static final int OPERATOR_BETWEEN           = 0x00;
  public static final int OPERATOR_NOT_BETWEEN       = 0x01;
  public static final int OPERATOR_EQUAL             = 0x02;
  public static final int OPERATOR_NOT_EQUAL         = 0x03;
  public static final int OPERATOR_GREATER_THAN      = 0x04;
  public static final int OPERATOR_LESS_THAN         = 0x05;
  public static final int OPERATOR_GREATER_OR_EQUAL  = 0x06;
  public static final int OPERATOR_LESS_OR_EQUAL     = 0x07;

  private short _first_row                  = 0;
  private short _first_col                  = 0;
  private short _last_row                   = 0;
  private short _last_col                   = 0;

  private String _prompt_title              = null;
  private String _prompt_text               = null;
  private String _error_title               = null;
  private String _error_text                = null;
  private String _string_first_formula      = null;
  private String _string_sec_formula        = null;

  private int  _data_type                   = HSSFDataValidation.DATA_TYPE_ANY;
  private int  _error_style                 = HSSFDataValidation.ERROR_STYLE_STOP;
  private boolean  _list_explicit_formula   = true;
  private boolean  _empty_cell_allowed      = true;
  private boolean  _surpress_dropdown_arrow = false;
  private boolean  _show_prompt_box         = true;
  private boolean  _show_error_box          = true;
  private int _operator                     = HSSFDataValidation.OPERATOR_BETWEEN;


  /**
   * Empty constructor
   */
  public HSSFDataValidation( )
  {
  }

  /**
   * Constructor wich initializes the cell range on wich this object will be applied
   * @param first_row First row
   * @param first_col First column
   * @param last_row Last row
   * @param last_col Last column
   */
  public HSSFDataValidation( short first_row, short first_col, short last_row, short last_col )
  {
      this._first_row = first_row;
      this._first_col = first_col;
      this._last_row  = last_row;
      this._last_col  = last_col;
  }

  /**
   * Set the type of this object
   * @param data_type The type
   * @see DATA_TYPE_ANY, DATA_TYPE_INTEGER, DATA_TYPE_DECIMNAL, DATA_TYPE_LIST, DATA_TYPE_DATE,
   *      DATA_TYPE_TIME, DATA_TYPE_TEXT_LENTGH, DATA_TYPE_FORMULA
   */
  public void setDataValidationType( int data_type )
  {
      this._data_type = data_type;
  }

  /**
   * The data type of this object
   * @return The type
   * @see DATA_TYPE_ANY, DATA_TYPE_INTEGER, DATA_TYPE_DECIMNAL, DATA_TYPE_LIST, DATA_TYPE_DATE,
   *      DATA_TYPE_TIME, DATA_TYPE_TEXT_LENTGH, DATA_TYPE_FORMULA
   */
  public int getDataValidationType()
  {
     return this._data_type;
  }

  /**
   * Sets the error style for error box
   * @param error_style Error style constant
   * @see ERROR_STYLE_STOP, ERROR_STYLE_WARNING, ERROR_STYLE_INFO
   */
  public void setErrorStyle( int error_style )
  {
      this._error_style = error_style;
  }

  /**
   * returns the error style of errror box
   * @return the style constant
   * @see ERROR_STYLE_STOP, ERROR_STYLE_WARNING, ERROR_STYLE_INFO
   */
  public int getErrorStyle( )
  {
      return this._error_style;
  }

  /**
   * If this object has an explicit formula . This is useful only for list data validation object
   * @param explicit True if use an explicit formula
   */
  public void setExplicitListFormula( boolean explicit )
  {
     this._list_explicit_formula = explicit;
  }

  /**
   * Returns the settings for explicit formula . This is useful only for list data validation objects.
   * This method always returns false if the object isn't a list validation object
   * @see setDataValidationType( int data_type )
   * @return
   */
  public boolean getExplicitListFormula( )
  {
    if ( this._data_type != HSSFDataValidation.DATA_TYPE_LIST )
    {
        return false;
    }
    return this._list_explicit_formula ;
  }

  /**
   * Sets if this object allows empty as a valid value
   * @param allowed True if this object should treats empty as valid value , false otherwise
   */
  public void setEmptyCellAllowed( boolean allowed )
  {
     this._empty_cell_allowed = allowed;
  }

  /**
   * Retrieve the settings for empty cells allowed
   * @return True if this object should treats empty as valid value , false otherwise
   */
  public boolean getEmptyCellAllowed( )
  {
     return this._empty_cell_allowed ;
  }
  /**
   * @deprecated - (Jul-2008) use setSuppressDropDownArrow
    */
  public void setSurppressDropDownArrow( boolean suppress ) {
    setSuppressDropDownArrow(suppress);
  }
  /**
   * @deprecated - (Jul-2008) use getSuppressDropDownArrow
    */
  public boolean getSurppressDropDownArrow( ) {
    return getSuppressDropDownArrow();
  }

  /**
   * Useful for list validation objects .
   * @param surppres True if a list should display the values into a drop down list , false otherwise .
   *                 In other words , if a list should display the arrow sign on its right side
   */
  public void setSuppressDropDownArrow( boolean surppres )
  {
     this._surpress_dropdown_arrow = surppres;
  }

  /**
   * Useful only list validation objects .
   * This method always returns false if the object isn't a list validation object
   * @return True if a list should display the values into a drop down list , false otherwise .
   * @see setDataValidationType( int data_type )
   */
  public boolean getSuppressDropDownArrow( )
  {
     if ( this._data_type != HSSFDataValidation.DATA_TYPE_LIST )
     {
         return false;
     }
     return this._surpress_dropdown_arrow ;
  }

  /**
   * Sets the behaviour when a cell which belongs to this object is selected
   * @param show True if an prompt box should be displayed , false otherwise
   */
  public void setShowPromptBox( boolean show )
  {
     this._show_prompt_box = show;
  }

  /**
   * @param show True if an prompt box should be displayed , false otherwise
   */
  public boolean getShowPromptBox( )
  {
     if ( (this.getPromptBoxText() == null) && (this.getPromptBoxTitle() == null) )
     {
       return false;
     }
     return this._show_prompt_box ;
  }

  /**
   * Sets the behaviour when an invalid value is entered
   * @param show True if an error box should be displayed , false otherwise
   */
  public void setShowErrorBox( boolean show )
  {
     this._show_error_box = show;
  }

  /**
   * @return True if an error box should be displayed , false otherwise
   */
  public boolean getShowErrorBox( )
  {
    if ( (this.getErrorBoxText() == null) && (this.getErrorBoxTitle() == null) )
    {
      return false;
    }
    return this._show_error_box ;
  }

  /**
   * Sets the operator involved in the formula whic governs this object
   * Example : if you wants that a cell to accept only values between 1 and 5 , which
   * mathematically means 1 <= value <= 5 , then the operator should be OPERATOR_BETWEEN
   * @param operator A constant for operator
   * @see OPERATOR_BETWEEN, OPERATOR_NOT_BETWEEN, OPERATOR_EQUAL, OPERATOR_NOT_EQUAL
   * OPERATOR_GREATER_THAN, OPERATOR_LESS_THAN, OPERATOR_GREATER_OR_EQUAL,
   * OPERATOR_LESS_OR_EQUAL
   */
  public void setOperator( int operator )
  {
      this._operator = operator;
  }

  /**
   * Retrieves the operator used for this object's formula
   * @return
   * @see OPERATOR_BETWEEN, OPERATOR_NOT_BETWEEN, OPERATOR_EQUAL, OPERATOR_NOT_EQUAL
   * OPERATOR_GREATER_THAN, OPERATOR_LESS_THAN, OPERATOR_GREATER_OR_EQUAL,
   * OPERATOR_LESS_OR_EQUAL
   */
  public int getOperator()
  {
      return this._operator;
  }

  /**
   * Sets the title and text for the prompt box . Prompt box is displayed when the user
   * selects a cell which belongs to this validation object . In order for a prompt box
   * to be displayed you should also use method setShowPromptBox( boolean show )
   * @param title The prompt box's title
   * @param text The prompt box's text
   * @see setShowPromptBox( boolean show )
   */
  public void createPromptBox( String title, String text )
  {
      this._prompt_title = title;
      this._prompt_text  = text;
      this.setShowPromptBox(true);
  }

  /**
   * Returns the prompt box's title
   * @return Prompt box's title or null
   */
  public String getPromptBoxTitle( )
  {
    return this._prompt_title;
  }

  /**
   * Returns the prompt box's text
   * @return Prompt box's text or null
   */
  public String getPromptBoxText( )
  {
    return this._prompt_text;
  }

  /**
   * Sets the title and text for the error box . Error box is displayed when the user
   * enters an invalid value int o a cell which belongs to this validation object .
   * In order for an error box to be displayed you should also use method
   * setShowErrorBox( boolean show )
   * @param title The error box's title
   * @param text The error box's text
   * @see setShowErrorBox( boolean show )
   */
  public void createErrorBox( String title, String text )
  {
      this._error_title = title;
      this._error_text  = text;
      this.setShowErrorBox(true);
  }

  /**
   * Returns the error box's title
   * @return Error box's title or null
   */
  public String getErrorBoxTitle( )
  {
    return this._error_title;
  }

  /**
   * Returns the error box's text
   * @return Error box's text or null
   */
  public String getErrorBoxText( )
  {
    return this._error_text;
  }

  /**
   * Sets the first formula for this object .
   * A formula is divided into three parts : first formula , operator and second formula .
   * In other words , a formula contains a left oprand , an operator and a right operand.
   * This is the general rule . An example is 1<= value <= 5 . In this case ,
   * the left operand ( or the first formula ) is the number 1 . The operator is
   * OPERATOR_BETWEEN and the right operand ( or the second formula ) is 5 .
   * @param formula
   */
  public void setFirstFormula( String formula )
  {
     this._string_first_formula = formula;
  }

  /**
   * Returns the first formula
   * @return
   */
  public String getFirstFormula( )
  {
     return this._string_first_formula;
  }

  /**
   * Sets the first formula for this object .
   * A formula is divided into three parts : first formula , operator and second formula .
   * In other words , a formula contains a left oprand , an operator and a right operand.
   * This is the general rule . An example is 1<= value <=5 . In this case ,
   * the left operand ( or the first formula ) is the number 1 . The operator is
   * OPERATOR_BETWEEN and the right operand ( or the second formula ) is 5 .
   * But there are cases when a second formula isn't needed :
   * You want somethink like : all values less than 5 . In this case , there's only a first
   * formula ( in our case 5 ) and the operator OPERATOR_LESS_THAN
   * @param formula
   */
  public void setSecondFormula( String formula )
  {
     this._string_sec_formula = formula;
  }

  /**
   * Returns the second formula
   * @return
   */
  public String getSecondFormula( )
  {
     return this._string_sec_formula;
  }

  public void setFirstRow( short first_row )
  {
    this._first_row = first_row;
  }

  public void setFirstColumn( short first_column )
  {
    this._first_col = first_column;
  }

  public void setLastRow( short last_row )
  {
    this._last_row = last_row;
  }

  public void setLastColumn( short last_column )
  {
    this._last_col = last_column;
  }

  public short getFirstRow()
  {
    return this._first_row;
  }

  public short getFirstColumn()
  {
    return this._first_col;
  }

  public short getLastRow()
  {
    return this._last_row;
  }

  public short getLastColumn()
  {
    return this._last_col;
  }

}
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
 * Title:        Range Address <P>
 * Description:  provides connectivity utilities for ranges<P>
 *
 *
 * REFERENCE:  <P>
 * @author IgOr KaTz && EuGeNe BuMaGiN (Tal Moshaiov) (VistaPortal LDT.)
@version 1.0
 */

public class RangeAddress {
  final static int WRONG_POS  = -1;
  final static int MAX_HEIGHT = 66666;
  final static char SO_FORMNAME_ENCLOSURE =  '\'';
  String m_sheetName;
  String m_cellFrom;
  String m_cellTo;

  /**
   * Accepts an external reference from excel.
   * <p>
   * i.e. Sheet1!$A$4:$B$9
   * @param _url
   */  
  public RangeAddress (String _url) {
    init (_url);
  }
  
  public RangeAddress (int _startCol, int _startRow, int _endCol, int _endRow) {
    init (numTo26Sys (_startCol) + _startRow + ":"
    + numTo26Sys (_endCol) + _endRow);
  }
  
  /**
   * 
   * @return String <b>note: </b> All absolute references are removed
   */
  public String getAddress (){
    String result = "";
    if(m_sheetName != null)
      result += m_sheetName+"!";
    if(m_cellFrom != null){
      result += m_cellFrom;
      if(m_cellTo != null)
        result += ":" + m_cellTo;
    }
    return result;
  }
  
  
  public String getSheetName (){
    return m_sheetName;
  }
  
  public String getRange (){
    String result = "";
    if(m_cellFrom != null){
      result += m_cellFrom;
      if(m_cellTo != null)
        result += ":" + m_cellTo;
    }
    return result;
  }
  
  public  boolean isCellOk (String _cell){
    if (_cell != null){
      if ( (getYPosition (_cell) != WRONG_POS) &&
      (getXPosition (_cell) != WRONG_POS) )
        return true;
      else
        return false;
    } else
      return false;
  }
  
  public  boolean isSheetNameOk (){
    return isSheetNameOk (m_sheetName);
  }
  
  private  static boolean intern_isSheetNameOk (String _sheetName, boolean _canBeWaitSpace){
    for (int i = 0  ; i < _sheetName.length (); i++){
      char ch = _sheetName.charAt (i);
      if (! (Character.isLetterOrDigit (ch) || (ch == '_')||
      _canBeWaitSpace&&(ch == ' '))){
        return false;
      }
    }
    return true;
  }
  
  public  static boolean isSheetNameOk (String _sheetName){
    boolean res = false;
    if ( ( _sheetName != null) && !_sheetName.equals ("")){
      res = intern_isSheetNameOk (_sheetName,true);
    }else
      res = true;
    return res;
  }
  
  
  public String getFromCell (){
    return m_cellFrom;
  }
  
  public String getToCell (){
    return m_cellTo;
  }
  
  public int getWidth (){
    if(m_cellFrom != null && m_cellTo != null){
      int toX    =  getXPosition (m_cellTo);
      int fromX  =  getXPosition (m_cellFrom);
      if ((toX == WRONG_POS) || (fromX == WRONG_POS)){
        return 0;
      }else
        return toX - fromX + 1;
    }
    return 0;
  }
  
  public int getHeight (){
    if(m_cellFrom != null && m_cellTo != null){
      int toY    =  getYPosition (m_cellTo);
      int fromY  =  getYPosition (m_cellFrom);
      if ((toY == WRONG_POS) || (fromY == WRONG_POS)){
        return 0;
      }else
        return toY - fromY + 1;
    }
    return 0;
  }
  
  public void setSize (int _width, int _height){
    if(m_cellFrom == null)
      m_cellFrom = "a1";
    int tlX, tlY, rbX, rbY;
    tlX = getXPosition (m_cellFrom);
    tlY = getYPosition (m_cellFrom);
    m_cellTo = numTo26Sys (tlX + _width - 1);
    m_cellTo += String.valueOf (tlY + _height - 1);
  }
  
  public boolean hasSheetName (){
    if(m_sheetName == null)
      return false;
    return true;
  }
  
  public boolean hasRange (){
    if(m_cellFrom == null || m_cellTo == null)
      return false;
    return true;
  }
  
  public boolean hasCell (){
    if(m_cellFrom == null)
      return false;
    return true;
  }
  
  private void init (String _url){

    _url = removeString(_url, "$");
    _url = removeString(_url, "'");
    
    String[] urls = parseURL (_url);
    m_sheetName = urls[0];
    m_cellFrom = urls[1];
    m_cellTo = urls[2];

    //What if range is one celled ?
    if (m_cellTo == null){
      m_cellTo = m_cellFrom;
    }
        
    //Removing noneeds characters
    m_cellTo    = removeString(m_cellTo,".");
    
    
  }
  
  private String[] parseURL (String _url){
    String[] result = new String[3];
    int index = _url.indexOf(':');
    if (index >= 0) {
      String fromStr = _url.substring(0, index);
      String toStr = _url.substring(index+1);
      index = fromStr.indexOf('!');
      if (index >= 0) {
        result[0] = fromStr.substring(0, index);
        result[1] = fromStr.substring(index+1);
      } else {
        result[1] = fromStr;
      }
      index = toStr.indexOf('!');
      if (index >= 0) {
        result[2] = toStr.substring(index+1); 
      } else {
        result[2] = toStr; 
      }     
    } else {
      index = _url.indexOf('!');
      if (index >= 0) {
        result[0] = _url.substring(0, index);
        result[1] = _url.substring(index+1);
      } else {
        result[1] = _url;
      }
    }
    return result;
  }
  
  public int getYPosition (String _subrange){
    int result = WRONG_POS;
    _subrange = _subrange.trim ();
    if (_subrange.length () != 0){
      String digitstr = getDigitPart (_subrange);
      try {
        result = Integer.parseInt (digitstr);
        if (result  > MAX_HEIGHT){
          result = WRONG_POS;
        }
      }
      catch (Exception ex) {
        
        result = WRONG_POS;
      }
    }
    return result;
  }
  
  private static boolean isLetter (String _str){
    boolean res = true;
    if ( !_str.equals ("") ){
      for (int i = 0  ; i < _str.length (); i++){
        char ch = _str.charAt (i);
        if (! Character.isLetter (ch)){
          res = false;
          break;
        }
      }
    }else
      res = false;
    return res;
  }
  
  public int getXPosition (String _subrange){
    int result = WRONG_POS;
    String tmp = filter$ (_subrange);
    tmp = this.getCharPart (_subrange);
    // we will process only 2 letters ranges
    if (isLetter (tmp) && ((tmp.length () == 2)|| (tmp.length () == 1) )){
      result =  get26Sys (tmp);
    }
    return result;
  }
  
  public String getDigitPart (String _value){
    String result = "";
    int digitpos = getFirstDigitPosition (_value);
    if(digitpos >= 0){
      result = _value.substring (digitpos);
    }
    return result;
  }
  
  public String getCharPart (String _value){
    String result = "";
    int digitpos = getFirstDigitPosition (_value);
    if(digitpos >= 0){
      result = _value.substring (0, digitpos);
    }
    return result;
  }
  
  private String filter$ (String _range){
    String res = "";
    for (int i = 0 ; i < _range.length () ; i++){
      char ch = _range.charAt (i);
      if  ( ch != '$' ){
        res = res + ch;
      }
    }
    return res;
  }
  
  private int getFirstDigitPosition (String _value){
    int result = WRONG_POS;
    if(_value != null && _value.trim ().length () == 0){
      return result;
    }
    _value = _value.trim ();
    int length = _value.length ();
    for(int i = 0; i < length; i++){
      if(Character.isDigit (_value.charAt (i))){
        result = i;
        break;
      }
    }
    return result;
  }
  
  public int get26Sys (String _s){
    int sum = 0;
    int multiplier = 1;
    if (_s != "") {
      for (int i = _s.length ()-1 ; i >= 0 ; i--){
        char ch = _s.charAt (i);
        int val =  Character.getNumericValue (ch) - Character.getNumericValue ('A')+1;
        sum = sum + val * multiplier;
        multiplier = multiplier * 26;
      }
      return sum;
    }
    return WRONG_POS;
  }
  
  public String numTo26Sys (int _num){
    int sum = 0;
    int reminder;
    String s ="";
    do{
      _num --;
      reminder = _num % 26;
      int val =  65 + reminder;
      _num = _num / 26;
      s = (char)val + s; // reverce
    }while(_num > 0);
    return s;
  }
  
    public String replaceString(String _source , String _oldPattern,
    String _newPattern){
        StringBuffer res = new StringBuffer(_source);
        int pos = -1;
        
        while ((pos = res.toString().indexOf(_oldPattern, pos)) > -1){
            res.replace(pos, pos + _oldPattern.length(), _newPattern);
        }
        
        return res.toString();
    }
    
    public String removeString(String _source, String _match){
        return replaceString(_source, _match, "");
    }
  
}

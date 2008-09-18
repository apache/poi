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
package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Font;

import org.apache.poi.xssf.util.Charset;
import org.apache.poi.xssf.usermodel.extensions.XSSFColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBooleanProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontScheme;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontSize;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIntProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTUnderlineProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTVerticalAlignFontProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STFontScheme;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnderlineValues;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignRun;

import java.util.ArrayList;

public class XSSFFont implements Font {


    public static final int SCHEME_MAJOR=2;
    public static final int SCHEME_MINOR=3;
    public static final int SCHEME_NONE=0;

    public static final int FONT_FAMILY_NOT_APPLICABLE=0;
    public static final int FONT_FAMILY_ROMAN=1;
    public static final int FONT_FAMILY_SWISS=2;
    public static final int FONT_FAMILY_MODERN=3;
    public static final int FONT_FAMILY_SCRIPT=4;
    public static final int FONT_FAMILY_DECORATIVE=5;


    public static final String DEFAULT_FONT_NAME="Calibri";
    public static final short DEFAULT_FONT_SIZE=11;
    public static final short DEFAULT_FONT_COLOR = IndexedColors.BLACK.getIndex();

     private CTFont ctFont;

    public XSSFFont(CTFont font) {
         this.ctFont=font;
    }

     protected XSSFFont() {
         this.ctFont = CTFont.Factory.newInstance();
    }


    public CTFont getCTFont(){
         return ctFont;
    }

     /**
      *
      */
     public boolean getBold() {
         CTBooleanProperty bold=ctFont.sizeOfBArray() == 0 ? null : ctFont.getBArray(0);
         return (bold!=null && bold.getVal());
    }



    public byte getCharSet() {
         CTIntProperty charset= ctFont.sizeOfCharsetArray() == 0?null:ctFont.getCharsetArray(0);
        if(charset!=null){
            //this value must be set -- can't be null
            switch (charset.getVal()) {
            case Charset.ANSI_CHARSET:
                return Font.ANSI_CHARSET;

            case Charset.DEFAULT_CHARSET:
                return Font.DEFAULT_CHARSET;

            case Charset.SYMBOL_CHARSET:
                return Font.SYMBOL_CHARSET;

            default://maight be correct to return this byte value???
                return Byte.parseByte(Integer.toString(charset.getVal()));
            }
        }
        else
            return Font.ANSI_CHARSET;
    }

    public short getColor() {
        CTColor color=ctFont.sizeOfColorArray()==0?null: ctFont.getColorArray(0);
        if(color == null) return Font.COLOR_NORMAL;
        
        long index=color.getIndexed();
        if (index==XSSFFont.DEFAULT_FONT_COLOR){
            return Font.COLOR_NORMAL;
        }
        else if(index == IndexedColors.RED.getIndex()){
            return Font.COLOR_RED;
        }
        else{
            return Short.parseShort(new Long(index).toString());
        }
    }


     public byte[] getRgbColor() {
         CTColor color=ctFont.sizeOfColorArray()==0?null: ctFont.getColorArray(0);
         return color.getRgb();
     }

     public short getThemeColor() {
         CTColor color=ctFont.sizeOfColorArray()==0?null: ctFont.getColorArray(0);
         long index=color.getTheme();
         return (short)index;
     }


    public short getFontHeight() {
         CTFontSize size=ctFont.sizeOfSzArray()==0?null: ctFont.getSzArray(0);
         if(size!=null){
             double fontHeight= size.getVal()/20;
            return (short)fontHeight;
        }
        else
            return DEFAULT_FONT_SIZE/20;
    }

    public short getFontHeightInPoints() {
         CTFontSize size=ctFont.sizeOfSzArray()==0?null: ctFont.getSzArray(0);
         if(size!=null){
             double fontHeight= size.getVal();
             return (short)fontHeight;
          }
        else
            return DEFAULT_FONT_SIZE;
    }


     public String getFontName() {
         CTFontName name=ctFont.sizeOfNameArray()==0?null:ctFont.getNameArray(0);
         return name==null? null:name.getVal();
    }


      public boolean getItalic() {
         CTBooleanProperty italic=ctFont.sizeOfIArray()==0?null:ctFont.getIArray(0);
         return italic!=null && italic.getVal();
      }

      public boolean getStrikeout() {
         CTBooleanProperty strike=ctFont.sizeOfStrikeArray()==0?null:ctFont.getStrikeArray(0);
         return strike!=null && strike.getVal();
      }

    public short getTypeOffset() {
        CTVerticalAlignFontProperty vAlign=ctFont.sizeOfVertAlignArray()==0?null:ctFont.getVertAlignArray(0);
        if(vAlign!=null){
            int val=vAlign.getVal().intValue();
            switch (val) {
            case STVerticalAlignRun.INT_BASELINE:
                return Font.SS_NONE;
            case STVerticalAlignRun.INT_SUBSCRIPT:
                return Font.SS_SUB;
            case STVerticalAlignRun.INT_SUPERSCRIPT:
                return Font.SS_SUPER;
            default: throw new RuntimeException("Wrong offset value "+val);
            }
        }
        else
            return Font.SS_NONE;
    }

     public byte getUnderline() {
         CTUnderlineProperty underline=ctFont.sizeOfUArray()==0?null:ctFont.getUArray(0);
        if(underline!=null){
             switch (underline.getVal().intValue()) {
            case STUnderlineValues.INT_DOUBLE:
                return Font.U_DOUBLE;
            case STUnderlineValues.INT_DOUBLE_ACCOUNTING:
                return Font.U_DOUBLE_ACCOUNTING;

            case STUnderlineValues.INT_SINGLE_ACCOUNTING:
                return Font.U_SINGLE_ACCOUNTING;

            case STUnderlineValues.INT_NONE:
                return Font.U_NONE;

            case STUnderlineValues.INT_SINGLE:
            default:
                return Font.U_SINGLE;
            }
        }
        return Font.U_NONE;
    }

     /**
      * Set characters in bold face font style.
      * If omitted, the default value is true.
      */
     public void setBold(boolean bold) {
         CTBooleanProperty ctBold=ctFont.sizeOfBArray()==0?ctFont.addNewB():ctFont.getBArray(0);
         ctBold.setVal(true);
      }

     /**
      *
      */
    public void setCharSet(byte charset) {
         CTIntProperty charsetProperty=ctFont.sizeOfCharsetArray()==0?ctFont.addNewCharset():ctFont.getCharsetArray(0);
        switch (charset) {
        case Font.ANSI_CHARSET:
            charsetProperty.setVal(Charset.ANSI_CHARSET);
            break;
        case Font.SYMBOL_CHARSET:
            charsetProperty.setVal(Charset.SYMBOL_CHARSET);
            break;
        case Font.DEFAULT_CHARSET:
            charsetProperty.setVal(Charset.DEFAULT_CHARSET);
            break;
        default:
            throw new RuntimeException("Attention: an attempt to set a type of unknow charset and charset");
        }
    }


    public void setColor(short color) {
         CTColor ctColor=ctFont.sizeOfColorArray()==0?ctFont.addNewColor():ctFont.getColorArray(0);

        switch (color) {
        case Font.COLOR_NORMAL:{
            ctColor.setIndexed(XSSFFont.DEFAULT_FONT_COLOR);
            break;
        }
        case Font.COLOR_RED:{
            ctColor.setIndexed(IndexedColors.RED.getIndex());
            break;
        }
        default:
            ctColor.setIndexed(color);
        }
    }



    public void setFontHeight(short height) {
        CTFontSize fontSize=ctFont.sizeOfSzArray()==0?ctFont.addNewSz():ctFont.getSzArray(0);
          fontSize.setVal(height*20);
    }


    public void setFontHeightInPoints(short height) {
          CTFontSize fontSize=ctFont.sizeOfSzArray()==0?ctFont.addNewSz():ctFont.getSzArray(0);
          fontSize.setVal(height);
    }



     public void setRgbColor(XSSFColor color) {
         CTColor ctColor=ctFont.sizeOfColorArray()==0?ctFont.addNewColor():ctFont.getColorArray(0);
         ctColor.setRgb(color.getRgb());
        }

     public void setThemeColor(short theme) {
         CTColor ctColor=ctFont.sizeOfColorArray()==0?ctFont.addNewColor():ctFont.getColorArray(0);
         ctColor.setTheme(theme);
     }

    public void setFontName(String name) {
 		CTFontName fontName=ctFont.sizeOfNameArray()==0?ctFont.addNewName():ctFont.getNameArray(0);
 		fontName.setVal(name);
    }

    public void setItalic(boolean italic) {
         CTBooleanProperty bool=ctFont.sizeOfIArray()==0?ctFont.addNewI():ctFont.getIArray(0);
          bool.setVal(italic);
    }

    public void setStrikeout(boolean strikeout) {
         CTBooleanProperty strike=ctFont.sizeOfStrikeArray()==0?ctFont.addNewStrike():ctFont.getStrikeArray(0);
          strike.setVal(strikeout);
    }

    public void setTypeOffset(short offset) {
         CTVerticalAlignFontProperty offsetProperty=ctFont.sizeOfVertAlignArray()==0?ctFont.addNewVertAlign(): ctFont.getVertAlignArray(0);
        switch (offset) {
        case Font.SS_NONE:
            offsetProperty.setVal(STVerticalAlignRun.BASELINE);
            break;
        case Font.SS_SUB:
            offsetProperty.setVal(STVerticalAlignRun.SUBSCRIPT);
            break;
        case Font.SS_SUPER:
            offsetProperty.setVal(STVerticalAlignRun.SUPERSCRIPT);
            break;
        }
    }

    public void setUnderline(byte underline) {
         CTUnderlineProperty ctUnderline=ctFont.sizeOfUArray()==0?ctFont.addNewU():ctFont.getUArray(0);
        switch (underline) {
        case Font.U_DOUBLE:
            ctUnderline.setVal(STUnderlineValues.DOUBLE);
            break;
        case Font.U_DOUBLE_ACCOUNTING:
            ctUnderline.setVal(STUnderlineValues.DOUBLE_ACCOUNTING);
            break;
        case Font.U_SINGLE_ACCOUNTING:
            ctUnderline.setVal(STUnderlineValues.SINGLE_ACCOUNTING);
            break;
        case Font.U_NONE:
            ctUnderline.setVal(STUnderlineValues.NONE);
            break;

        case Font.U_SINGLE:
        default:
            ctUnderline.setVal(STUnderlineValues.SINGLE);
        break;
        }
    }


     public long putFont(ArrayList<CTFont> fonts) {
        //TODO
        /*
           * we need to implement a method equals to check that 2 instances of CTFont
           * are different by comparison of all font attributes.
           * NB: take a look to findFont method in XSSFWorkbook
           */
         if(fonts.contains(ctFont)) {
             return fonts.indexOf(ctFont);
        }
         fonts.add(ctFont);
        return fonts.size() - 1;
    }

    // solo di xssfFont - non di Font-
    //sono utilizzati nel metodo createDefaultFont in StylesTable insta.

    public int getScheme(){
        CTFontScheme scheme=ctFont.sizeOfSchemeArray()==0?null:ctFont.getSchemeArray(0);
        if(scheme!=null){
            int fontScheme = scheme.getVal().intValue();
            switch (fontScheme) {
            case STFontScheme.INT_MAJOR:
                return XSSFFont.SCHEME_MAJOR;
            case STFontScheme.INT_MINOR:
                return XSSFFont.SCHEME_MINOR;
            case STFontScheme.INT_NONE:
                return XSSFFont.SCHEME_NONE;

            default:
                return fontScheme;
            }
        }
        return 0;
    }


      public void setScheme(int scheme){
         CTFontScheme ctFontScheme=ctFont.sizeOfSchemeArray()==0?ctFont.addNewScheme():ctFont.getSchemeArray(0);
        switch (scheme) {
        case XSSFFont.SCHEME_MAJOR:
            ctFontScheme.setVal(STFontScheme.MAJOR);
            break;
        case XSSFFont.SCHEME_MINOR:
            ctFontScheme.setVal(STFontScheme.MINOR);
            break;
        case XSSFFont.SCHEME_NONE:
            ctFontScheme.setVal(STFontScheme.NONE);
            break;
        default:
            throw new RuntimeException("Schema value ["+ scheme +"] not supported in XSSFFont");
        }
    }


      public int getFamily(){
         CTIntProperty family=ctFont.sizeOfFamilyArray()==0?ctFont.addNewFamily():ctFont.getFamilyArray(0);
         if(family!=null)
             return family.getVal();
          else
              return XSSFFont.FONT_FAMILY_SWISS;
      }

      public void setFamily(int value){
         CTIntProperty family=ctFont.sizeOfFamilyArray()==0?ctFont.addNewFamily():ctFont.getFamilyArray(0);
          family.setVal(value);
     }

}

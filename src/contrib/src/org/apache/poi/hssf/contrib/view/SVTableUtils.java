/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache POI" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache POI", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.poi.hssf.contrib.view;

import java.util.*;
import java.awt.*;
import javax.swing.border.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;

/**
 * SVTableCell Editor and Renderer helper functions.
 *
 * @author     Jason Height
 * @created    16 July 2002
 */
public class SVTableUtils {
  private final static Hashtable colors = HSSFColor.getIndexHash();
  /**  Description of the Field */
  public final static Color black = getAWTColor(new HSSFColor.BLACK());
  /**  Description of the Field */
  public final static Color white = getAWTColor(new HSSFColor.WHITE());
  /**  Description of the Field */
  public static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);


  /**
   *  Creates a new font for a specific cell style
   *
   * @param  wb     Description of the Parameter
   * @param  style  Description of the Parameter
   * @return        Description of the Return Value
   */
  public static Font makeFont(HSSFFont font) {
    boolean isbold = font.getBoldweight() > HSSFFont.BOLDWEIGHT_NORMAL;
    boolean isitalics = font.getItalic();
    int fontstyle = Font.PLAIN;
    if (isbold) {
      fontstyle = Font.BOLD;
    }
    if (isitalics) {
      fontstyle = fontstyle | Font.ITALIC;
    }

    int fontheight = font.getFontHeightInPoints();
    if (fontheight == 9) {
      //fix for stupid ol Windows
      fontheight = 10;
    }

    return new Font(font.getFontName(), fontstyle, fontheight);
  }


  /**
   * This method retrieves the AWT Color representation from the colour hash table
   *
   * @param  index  Description of the Parameter
   * @param  deflt  Description of the Parameter
   * @return        The aWTColor value
   */
  public final static Color getAWTColor(int index, Color deflt) {
    HSSFColor clr = (HSSFColor) colors.get(new Integer(index));
    if (clr == null) {
      return deflt;
    }
    return getAWTColor(clr);
  }


  /**
   *  Gets the aWTColor attribute of the SVTableUtils class
   *
   * @param  clr  Description of the Parameter
   * @return      The aWTColor value
   */
  public final static Color getAWTColor(HSSFColor clr) {
    short[] rgb = clr.getTriplet();
    return new Color(rgb[0], rgb[1], rgb[2]);
  }

}

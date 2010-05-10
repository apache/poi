
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

package org.apache.poi.hssf.view;

import java.util.*;
import java.awt.*;
import javax.swing.border.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;

/**
 * SVTableCell Editor and Renderer helper functions.
 *
 * @author     Jason Height
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
    HSSFColor clr = (HSSFColor) colors.get(Integer.valueOf(index));
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

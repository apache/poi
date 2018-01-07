
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

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;

/**
 * SVTableCell Editor and Renderer helper functions.
 *
 * @author     Jason Height
 */
public class SVTableUtils {
  private final static Map<Integer,HSSFColor> colors = HSSFColor.getIndexHash();
  /**  Description of the Field */
  public final static Color black = getAWTColor(HSSFColorPredefined.BLACK);
  /**  Description of the Field */
  public final static Color white = getAWTColor(HSSFColorPredefined.WHITE);
  /**  Description of the Field */
  public static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);


  /**
   *  Creates a new font for a specific cell style
   */
  public static Font makeFont(HSSFFont font) {
    boolean isbold = font.getBold();
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

  /** This method retrieves the AWT Color representation from the colour hash table
   *
   */
  /* package */ static Color getAWTColor(int index, Color deflt) {
    HSSFColor clr = colors.get(index);
    if (clr == null) {
      return deflt;
    }
    short[] rgb = clr.getTriplet();
    return new Color(rgb[0],rgb[1],rgb[2]);
  }

  /* package */ static Color getAWTColor(HSSFColorPredefined clr) {
    short[] rgb = clr.getTriplet();
    return new Color(rgb[0],rgb[1],rgb[2]);
  }
}

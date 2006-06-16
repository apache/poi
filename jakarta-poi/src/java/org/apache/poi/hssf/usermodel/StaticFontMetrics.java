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

package org.apache.poi.hssf.usermodel;

import java.util.*;
import java.awt.*;
import java.io.*;

/**
 * Allows the user to lookup the font metrics for a particular font without
 * actually having the font on the system.  The font details are loaded
 * as a resource from the POI jar file (or classpath) and should be contained
 * in path "/font_metrics.properties".  The font widths are for a 10 point
 * version of the font.  Use a multiplier for other sizes.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
class StaticFontMetrics
{
    private static Properties fontMetricsProps;
    private static Map fontDetailsMap = new HashMap();

    /**
     * Retrieves the fake font details for a given font.
     * @param font  the font to lookup.
     * @return  the fake font.
     */
    public static FontDetails getFontDetails(Font font)
    {
        if (fontMetricsProps == null)
        {
            InputStream metricsIn = null;
            try
            {
                fontMetricsProps = new Properties();
                if (System.getProperty("font.metrics.filename") != null)
                {
                    String filename = System.getProperty("font.metrics.filename");
                    File file = new File(filename);
                    if (!file.exists())
                        throw new FileNotFoundException("font_metrics.properties not found at path " + file.getAbsolutePath());
                    metricsIn = new FileInputStream(file);
                }
                else
                {
                    metricsIn = FontDetails.class.getResourceAsStream("/font_metrics.properties");
                    if (metricsIn == null)
                        throw new FileNotFoundException("font_metrics.properties not found in classpath");
                }
                fontMetricsProps.load(metricsIn);
            }
            catch ( IOException e )
            {
                throw new RuntimeException("Could not load font metrics: " + e.getMessage());
            }
            finally
            {
                if (metricsIn != null)
                {
                    try
                    {
                        metricsIn.close();
                    }
                    catch ( IOException ignore ) { }
                }
            }
        }

        String fontName = font.getName();

        if (fontDetailsMap.get(fontName) == null)
        {
            FontDetails fontDetails = FontDetails.create(fontName, fontMetricsProps);
            fontDetailsMap.put( fontName, fontDetails );
            return fontDetails;
        }
        else
        {
            return (FontDetails) fontDetailsMap.get(fontName);
        }

    }
}

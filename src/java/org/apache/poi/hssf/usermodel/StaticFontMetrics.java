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

package org.apache.poi.hssf.usermodel;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Allows the user to lookup the font metrics for a particular font without
 * actually having the font on the system. The font details are loaded as a
 * resource from the POI jar file (or classpath) and should be contained in path
 * "/font_metrics.properties". The font widths are for a 10 point version of the
 * font. Use a multiplier for other sizes.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
final class StaticFontMetrics {
	/** The font metrics property file we're using */
	private static Properties fontMetricsProps;
	/** Our cache of font details we've already looked up */
	private static Map<String, FontDetails> fontDetailsMap = new HashMap<String, FontDetails>();

	/**
	 * Retrieves the fake font details for a given font.
	 *
	 * @param font
	 *            the font to lookup.
	 * @return the fake font.
	 */
	public static FontDetails getFontDetails(Font font) {
		// If we haven't already identified out font metrics file,
		// figure out which one to use and load it
		if (fontMetricsProps == null) {
			InputStream metricsIn = null;
			try {
				fontMetricsProps = new Properties();

				// Check to see if the font metric file was specified
				// as a system property
				String propFileName = null;
				try {
					propFileName = System.getProperty("font.metrics.filename");
				} catch (SecurityException e) {
				}

				if (propFileName != null) {
					File file = new File(propFileName);
					if (!file.exists())
						throw new FileNotFoundException(
								"font_metrics.properties not found at path "
										+ file.getAbsolutePath());
					metricsIn = new FileInputStream(file);
				} else {
					// Use the built-in font metrics file off the classpath
					metricsIn = FontDetails.class.getResourceAsStream("/font_metrics.properties");
					if (metricsIn == null)
						throw new FileNotFoundException(
								"font_metrics.properties not found in classpath");
				}
				fontMetricsProps.load(metricsIn);
			} catch (IOException e) {
				throw new RuntimeException("Could not load font metrics: " + e.getMessage());
			} finally {
				if (metricsIn != null) {
					try {
						metricsIn.close();
					} catch (IOException ignore) {
					}
				}
			}
		}

		// Grab the base name of the font they've asked about
		String fontName = font.getName();

		// Some fonts support plain/bold/italic/bolditalic variants
		// Others have different font instances for bold etc
		// (eg font.dialog.plain.* vs font.Californian FB Bold.*)
		String fontStyle = "";
		if (font.isPlain())
			fontStyle += "plain";
		if (font.isBold())
			fontStyle += "bold";
		if (font.isItalic())
			fontStyle += "italic";

		// Do we have a definition for this font with just the name?
		// If not, check with the font style added
		if (fontMetricsProps.get(FontDetails.buildFontHeightProperty(fontName)) == null
				&& fontMetricsProps.get(FontDetails.buildFontHeightProperty(fontName + "."
						+ fontStyle)) != null) {
			// Need to add on the style to the font name
			fontName += "." + fontStyle;
		}

		// Get the details on this font
		if (fontDetailsMap.get(fontName) == null) {
			FontDetails fontDetails = FontDetails.create(fontName, fontMetricsProps);
			fontDetailsMap.put(fontName, fontDetails);
			return fontDetails;
		}
		return fontDetailsMap.get(fontName);
	}
}

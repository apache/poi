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

package org.apache.poi.ss.usermodel.charts;

import org.apache.poi.util.Removal;

/**
 * High level representation of chart legend.
 *
 * @deprecated use XDDFChartLegend instead
 */
@Deprecated
@Removal(version="4.2")
public interface ChartLegend extends ManuallyPositionable {

	/**
	 * @return legend position
	 */
	LegendPosition getPosition();

	/**
	 * @param position new legend position
	 */
	void setPosition(LegendPosition position);

	/**
	 * @return overlay value.
	 */
	boolean isOverlay();

	/**
	 * If true the legend is positioned over the chart area otherwise
	 * the legend is displayed next to it.
	 *
	 * Default is no overlay.
	 *
	 * @param value
	 */
	void setOverlay(boolean value);
}

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
 * Specifies the possible ways to store a chart element's position.
 * @see org.apache.poi.xddf.usermodel.chart.LayoutMode XDDF LayoutMode instead
 */
@Deprecated
@Removal(version="4.2")
public enum LayoutMode {
	/**
	 * Specifies that the Width or Height shall be interpreted as the
	 * Right or Bottom of the chart element.
	 */
	EDGE,
	/**
	 * Specifies that the Width or Height shall be interpreted as the
	 * Width or Height of the chart element.
	 */
	FACTOR
}

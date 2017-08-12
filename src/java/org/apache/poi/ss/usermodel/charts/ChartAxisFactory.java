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

/**
 * A factory for different chart axis.
 *
 * @deprecated
 */
@Deprecated
public interface ChartAxisFactory {

	/**
	 * @param pos
	 * @return new value axis at the end of the list at the specified chart position
	 */
	ValueAxis createValueAxis(AxisPosition pos);

	/**
	 * @param pos
	 * @return new category axis at the end of the list at the specified chart position
	 */
	ChartAxis createCategoryAxis(AxisPosition pos);

	/**
	 * @param pos
	 * @return new date category axis at the end of the list at the specified chart position
	 */
	ChartAxis createDateAxis(AxisPosition pos);

}

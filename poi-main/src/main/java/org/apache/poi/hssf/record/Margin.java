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

package org.apache.poi.hssf.record;

/**
 * The margin interface is a parent used to define left, right, top and bottom margins.
 * This allows much of the code to be generic when it comes to handling margins.
 *
 * @author Shawn Laubach (slaubach at apache dot org)
 */
public interface Margin {
	// TODO - introduce MarginBaseRecord
	/**
	 * Get the margin field for the Margin.
	 */
	public double getMargin();

	/**
	 * Set the margin field for the Margin.
	 */
	public void setMargin(double field_1_margin);
}

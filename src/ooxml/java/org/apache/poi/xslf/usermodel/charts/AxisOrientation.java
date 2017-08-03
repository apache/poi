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

package org.apache.poi.xslf.usermodel.charts;

import java.util.HashMap;

import org.openxmlformats.schemas.drawingml.x2006.chart.STOrientation;

public enum AxisOrientation {
	MIN_MAX(STOrientation.MIN_MAX), MAX_MIN(STOrientation.MAX_MIN);

	final STOrientation.Enum underlying;

	AxisOrientation(STOrientation.Enum orientation) {
		this.underlying = orientation;
	}

	private final static HashMap<STOrientation.Enum, AxisOrientation> reverse = new HashMap<STOrientation.Enum, AxisOrientation>();
	static {
		reverse.put(STOrientation.MIN_MAX, AxisOrientation.MIN_MAX);
		reverse.put(STOrientation.MAX_MIN, AxisOrientation.MAX_MIN);
	}

	static AxisOrientation valueOf(STOrientation.Enum orientation) {
		return reverse.get(orientation);
	}
}

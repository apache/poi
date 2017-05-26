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

import org.openxmlformats.schemas.drawingml.x2006.chart.STCrosses;

public enum AxisCrosses {
	AUTO_ZERO(STCrosses.AUTO_ZERO), MAX(STCrosses.MAX), MIN(STCrosses.MIN);

	final STCrosses.Enum underlying;

	AxisCrosses(STCrosses.Enum crosses) {
		this.underlying = crosses;
	}

	private final static HashMap<STCrosses.Enum, AxisCrosses> reverse = new HashMap<STCrosses.Enum, AxisCrosses>();
	static {
		reverse.put(STCrosses.AUTO_ZERO, AxisCrosses.AUTO_ZERO);
		reverse.put(STCrosses.MAX, AxisCrosses.MAX);
		reverse.put(STCrosses.MIN, AxisCrosses.MIN);
	}

	static AxisCrosses valueOf(STCrosses.Enum crosses) {
		return reverse.get(crosses);
	}
}

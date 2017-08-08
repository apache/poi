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

package org.apache.poi.xddf.usermodel;

import java.util.HashMap;

import org.openxmlformats.schemas.drawingml.x2006.chart.STBarGrouping;

public enum BarGrouping {
	STANDARD(STBarGrouping.STANDARD),
	CLUSTERED(STBarGrouping.CLUSTERED),
	STACKED(STBarGrouping.STACKED),
	PERCENT_STACKED(STBarGrouping.PERCENT_STACKED);

	final STBarGrouping.Enum underlying;

	BarGrouping(STBarGrouping.Enum grouping) {
		this.underlying = grouping;
	}

	private final static HashMap<STBarGrouping.Enum, BarGrouping> reverse = new HashMap<STBarGrouping.Enum, BarGrouping>();
	static {
		reverse.put(STBarGrouping.STANDARD, BarGrouping.STANDARD);
		reverse.put(STBarGrouping.CLUSTERED, BarGrouping.CLUSTERED);
		reverse.put(STBarGrouping.STACKED, BarGrouping.STACKED);
		reverse.put(STBarGrouping.PERCENT_STACKED, BarGrouping.PERCENT_STACKED);
	}

	static BarGrouping valueOf(STBarGrouping.Enum grouping) {
		return reverse.get(grouping);
	}
}

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
package org.apache.poi.util;

import java.util.ArrayList;
import java.util.List;

/**
 * POILogger which logs into an ArrayList, so that 
 *  tests can see what got logged
 */
public class DummyPOILogger extends POILogger {
	public List logged = new ArrayList(); 

	public void reset() {
		logged = new ArrayList();
	}
	
	public boolean check(int level) {
		return true;
	}

	public void initialize(String cat) {}

	public void log(int level, Object obj1) {
		logged.add(new String(level + " - " + obj1));
	}

	public void log(int level, Object obj1, Throwable exception) {
		logged.add(new String(level + " - " + obj1 + " - " + exception));
	}
}

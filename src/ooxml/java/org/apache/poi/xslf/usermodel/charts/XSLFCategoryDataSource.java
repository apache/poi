/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel.charts;

import java.util.List;
import java.util.RandomAccess;

import org.apache.poi.util.Beta;

@Beta
public class XSLFCategoryDataSource implements XSLFDataSource<String> {
	private List<String> data;
	
	public XSLFCategoryDataSource(List<String> list) {
		if (list instanceof RandomAccess) {
			this.data = list;
		} else {
			throw new IllegalArgumentException("List argument should implement RandomAccess, as Vector or ArrayList.");
		}
	}

	@Override
	public int getPointCount() {
		return this.data.size();
	}

	@Override
	public String getPointAt(int index) {
		return this.data.get(index);
	}
}

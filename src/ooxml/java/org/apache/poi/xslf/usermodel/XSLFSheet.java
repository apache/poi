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
package org.apache.poi.xslf.usermodel;

import org.apache.poi.sl.usermodel.Background;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.sl.usermodel.SlideShow;

public abstract class XSLFSheet implements Sheet {
	private SlideShow slideShow;
	protected XSLFSheet(SlideShow parent) {
		this.slideShow = parent;
	}

	public Background getBackground() {
		// TODO Auto-generated method stub
		return null;
	}

	public MasterSheet getMasterSheet() {
		// TODO Auto-generated method stub
		return null;
	}

	public SlideShow getSlideShow() {
		return slideShow;
	}

	public void addShape(Shape shape) {
		// TODO Auto-generated method stub

	}

	public Shape[] getShapes() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean removeShape(Shape shape) {
		// TODO Auto-generated method stub
		return false;
	}
}
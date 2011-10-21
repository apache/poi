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
package org.apache.poi.xslf.usermodel;

import java.awt.Color;

import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeStyleSheet;

public class XSLFColor {
	private final CTColor _ctColor;
	
	XSLFColor(CTColor ctColor){
		_ctColor = ctColor;
	}
	
    @Internal
    public CTColor getXmlObject() {
        return _ctColor;
    }
    
    public Color getColor(){
    	return getColor(0xFF);
    }
	
    public Color getColor(int alpha){
    	Color color = Color.black;
    	if(_ctColor.isSetSrgbClr()){
    		byte[] val = _ctColor.getSrgbClr().getVal();
    		color = new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2], alpha);
    	} else if (_ctColor.isSetSysClr()){
    		byte[] val = _ctColor.getSysClr().getLastClr();
    		color = new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2], alpha);
    	}
    	return color;
    }
}

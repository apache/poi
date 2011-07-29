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
package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.PatternFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;

/**
 * @author Yegor Kozlov
 */
public class XSSFPatternFormatting implements PatternFormatting {
    CTFill _fill;

    XSSFPatternFormatting(CTFill fill){
        _fill = fill;
    }

    public short getFillBackgroundColor(){
        if(!_fill.isSetPatternFill()) return 0;

        return (short)_fill.getPatternFill().getBgColor().getIndexed();
    }

    public short getFillForegroundColor(){
        if(!_fill.isSetPatternFill() || ! _fill.getPatternFill().isSetFgColor())
            return 0;

        return (short)_fill.getPatternFill().getFgColor().getIndexed();
    }

    public short getFillPattern(){
        if(!_fill.isSetPatternFill() || !_fill.getPatternFill().isSetPatternType()) return NO_FILL;

        return (short)(_fill.getPatternFill().getPatternType().intValue() - 1);
     }

    public void setFillBackgroundColor(short bg){
        CTPatternFill ptrn = _fill.isSetPatternFill() ? _fill.getPatternFill() : _fill.addNewPatternFill();
        CTColor bgColor = CTColor.Factory.newInstance();
        bgColor.setIndexed(bg);
        ptrn.setBgColor(bgColor);
    }

    public void setFillForegroundColor(short fg){
        CTPatternFill ptrn = _fill.isSetPatternFill() ? _fill.getPatternFill() : _fill.addNewPatternFill();
        CTColor fgColor = CTColor.Factory.newInstance();
        fgColor.setIndexed(fg);
        ptrn.setFgColor(fgColor);
    }

    public void setFillPattern(short fp){
        CTPatternFill ptrn = _fill.isSetPatternFill() ? _fill.getPatternFill() : _fill.addNewPatternFill();
        if(fp == NO_FILL) ptrn.unsetPatternType();
        else ptrn.setPatternType(STPatternType.Enum.forInt(fp + 1));
    }
}

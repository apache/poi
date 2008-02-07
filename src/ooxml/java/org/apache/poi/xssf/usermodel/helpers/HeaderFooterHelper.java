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

package org.apache.poi.xssf.usermodel.helpers;


public class HeaderFooterHelper {
    
    private static final String HeaderFooterEntity = "&amp;";
    private static final String HeaderFooterEntity_R = "&amp;R";
    private static final String HeaderFooterEntity_L = "&amp;L";
    private static final String HeaderFooterEntity_C = "&amp;C";

    public String getCenterSection(String string) {
        return getSection(string, HeaderFooterEntity_C);
    }
    public String getLeftSection(String string) {
        return getSection(string, HeaderFooterEntity_L);
    }
    public String getRightSection(String string) {
        return getSection(string, HeaderFooterEntity_R);
    }
    
    public String setCenterSection(String string, String newCenter) {
        return setSection(string, newCenter, HeaderFooterEntity_C);
    }
    public String setLeftSection(String string, String newLeft) {
        return setSection(string, newLeft, HeaderFooterEntity_L);
    }
    public String setRightSection(String string, String newRight) {
        return setSection(string, newRight, HeaderFooterEntity_R);
    }
    
    public String setSection(String string, String newSection, String entity) {
        string = string != null ? string : "";
        String oldSection = getSection(string, entity);
        if (oldSection.equals("")) {
            return string.concat(entity + newSection);
        }
        return string.replaceAll(entity + oldSection, entity + newSection);
    }

    private String getSection(String string, String entity) {
        if (string == null) {
            return "";
        }
        String stringAfterEntity = "";
        if (string.indexOf(entity) >= 0) {
            stringAfterEntity = string.substring(string.indexOf(entity) + entity.length());
        }
        String nextEntity = "";
        if (stringAfterEntity.indexOf(HeaderFooterEntity) > 0) {
            nextEntity = stringAfterEntity.substring(stringAfterEntity.indexOf(HeaderFooterEntity), stringAfterEntity.indexOf(HeaderFooterEntity) + (HeaderFooterEntity.length()));
            stringAfterEntity = stringAfterEntity.substring(0, stringAfterEntity.indexOf(nextEntity));
        }
        return stringAfterEntity;
    }

}

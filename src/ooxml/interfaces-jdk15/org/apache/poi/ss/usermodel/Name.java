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

package org.apache.poi.ss.usermodel;

public interface Name {

    /** Get the sheets name which this named range is referenced to
     * @return sheet name, which this named range refered to
     */

    String getSheetName();

    /** 
     * gets the name of the named range
     * @return named range name
     */

    String getNameName();

    /** 
     * sets the name of the named range
     * @param nameName named range name to set
     */

    void setNameName(String nameName);

    /** 
     * gets the reference of the named range
     * @return reference of the named range
     */

    String getReference();

    /** 
     * sets the reference of this named range
     * @param ref the reference to set
     */

    void setReference(String ref);

	boolean isFunctionName();
}

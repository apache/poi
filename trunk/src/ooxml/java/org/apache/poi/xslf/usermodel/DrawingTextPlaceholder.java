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

import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

/**
 * A {@link DrawingTextBody} which is a placeholder
 * @author nick
 *
 */
public class DrawingTextPlaceholder extends DrawingTextBody {
    private final CTPlaceholder placeholder;

    public DrawingTextPlaceholder(CTTextBody textBody, CTPlaceholder placeholder) {
       super(textBody);
       this.placeholder = placeholder;
    }
    
    /**
     * What kind of placeholder is this?
     */
    public String getPlaceholderType() {
       return placeholder.getType().toString();
    }

    /**
     * What kind of placeholder is this?
     */
    public STPlaceholderType.Enum getPlaceholderTypeEnum() {
       return placeholder.getType();
    }

    /**
     * Is the PlaceHolder text customised?
     */
    public boolean isPlaceholderCustom() {
       return placeholder.getHasCustomPrompt();
    }
}

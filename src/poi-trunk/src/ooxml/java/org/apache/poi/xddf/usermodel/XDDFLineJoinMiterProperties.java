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

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineJoinMiterProperties;

@Beta
public class XDDFLineJoinMiterProperties implements XDDFLineJoinProperties {
    private CTLineJoinMiterProperties join;

    public XDDFLineJoinMiterProperties() {
        this(CTLineJoinMiterProperties.Factory.newInstance());
    }

    protected XDDFLineJoinMiterProperties(CTLineJoinMiterProperties join) {
        this.join = join;
    }

    @Internal
    protected CTLineJoinMiterProperties getXmlObject() {
        return join;
    }

    public Integer getLimit() {
        if (join.isSetLim()) {
            return join.getLim();
        } else {
            return null;
        }
    }

    public void setLimit(Integer limit) {
        if (limit == null) {
            if (join.isSetLim()) {
                join.unsetLim();
            }
        } else {
            join.setLim(limit);
        }
    }
}

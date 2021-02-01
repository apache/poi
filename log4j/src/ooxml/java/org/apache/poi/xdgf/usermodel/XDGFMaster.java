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

package org.apache.poi.xdgf.usermodel;

import org.apache.poi.util.Internal;

import com.microsoft.schemas.office.visio.x2012.main.MasterType;

/**
 * Provides the API to work with an underlying master. Typically, each set of
 * stencils used in a Visio diagram are found in a separate master for each.
 */
public class XDGFMaster {

    private MasterType _master;
    protected XDGFMasterContents _content;
    protected XDGFSheet _pageSheet;

    public XDGFMaster(MasterType master, XDGFMasterContents content,
            XDGFDocument document) {
        _master = master;
        _content = content;
        content.setMaster(this);

        if (master.isSetPageSheet())
            _pageSheet = new XDGFPageSheet(master.getPageSheet(), document);
    }

    @Internal
    protected MasterType getXmlObject() {
        return _master;
    }

    @Override
    public String toString() {
        return "<Master ID=\"" + getID() + "\" " + _content + ">";
    }

    public long getID() {
        return _master.getID();
    }

    public String getName() {
        return _master.getName();
    }

    public XDGFMasterContents getContent() {
        return _content;
    }

    public XDGFSheet getPageSheet() {
        return _pageSheet;
    }

}

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

package org.apache.poi.osgi;

import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.MainExtractorFactory;
import org.apache.poi.extractor.ole2.OLE2ScratchpadExtractorFactory;
import org.apache.poi.hslf.usermodel.HSLFSlideShowFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ooxml.extractor.POIXMLExtractorFactory;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XSLFSlideShowFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) {
        WorkbookFactory.addProvider(new HSSFWorkbookFactory());
        WorkbookFactory.addProvider(new XSSFWorkbookFactory());

        SlideShowFactory.addProvider(new HSLFSlideShowFactory());
        SlideShowFactory.addProvider(new XSLFSlideShowFactory());

        ExtractorFactory.addProvider(new OLE2ScratchpadExtractorFactory());
        ExtractorFactory.addProvider(new POIXMLExtractorFactory());
        ExtractorFactory.addProvider(new MainExtractorFactory());
    }

    @Override
    public void stop(BundleContext context) {
        WorkbookFactory.removeProvider(HSSFWorkbookFactory.class);
        WorkbookFactory.removeProvider(XSSFWorkbookFactory.class);

        SlideShowFactory.removeProvider(HSLFSlideShowFactory.class);
        SlideShowFactory.removeProvider(XSLFSlideShowFactory.class);

        ExtractorFactory.removeProvider(OLE2ScratchpadExtractorFactory.class);
        ExtractorFactory.removeProvider(POIXMLExtractorFactory.class);
        ExtractorFactory.removeProvider(MainExtractorFactory.class);
    }
}

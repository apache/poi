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

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Test to ensure that all our main formats can create, write
 * and read back in, when running under OSGi
 */
public class BaseOSGiTestCase {

    @Inject
    private BundleContext bc;

    @Configuration
    public Option[] configuration() throws IOException {
        String bundlePath = System.getProperty("bundle.filename");
        if(bundlePath == null){
            throw new IllegalStateException("-Dbundle.filename property is not set.");
        }
        return options(
                junitBundles(),
                bundle(new File(bundlePath).toURI().toURL().toString()));
    }
}

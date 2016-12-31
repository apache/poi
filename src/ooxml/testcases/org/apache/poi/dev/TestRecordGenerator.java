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

   2012 - Alfresco Software, Ltd.
   Alfresco Software has modified source of this file
   The details of changes as svn diff can be found in svn at location root/projects/3rd-party/src
==================================================================== */
package org.apache.poi.dev;

import org.apache.poi.util.TempFile;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class TestRecordGenerator {
    @Ignore("Could not get this to run, probably the dev-application does not work any more at all")
    @Test
    public void testNotEnoughArgs() throws Exception {
        RecordGenerator.main(new String[] {});
    }

    @Ignore("Could not get this to run, probably the dev-application does not work any more at all")
    @Test
    public void testMainRecords() throws Exception {
        File dir = TempFile.createTempDirectory("TestRecordGenerator");

        RecordGenerator.main(new String[] {
                "src/records/definitions/",
                "src/records/styles/",
                dir.getAbsolutePath(),
                dir.getAbsolutePath(),
        });
    }

    @Ignore("Could not get this to run, probably the dev-application does not work any more at all")
    @Test
    public void testMainTypes() throws Exception {
        File dir = TempFile.createTempDirectory("TestRecordGenerator");

        RecordGenerator.main(new String[] {
                "src/types/definitions/",
                "src/types/styles/",
                dir.getAbsolutePath(),
                dir.getAbsolutePath(),
        });
    }
}
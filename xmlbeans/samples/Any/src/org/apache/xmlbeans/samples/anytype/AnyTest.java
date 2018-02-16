/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.samples.anytype;

import org.apache.xmlbeans.samples.any.RootDocument;

/**
 * A class with which to test the Any sample.
 */
public class AnyTest
{
    /**
     * Tests the Any sample.
     */
    public static void main(String[] args)
    {
        Any sample = new Any();
        boolean newDocIsValid = sample.buildDocFromScratch();
//        assert !newDocIsValid;

        RootDocument rootDoc = (RootDocument)sample.parseXml(args[0]);
        
        boolean domEditsAreValid = sample.editExistingDocWithDOM(rootDoc);
//        assert !domEditsAreValid;
        
        boolean selectPathEditsAreValid = sample.editExistingDocWithSelectPath(rootDoc);
//        assert !selectPathEditsAreValid;

        boolean selectChildrenEditsAreValid = sample.editExistingDocWithSelectChildren(rootDoc);
//        assert !selectChildrenEditsAreValid;
    }
}

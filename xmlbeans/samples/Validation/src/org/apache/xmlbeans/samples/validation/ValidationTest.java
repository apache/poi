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

package org.apache.xmlbeans.samples.validation;

import org.apache.xmlbeans.samples.validation.Validation;

/**
 * A class with which to test the Validation sample.
 */
public class ValidationTest
{
    /**
     * Tests the Validation sample.
     */
    public static void main(String[] args)
    {
        // all we're checking for is that the sample doesn't throw anything.
        // a real sample test might assert something more interesting.
        Validation sample = new Validation();
        // Use the validate method to validate an instance after
        // updates.
        boolean isValidAfterChanges = sample.isValidAfterChanges(args[0]);
        assert !isValidAfterChanges;
        
        // Use the VALIDATE_ON_SET option to validate an instance
        // as updates are made.
        boolean isValidOnTheFly = sample.isValidOnTheFly(args[0]);        
        assert !isValidOnTheFly;
    }
}

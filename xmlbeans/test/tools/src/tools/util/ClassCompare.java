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
package tools.util;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Compare two classes based on methods and fields
 */
public class ClassCompare {

    public String compareClass(Class actual, Class expected) throws Exception {
        Method[] actualMethods = actual.getMethods();
        Method[] expectedMethods = expected.getMethods();
        Field[] actualField = actual.getFields();
        Field[] bField = expected.getFields();

        if (actual.getName() != expected.getName())
            throw new Exception("Class Names were not equal:\n" +
                                "Actual: " + actual.getName() + "\n" +
                                "Expected: " + expected.getName());


        return null;
    }

    public String compareMethods(Method[] actual, Method[] expected) throws Exception {
        if (actual.length != expected.length)
            throw new Exception("Method Count was not equal");

        //for (int i=0; i < actual.length)


        return null;
    }

    public String compareFields(Field[] actual, Field[] expected) throws Exception {


        return null;
    }


}

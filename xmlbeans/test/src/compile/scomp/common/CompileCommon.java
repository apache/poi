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
package compile.scomp.common;

import java.util.Vector;

import common.Common;

/**
 *
 *
 */
public class CompileCommon extends Common{

    public static String fileLocation = XBEAN_CASE_ROOT +P + "compile" + P + "scomp" + P;

    public CompileCommon(String name){
        super(name);
    }

    /** compare contents of two vectors */
    public static void comparefNameVectors(Vector act, Vector exp) throws Exception
    {
        if (exp == null)
            throw new Exception("Exp was null");
        if (act == null)
            throw new Exception("Act was null");

        if (exp.size() != act.size())
            throw new Exception("Size was not the same exp.size:" + exp.size() + " act.size:" + act.size());

        //use Vector.equals to compare
        if (!act.equals(exp))
            throw new Exception("Expected FNames did Not Match");

        //check sequence is as expected (not sure if vector.equals does this
        for (int i = 0; i < exp.size(); i++) {
            if (!exp.get(i).equals(act.get(i)))
                throw new Exception("Item[" + i + "]-was not as expected" +
                        "ACT[" + i + "]-" + act.get(i) + " != EXP[" + i + "]-" + exp.get(i));
        }
    }

}

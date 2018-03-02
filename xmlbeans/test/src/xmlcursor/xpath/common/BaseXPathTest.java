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
package xmlcursor.xpath.common;

import xmlcursor.common.BasicCursorTestCase;

import java.util.HashMap;

public abstract class BaseXPathTest extends BasicCursorTestCase {
    protected HashMap testMap = new HashMap();

    public BaseXPathTest(String sName) {
        super(sName);
        String[] add = new String[]{"//price[position()=0+1]"};
        String[] sub = new String[]{"//price[position()=4-2]"};
        String[] mult = new String[]{"//price[position()=2*1]"};
        String[] div = new String[]{
            "//price[ position()= last() div 2 ]",
            "//price[ position()= 4 div 2 ]",
            "//price[ position()=10 div 0 ]",
            "//price[position()=round(22 div 7)-1]"
        };

        String[] mod = new String[]{
            "//price[ position() mod 2 = 0 ]",
            "//price[position() = 5 mod 3]"
        };

        String[] eq = new String[]{"//bar[price=5.00]"};

        String[] eq_ns = new String[]{"//bar[price=3]"};

        String[] neq = new String[]{"//bar[price!=3]"};

        String[] lt = new String[]{"//bar[price < 2 ]"};
        String[] lteq = new String[]{"//bar[price <=2 ]"};

        String[] gt = new String[]{"//bar[price > 2 ]"};

        String[] gteq = new String[]{"//bar[price >= 2 ]"};

        String[] or = new String[]{"//price[text()>3 or @at=\"val1\"]"};

        String[] and = new String[]{"//price[text()>2 and @at=\"val1\"]"};

        testMap.put("testAddition", add);
        testMap.put("testSubtraction", sub);
        testMap.put("testMultiplication", mult);
        testMap.put("testDiv", div);
        testMap.put("testMod", mod);
        testMap.put("testEqual", eq);
        testMap.put("testEqualityNodeset", eq_ns);
        testMap.put("testNotEqual", neq);
        testMap.put("testLessThan", lt);
        testMap.put("testLessOrEqual", lteq);
        testMap.put("testGreaterThan", gt);
        testMap.put("testGreaterOrEqual", gteq);
        testMap.put("testOr", or);
        testMap.put("testAnd", and);

        testMap.put("testFunctionId", new String[]{
            "id(\"bobdylan\")",
            "id(\"foobar\")",
            "id(\"*\")/child::cd[position()=3]"});

        testMap.put("testFunctionLast", new String[]{
            "/catalog/cd[last()]"});

        testMap.put("testFunctionNamespaceURI", new String[]{
            "//*[namespace-uri(.)=\"uri.org\"]"});

        testMap.put("testFunctionNumber", new String[]{
            "/foo/bar[number(price)+1=4]"});

        testMap.put("testFunctionRound", new String[]{
            "//bar//*[round(text())=3]"});

        testMap.put("testFunctionSum", new String[]{
            "//bar[position()=sum(price)-4]"});

        testMap.put("testFunctionBoolean", new String[]{
            "/foo[boolean(.//@at)]"});

        testMap.put("testFunctionFalse", new String[]{
            "//foo[boolean(price)=false()]"});

        testMap.put("testFunctionLang", new String[]{
            "//price[xf:lang(\"en\")=true()]",
            "//foo[xf:lang(\"en\")=true()]"});

        testMap.put("testFunctionTrue", new String[]{
            "//*[xf:boolean(@at)=true()]"});
    }

    public abstract String getQuery(String testName, int testCase);
}

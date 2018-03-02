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


package dom.checkin;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 *
 */

public class NodeListSyncTest extends NodeListTest {
    public NodeListSyncTest(String s) {
        super(s);
    }

    public static Test suite() {
        return new TestSuite(NodeListSyncTest.class);
    }

    public void setUp() throws Exception {
        super.loadSync();
        super.moveToNode();
    }

}

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

package org.w3c.dom;

public interface UserDataHandler
{
    public static final short NODE_ADOPTED  = 5;
    public static final short NODE_CLONED   = 1;
    public static final short NODE_DELETED  = 3;
    public static final short NODE_IMPORTED = 2;
    public static final short NODE_RENAMED  = 4;

    public void handle ( short operation, String key, Object data, Node src, Node dst );
}
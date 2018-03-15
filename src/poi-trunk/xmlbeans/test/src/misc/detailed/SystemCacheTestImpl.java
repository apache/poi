/*
 *   Copyright 2004 The Apache Software Foundation
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

package misc.detailed;

import org.apache.xmlbeans.impl.common.SystemCache;

// This class provides an alternative implementation to the SystemCache that essentially does nothing special
// Used in testing the SystemCache.set() API to test an alternate implementation being picked up. Used in
// SystemCacheTests
public class SystemCacheTestImpl extends SystemCache
{
       private static int _accessed;

        public SystemCacheTestImpl()
        {
            System.out.println("constructor SystemCacheTestImpl");
            _accessed++;
        }

        public String testCacheImpl()
        {
            return (this.getClass().getName());
        }

        public static final int getAccessed()
        {
            return  _accessed;  
        }

}


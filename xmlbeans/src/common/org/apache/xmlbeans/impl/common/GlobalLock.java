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

package org.apache.xmlbeans.impl.common;

/**
 * Whenever multiple locks must be acquired within the implementation of
 * XML Beans, this GlobalLock is acquired first, and then released when all
 * the acutally-needed locks have been acquired.  This prevents deadlocks.
 */ 
public class GlobalLock
{
    private static final Mutex GLOBAL_MUTEX = new Mutex();
    
    public static void acquire() throws InterruptedException { GLOBAL_MUTEX.acquire(); }
    public static void tryToAcquire() { GLOBAL_MUTEX.tryToAcquire(); }
    public static void release() { GLOBAL_MUTEX.release(); }
}

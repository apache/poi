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

public class Mutex
{
    private Thread  owner      = null;  // Owner of mutex, null if nobody
    private int     lock_count = 0;

    /**
     * Acquire the mutex. The mutex can be acquired multiple times
     * by the same thread, provided that it is released as many
     * times as it is acquired. The calling thread blocks until
     * it has acquired the mutex. (There is no timeout).
     */
    public synchronized void acquire() throws InterruptedException
    {
        while (tryToAcquire() == false)
        {
            wait();
        }
    }

    /**
     * Attempts to acquire the mutex. Returns false (and does not
     * block) if it can't get it.
     */

    public synchronized boolean tryToAcquire()
    {
        // Try to get the mutex. Return true if you got it.

        if( owner == null )
        {
            owner = Thread.currentThread();
            lock_count = 1;
            return true;
        }

        if( owner == Thread.currentThread() )
        {
            ++lock_count;
            return true;
        }

        return false;
    }

    /**
     * Release the mutex. The mutex has to be released as many times
     * as it was acquired to actually unlock the resource. The mutex
     * must be released by the thread that acquired it
     *
     * @throws IllegalStateException (a RuntimeException) if a thread
     *      other than the current owner tries to release the mutex.
     */

    public synchronized void release()
    {
        if (owner != Thread.currentThread())
            throw new IllegalStateException("Thread calling release() doesn't own mutex");

        if (--lock_count <= 0)
        {
            owner = null;
            notify();
        }
    }
}


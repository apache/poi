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
package tools.JUnit;

import junit.framework.TestListener;

import java.io.OutputStream;

public interface JUnitXResultFormatter extends TestListener
{
    /** Signals start of run */
    public void startRun();

    /** Signals end of run */
    public void endRun();

    /** Sets an outputstream to output logs to */
    public void setOutput(OutputStream out);

    /** Tells an ResultFormatter to show stdout/stderr if its capturing
     * the streams
     */
    public void showTestOutput(boolean show);
    /* Any class implementing this will automatically have to
     * implement TestListener
     */
}

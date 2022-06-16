/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.fail;

public class ExcInfo {
    private static final String IGNORED_TESTS = "IGNORE";

    private String file;
    private String tests;
    private String handler;
    private String password;
    private Class<? extends Throwable> exClazz;
    private String exMessage;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getTests() {
        return tests;
    }

    public void setTests(String tests) {
        this.tests = tests;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Class<? extends Throwable> getExClazz() {
        return exClazz;
    }

    @SuppressWarnings("unchecked")
    public void setExClazz(String exClazz) {
        try {
            this.exClazz = (Class<? extends Exception>) Class.forName(exClazz);
        } catch (ClassNotFoundException ex) {
            fail(ex);
        }
    }

    public String getExMessage() {
        return exMessage;
    }

    public void setExMessage(String exMessage) {
        this.exMessage = exMessage;
    }

    public boolean isMatch(String testName, String handler) {
        return
            (tests == null || tests.contains(testName) || IGNORED_TESTS.equals(tests)) &&
            (this.handler == null || this.handler.contains(handler));
    }

    public boolean isValid(String testName, String handler) {
        return
            !IGNORED_TESTS.equals(tests) &&
            (tests == null || (tests.contains(testName) && !tests.contains("!"+testName))) &&
            (this.handler == null || (this.handler.contains(handler) && !this.handler.contains("!"+handler)));
    }
}

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

/* ====================================================================
   This product contains an ASLv2 licensed version of the OOXML signer
   package from the eID Applet project
   http://code.google.com/p/eid-applet/source/browse/trunk/README.txt
   Copyright (C) 2008-2014 FedICT.
   ================================================================= */
package org.apache.poi.util;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * Collection of Junit annotations to control the executions of tests
 */
public class ConditionalExecution {
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtendWith(DisabledOnJreExCondition.class)
    public @interface DisabledOnJreEx {

        /**
         * Version-Strings as Regex
         */
        String[] value();

        /**
         * Reason to provide if the test of container ends up being disabled.
         */
        String disabledReason() default "";

    }

    public static class DisabledOnJreExCondition implements ExecutionCondition {

        DisabledOnJreExCondition() {
        }

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            String version = Runtime.class.getPackage().getImplementationVersion();
            try {
                return findAnnotation(context.getElement(), DisabledOnJreEx.class).filter(annotation -> !isEnabled(annotation))
                        .map(annotation -> disabled("PatchLevel skipped", "JRE version " + version + " skipped"))
                        .orElseGet(() -> enabled("PatchLevel not matched"));
            } catch (IllegalAccessError e) {
                // cannot access org.junit.platform.commons.util.AnnotationUtils when run in JPMS
                // for now let's ignore this check and report "enabled"
                return ConditionEvaluationResult.enabled("Cannot check annotation: " + e);
            }
        }

        boolean isEnabled(DisabledOnJreEx annotation) {
            String[] versions = annotation.value();
            Preconditions.condition(versions.length > 0, "You must declare at least one JRE version in @DisabledOnJreEx");
            String version1 = Runtime.class.getPackage().getImplementationVersion();
            if (version1 == null) {
                // revert to system-property if no implementation version is available
                version1 = System.getProperty("java.version");
            }
            String version = version1;
            return Arrays.stream(versions).noneMatch(p -> Pattern.matches(p, version));
        }
    }
}

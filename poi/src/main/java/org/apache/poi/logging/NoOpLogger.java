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

package org.apache.poi.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.EntryMessage;
import org.apache.logging.log4j.message.FlowMessageFactory;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;

/**
 * A No-Op implementation of the Log4J Logger interface.
 */
final class NoOpLogger implements Logger {

    static final NoOpLogger INSTANCE = new NoOpLogger();

    @Override
    public void catching(Level level, Throwable throwable) {

    }

    @Override
    public void catching(Throwable throwable) {

    }

    @Override
    public void debug(Marker marker, Message message) {

    }

    @Override
    public void debug(Marker marker, Message message, Throwable throwable) {

    }

    @Override
    public void debug(Marker marker, MessageSupplier messageSupplier) {

    }

    @Override
    public void debug(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void debug(Marker marker, CharSequence message) {

    }

    @Override
    public void debug(Marker marker, CharSequence message, Throwable throwable) {

    }

    @Override
    public void debug(Marker marker, Object message) {

    }

    @Override
    public void debug(Marker marker, Object message, Throwable throwable) {

    }

    @Override
    public void debug(Marker marker, String message) {

    }

    @Override
    public void debug(Marker marker, String message, Object... params) {

    }

    @Override
    public void debug(Marker marker, String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void debug(Marker marker, String message, Throwable throwable) {

    }

    @Override
    public void debug(Marker marker, Supplier<?> messageSupplier) {

    }

    @Override
    public void debug(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void debug(Message message) {

    }

    @Override
    public void debug(Message message, Throwable throwable) {

    }

    @Override
    public void debug(MessageSupplier messageSupplier) {

    }

    @Override
    public void debug(MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void debug(CharSequence message) {

    }

    @Override
    public void debug(CharSequence message, Throwable throwable) {

    }

    @Override
    public void debug(Object message) {

    }

    @Override
    public void debug(Object message, Throwable throwable) {

    }

    @Override
    public void debug(String message) {

    }

    @Override
    public void debug(String message, Object... params) {

    }

    @Override
    public void debug(String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void debug(String message, Throwable throwable) {

    }

    @Override
    public void debug(Supplier<?> messageSupplier) {

    }

    @Override
    public void debug(Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void debug(String message, Object p0) {

    }

    @Override
    public void debug(String message, Object p0, Object p1) {

    }

    @Override
    public void debug(String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void debug(String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void entry() {

    }

    @Override
    public void entry(Object... params) {

    }

    @Override
    public void error(Marker marker, Message message) {

    }

    @Override
    public void error(Marker marker, Message message, Throwable throwable) {

    }

    @Override
    public void error(Marker marker, MessageSupplier messageSupplier) {

    }

    @Override
    public void error(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void error(Marker marker, CharSequence message) {

    }

    @Override
    public void error(Marker marker, CharSequence message, Throwable throwable) {

    }

    @Override
    public void error(Marker marker, Object message) {

    }

    @Override
    public void error(Marker marker, Object message, Throwable throwable) {

    }

    @Override
    public void error(Marker marker, String message) {

    }

    @Override
    public void error(Marker marker, String message, Object... params) {

    }

    @Override
    public void error(Marker marker, String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void error(Marker marker, String message, Throwable throwable) {

    }

    @Override
    public void error(Marker marker, Supplier<?> messageSupplier) {

    }

    @Override
    public void error(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void error(Message message) {

    }

    @Override
    public void error(Message message, Throwable throwable) {

    }

    @Override
    public void error(MessageSupplier messageSupplier) {

    }

    @Override
    public void error(MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void error(CharSequence message) {

    }

    @Override
    public void error(CharSequence message, Throwable throwable) {

    }

    @Override
    public void error(Object message) {

    }

    @Override
    public void error(Object message, Throwable throwable) {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void error(String message, Object... params) {

    }

    @Override
    public void error(String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void error(String message, Throwable throwable) {

    }

    @Override
    public void error(Supplier<?> messageSupplier) {

    }

    @Override
    public void error(Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void error(Marker marker, String message, Object p0) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void error(String message, Object p0) {

    }

    @Override
    public void error(String message, Object p0, Object p1) {

    }

    @Override
    public void error(String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void error(String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void exit() {

    }

    @Override
    public <R> R exit(R result) {
        return null;
    }

    @Override
    public void fatal(Marker marker, Message message) {

    }

    @Override
    public void fatal(Marker marker, Message message, Throwable throwable) {

    }

    @Override
    public void fatal(Marker marker, MessageSupplier messageSupplier) {

    }

    @Override
    public void fatal(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void fatal(Marker marker, CharSequence message) {

    }

    @Override
    public void fatal(Marker marker, CharSequence message, Throwable throwable) {

    }

    @Override
    public void fatal(Marker marker, Object message) {

    }

    @Override
    public void fatal(Marker marker, Object message, Throwable throwable) {

    }

    @Override
    public void fatal(Marker marker, String message) {

    }

    @Override
    public void fatal(Marker marker, String message, Object... params) {

    }

    @Override
    public void fatal(Marker marker, String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void fatal(Marker marker, String message, Throwable throwable) {

    }

    @Override
    public void fatal(Marker marker, Supplier<?> messageSupplier) {

    }

    @Override
    public void fatal(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void fatal(Message message) {

    }

    @Override
    public void fatal(Message message, Throwable throwable) {

    }

    @Override
    public void fatal(MessageSupplier messageSupplier) {

    }

    @Override
    public void fatal(MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void fatal(CharSequence message) {

    }

    @Override
    public void fatal(CharSequence message, Throwable throwable) {

    }

    @Override
    public void fatal(Object message) {

    }

    @Override
    public void fatal(Object message, Throwable throwable) {

    }

    @Override
    public void fatal(String message) {

    }

    @Override
    public void fatal(String message, Object... params) {

    }

    @Override
    public void fatal(String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void fatal(String message, Throwable throwable) {

    }

    @Override
    public void fatal(Supplier<?> messageSupplier) {

    }

    @Override
    public void fatal(Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void fatal(String message, Object p0) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public Level getLevel() {
        return null;
    }

    @Override
    public <MF extends MessageFactory> MF getMessageFactory() {
        return null;
    }

    @Override
    public FlowMessageFactory getFlowMessageFactory() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void info(Marker marker, Message message) {

    }

    @Override
    public void info(Marker marker, Message message, Throwable throwable) {

    }

    @Override
    public void info(Marker marker, MessageSupplier messageSupplier) {

    }

    @Override
    public void info(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void info(Marker marker, CharSequence message) {

    }

    @Override
    public void info(Marker marker, CharSequence message, Throwable throwable) {

    }

    @Override
    public void info(Marker marker, Object message) {

    }

    @Override
    public void info(Marker marker, Object message, Throwable throwable) {

    }

    @Override
    public void info(Marker marker, String message) {

    }

    @Override
    public void info(Marker marker, String message, Object... params) {

    }

    @Override
    public void info(Marker marker, String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void info(Marker marker, String message, Throwable throwable) {

    }

    @Override
    public void info(Marker marker, Supplier<?> messageSupplier) {

    }

    @Override
    public void info(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void info(Message message) {

    }

    @Override
    public void info(Message message, Throwable throwable) {

    }

    @Override
    public void info(MessageSupplier messageSupplier) {

    }

    @Override
    public void info(MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void info(CharSequence message) {

    }

    @Override
    public void info(CharSequence message, Throwable throwable) {

    }

    @Override
    public void info(Object message) {

    }

    @Override
    public void info(Object message, Throwable throwable) {

    }

    @Override
    public void info(String message) {

    }

    @Override
    public void info(String message, Object... params) {

    }

    @Override
    public void info(String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void info(String message, Throwable throwable) {

    }

    @Override
    public void info(Supplier<?> messageSupplier) {

    }

    @Override
    public void info(Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void info(Marker marker, String message, Object p0) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void info(String message, Object p0) {

    }

    @Override
    public void info(String message, Object p0, Object p1) {

    }

    @Override
    public void info(String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void info(String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isEnabled(Level level) {
        return false;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker) {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isFatalEnabled() {
        return false;
    }

    @Override
    public boolean isFatalEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public void log(Level level, Marker marker, Message message) {

    }

    @Override
    public void log(Level level, Marker marker, Message message, Throwable throwable) {

    }

    @Override
    public void log(Level level, Marker marker, MessageSupplier messageSupplier) {

    }

    @Override
    public void log(Level level, Marker marker, MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void log(Level level, Marker marker, CharSequence message) {

    }

    @Override
    public void log(Level level, Marker marker, CharSequence message, Throwable throwable) {

    }

    @Override
    public void log(Level level, Marker marker, Object message) {

    }

    @Override
    public void log(Level level, Marker marker, Object message, Throwable throwable) {

    }

    @Override
    public void log(Level level, Marker marker, String message) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object... params) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Throwable throwable) {

    }

    @Override
    public void log(Level level, Marker marker, Supplier<?> messageSupplier) {

    }

    @Override
    public void log(Level level, Marker marker, Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void log(Level level, Message message) {

    }

    @Override
    public void log(Level level, Message message, Throwable throwable) {

    }

    @Override
    public void log(Level level, MessageSupplier messageSupplier) {

    }

    @Override
    public void log(Level level, MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void log(Level level, CharSequence message) {

    }

    @Override
    public void log(Level level, CharSequence message, Throwable throwable) {

    }

    @Override
    public void log(Level level, Object message) {

    }

    @Override
    public void log(Level level, Object message, Throwable throwable) {

    }

    @Override
    public void log(Level level, String message) {

    }

    @Override
    public void log(Level level, String message, Object... params) {

    }

    @Override
    public void log(Level level, String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void log(Level level, String message, Throwable throwable) {

    }

    @Override
    public void log(Level level, Supplier<?> messageSupplier) {

    }

    @Override
    public void log(Level level, Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void log(Level level, String message, Object p0) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void printf(Level level, Marker marker, String format, Object... params) {

    }

    @Override
    public void printf(Level level, String format, Object... params) {

    }

    @Override
    public <T extends Throwable> T throwing(Level level, T throwable) {
        return null;
    }

    @Override
    public <T extends Throwable> T throwing(T throwable) {
        return null;
    }

    @Override
    public void trace(Marker marker, Message message) {

    }

    @Override
    public void trace(Marker marker, Message message, Throwable throwable) {

    }

    @Override
    public void trace(Marker marker, MessageSupplier messageSupplier) {

    }

    @Override
    public void trace(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void trace(Marker marker, CharSequence message) {

    }

    @Override
    public void trace(Marker marker, CharSequence message, Throwable throwable) {

    }

    @Override
    public void trace(Marker marker, Object message) {

    }

    @Override
    public void trace(Marker marker, Object message, Throwable throwable) {

    }

    @Override
    public void trace(Marker marker, String message) {

    }

    @Override
    public void trace(Marker marker, String message, Object... params) {

    }

    @Override
    public void trace(Marker marker, String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void trace(Marker marker, String message, Throwable throwable) {

    }

    @Override
    public void trace(Marker marker, Supplier<?> messageSupplier) {

    }

    @Override
    public void trace(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void trace(Message message) {

    }

    @Override
    public void trace(Message message, Throwable throwable) {

    }

    @Override
    public void trace(MessageSupplier messageSupplier) {

    }

    @Override
    public void trace(MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void trace(CharSequence message) {

    }

    @Override
    public void trace(CharSequence message, Throwable throwable) {

    }

    @Override
    public void trace(Object message) {

    }

    @Override
    public void trace(Object message, Throwable throwable) {

    }

    @Override
    public void trace(String message) {

    }

    @Override
    public void trace(String message, Object... params) {

    }

    @Override
    public void trace(String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void trace(String message, Throwable throwable) {

    }

    @Override
    public void trace(Supplier<?> messageSupplier) {

    }

    @Override
    public void trace(Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void trace(String message, Object p0) {

    }

    @Override
    public void trace(String message, Object p0, Object p1) {

    }

    @Override
    public void trace(String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void trace(String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public EntryMessage traceEntry() {
        return null;
    }

    @Override
    public EntryMessage traceEntry(String format, Object... params) {
        return null;
    }

    @Override
    public EntryMessage traceEntry(Supplier<?>... paramSuppliers) {
        return null;
    }

    @Override
    public EntryMessage traceEntry(String format, Supplier<?>... paramSuppliers) {
        return null;
    }

    @Override
    public EntryMessage traceEntry(Message message) {
        return null;
    }

    @Override
    public void traceExit() {

    }

    @Override
    public <R> R traceExit(R result) {
        return null;
    }

    @Override
    public <R> R traceExit(String format, R result) {
        return null;
    }

    @Override
    public void traceExit(EntryMessage message) {

    }

    @Override
    public <R> R traceExit(EntryMessage message, R result) {
        return null;
    }

    @Override
    public <R> R traceExit(Message message, R result) {
        return null;
    }

    @Override
    public void warn(Marker marker, Message message) {

    }

    @Override
    public void warn(Marker marker, Message message, Throwable throwable) {

    }

    @Override
    public void warn(Marker marker, MessageSupplier messageSupplier) {

    }

    @Override
    public void warn(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void warn(Marker marker, CharSequence message) {

    }

    @Override
    public void warn(Marker marker, CharSequence message, Throwable throwable) {

    }

    @Override
    public void warn(Marker marker, Object message) {

    }

    @Override
    public void warn(Marker marker, Object message, Throwable throwable) {

    }

    @Override
    public void warn(Marker marker, String message) {

    }

    @Override
    public void warn(Marker marker, String message, Object... params) {

    }

    @Override
    public void warn(Marker marker, String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void warn(Marker marker, String message, Throwable throwable) {

    }

    @Override
    public void warn(Marker marker, Supplier<?> messageSupplier) {

    }

    @Override
    public void warn(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void warn(Message message) {

    }

    @Override
    public void warn(Message message, Throwable throwable) {

    }

    @Override
    public void warn(MessageSupplier messageSupplier) {

    }

    @Override
    public void warn(MessageSupplier messageSupplier, Throwable throwable) {

    }

    @Override
    public void warn(CharSequence message) {

    }

    @Override
    public void warn(CharSequence message, Throwable throwable) {

    }

    @Override
    public void warn(Object message) {

    }

    @Override
    public void warn(Object message, Throwable throwable) {

    }

    @Override
    public void warn(String message) {

    }

    @Override
    public void warn(String message, Object... params) {

    }

    @Override
    public void warn(String message, Supplier<?>... paramSuppliers) {

    }

    @Override
    public void warn(String message, Throwable throwable) {

    }

    @Override
    public void warn(Supplier<?> messageSupplier) {

    }

    @Override
    public void warn(Supplier<?> messageSupplier, Throwable throwable) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }

    @Override
    public void warn(String message, Object p0) {

    }

    @Override
    public void warn(String message, Object p0, Object p1) {

    }

    @Override
    public void warn(String message, Object p0, Object p1, Object p2) {

    }

    @Override
    public void warn(String message, Object p0, Object p1, Object p2, Object p3) {

    }

    @Override
    public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {

    }

    @Override
    public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {

    }

    @Override
    public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {

    }

    @Override
    public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {

    }

    @Override
    public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {

    }

    @Override
    public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {

    }
}

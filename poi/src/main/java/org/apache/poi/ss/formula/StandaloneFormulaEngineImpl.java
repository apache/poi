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

package org.apache.poi.ss.formula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CompiledFormula;
import org.apache.poi.ss.usermodel.StandaloneFormulaEngine;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellReference.NameType;
import org.apache.poi.util.Internal;

/**
 * The implementation of the {@link StandaloneFormulaEngine} public API. Also serves
 * as its {@link StandaloneFormulaEngine.Builder} until {@link #build()} is called.
 */
@Internal
public final class StandaloneFormulaEngineImpl implements StandaloneFormulaEngine, StandaloneFormulaEngine.Builder {

    private final List<String> _inputs = new ArrayList<>();
    private SpreadsheetVersion _version = SpreadsheetVersion.EXCEL2007;
    private UDFFinder _udfFinder = AggregatingUDFFinder.DEFAULT;
    private VirtualFormulaWorkbook _parsingWorkbook;
    private String[] _inputNames;

    @Override
    public Builder input(String name) {
        _inputs.add(requireValidName(name));
        return this;
    }

    @Override
    public Builder inputs(String... names) {
        for (String name : names) {
            input(name);
        }
        return this;
    }

    @Override
    public Builder inputs(Collection<String> names) {
        for (String name : names) {
            input(name);
        }
        return this;
    }

    @Override
    public Builder spreadsheetVersion(SpreadsheetVersion version) {
        _version = Objects.requireNonNull(version, "version must not be null");
        return this;
    }

    @Override
    public Builder udfFinder(UDFFinder udfFinder) {
        _udfFinder = Objects.requireNonNull(udfFinder, "udfFinder must not be null");
        return this;
    }

    @Override
    public StandaloneFormulaEngine build() {
        if (_inputNames != null) {
            return this;
        }
        for (String name : _inputs) {
            requireReferenceFreeName(name, _version);
        }
        Set<String> seen = new HashSet<>();
        for (String name : _inputs) {
            if (!seen.add(name.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Duplicate input name '" + name + "'");
            }
        }
        if (_inputs.size() > _version.getMaxColumns()) {
            throw new IllegalArgumentException("Too many inputs: " + _inputs.size()
                    + " exceeds the " + _version.getMaxColumns() + " columns of " + _version);
        }
        _inputNames = _inputs.toArray(new String[0]);
        if (!(_udfFinder instanceof AggregatingUDFFinder)) {
            _udfFinder = new AggregatingUDFFinder(_udfFinder);
        }
        _parsingWorkbook = new VirtualFormulaWorkbook(_version, _udfFinder, null, _inputNames);
        return this;
    }

    @Override
    public CompiledFormula compile(String formula) throws FormulaParseException {
        if (_inputNames == null) {
            throw new IllegalStateException("build() must be called before compile(...)");
        }
        Objects.requireNonNull(formula, "formula must not be null");
        Ptg[] tokens = FormulaParser.parse(formula, _parsingWorkbook, FormulaType.CELL, 0, 0);
        return new CompiledFormulaImpl(this, formula, tokens);
    }

    String[] inputNames() {
        return _inputNames;
    }

    SpreadsheetVersion version() {
        return _version;
    }

    UDFFinder udfFinder() {
        return _udfFinder;
    }

    List<String> inputNamesList() {
        return List.of(_inputNames);
    }

    private static final int MAX_NAME_LENGTH = 255;

    /**
     * Validated eagerly: rules that do not depend on the spreadsheet version.
     * The rules mirror {@code XSSFName.validateName} so that every accepted name is
     * also a legal Excel defined name, plus the {@code TRUE}/{@code FALSE} literals
     * which the formula parser would otherwise swallow as boolean constants.
     */
    private static String requireValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Input name must not be null or blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Invalid name: '" + name + "': cannot exceed "
                    + MAX_NAME_LENGTH + " characters in length");
        }
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.equals("r") || lower.equals("c")) {
            throw new IllegalArgumentException("Invalid name: '" + name + "': cannot be special shorthand R or C");
        }
        if (lower.equals("true") || lower.equals("false")) {
            throw new IllegalArgumentException("Invalid name: '" + name + "': cannot be the boolean literal TRUE or FALSE");
        }
        char first = name.charAt(0);
        if (!Character.isLetter(first) && first != '_' && first != '\\') {
            throw new IllegalArgumentException("Invalid name: '" + name
                    + "': first character must be a letter, underscore or backslash");
        }
        for (char ch : name.toCharArray()) {
            if (!Character.isLetterOrDigit(ch) && ch != '.' && ch != '_' && ch != '\\') {
                throw new IllegalArgumentException("Invalid name: '" + name
                        + "': characters must be letters, digits, periods, underscores or backslashes");
            }
        }
        return name;
    }

    /**
     * Validated at build time when the spreadsheet version is final: a name classified as an
     * A1-style cell reference (e.g. {@code Q1}) would be parsed as a reference to a virtual
     * cell instead of resolving to the input. {@code NameType.COLUMN} bare-letter names
     * (e.g. {@code x}) are deliberately accepted - they are legal Excel defined names and the
     * formula parser resolves them through the name lookup.
     */
    private static void requireReferenceFreeName(String name, SpreadsheetVersion version) {
        NameType type;
        try {
            type = CellReference.classifyCellReference(name, version);
        } catch (IllegalArgumentException e) {
            // classify only rejects strings it cannot look at (e.g. a leading backslash);
            // such names can never be mistaken for a cell reference
            return;
        }
        if (type == NameType.CELL) {
            throw new IllegalArgumentException("Invalid name: '" + name + "': looks like a cell reference and would"
                    + " be parsed as a cell instead of resolving to this input; choose another name");
        }
        if (type == NameType.ROW || type == NameType.BAD_CELL_OR_NAMED_RANGE) {
            throw new IllegalArgumentException("Invalid name: '" + name + "': looks like a row or cell reference"
                    + " instead of a valid input name");
        }
    }
}

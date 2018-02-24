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

import org.apache.xmlbeans.impl.common.ValidatorListener.Event;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;

import org.apache.xmlbeans.*;
import java.util.*;

/**
 * Identity constraint engine. Performs streaming validation of identity constraints.
 * This includes key, keyref, & unique, as well as ID & IDRef.
 */
public class IdentityConstraint {

    private ConstraintState _constraintStack;
    private ElementState _elementStack;
    private Collection _errorListener;
    private boolean _invalid;
    private boolean _trackIdrefs; // We only track idrefs if validating from the root element

    public IdentityConstraint(Collection  errorListener, boolean trackIdrefs) {
        _errorListener = errorListener;
        _trackIdrefs = trackIdrefs;
    }

    public void element(Event e, SchemaType st, SchemaIdentityConstraint[] ics) {

        // Construct a new state for the element
        newState();

        // First dispatch this element event
        for (ConstraintState cs = _constraintStack ; cs != null ; cs = cs._next)
            cs.element(e, st);

        // Create a new SelectorState for each new Identity Constraint

        for (int i = 0 ; ics != null && i < ics.length ; i++)
            newConstraintState(ics[i], e, st);
    }

    public void endElement(Event e) {
        // Pop the element state stack and any constraints at this depth
        if (_elementStack._hasConstraints)
        {
            for (ConstraintState cs = _constraintStack ; cs != null && cs != _elementStack._savePoint ; cs = cs._next)
                cs.remove( e );

            _constraintStack = _elementStack._savePoint;
        }

        _elementStack = _elementStack._next;

        // Dispatch the event
        for (ConstraintState cs = _constraintStack ; cs != null ; cs = cs._next)
            cs.endElement(e);

    }

    public void attr(Event e, QName name, SchemaType st, String value) {
        for (ConstraintState cs = _constraintStack ; cs != null ; cs = cs._next)
            cs.attr(e, name, st, value);
    }

    public void text(Event e, SchemaType st, String value, boolean emptyContent) {
        for (ConstraintState cs = _constraintStack ; cs != null ; cs = cs._next)
            cs.text(e, st, value, emptyContent);
    }

    public boolean isValid() {
        return !_invalid;
    }

    private void newConstraintState(SchemaIdentityConstraint ic, Event e, SchemaType st)
    {
        if (ic.getConstraintCategory() == SchemaIdentityConstraint.CC_KEYREF)
            new KeyrefState(ic, e, st);
        else
            new SelectorState(ic, e, st);
    }

    private void buildIdStates()
    {
        // Construct states to hold the values for IDs and IDRefs
        IdState ids = new IdState();
        if (_trackIdrefs)
            new IdRefState(ids);
    }

    private void newState() {
        boolean firstTime = _elementStack == null;

        ElementState st = new ElementState();
        st._next = _elementStack;
        _elementStack = st;

        if (firstTime)
            buildIdStates();
    }

    private void emitError ( Event event, String code, Object[] args )
    {
        _invalid = true;

        if (_errorListener != null)
        {
            assert event != null;
            
            _errorListener.add(errorForEvent(code, args, XmlError.SEVERITY_ERROR, event));
        }
    }

    public static XmlError errorForEvent(String code, Object[] args, int severity, Event event)
    {
        XmlCursor loc = event.getLocationAsCursor();
        XmlError error;
        if (loc!=null)
            error = XmlError.forCursor(code, args, severity, loc);
        else
        {
            Location location = event.getLocation();
            if (location!=null)
            {
                error = XmlError.forLocation(code, args, severity,
                    location.getSystemId(), location.getLineNumber(),
                    location.getColumnNumber(), location.getCharacterOffset());
            }
            else
            {
                error = XmlError.forMessage(code, args, severity);
            }
        }
        return error;
    }

    private void emitError ( Event event, String msg )
    {
        _invalid = true;

        if (_errorListener != null)
        {
            assert event != null;
            
            _errorListener.add(errorForEvent(msg, XmlError.SEVERITY_ERROR, event));
        }
    }

    public static XmlError errorForEvent(String msg, int severity, Event event)
    {
        XmlCursor loc = event.getLocationAsCursor();
        XmlError error;
        if (loc!=null)
            error = XmlError.forCursor(msg, severity, loc);
        else
        {
            Location location = event.getLocation();
            if (location!=null)
            {
                error = XmlError.forLocation(msg, severity,
                    location.getSystemId(), location.getLineNumber(),
                    location.getColumnNumber(), location.getCharacterOffset());
            }
            else
            {
                error = XmlError.forMessage(msg, severity);
            }
        }
        return error;
    }

    private void setSavePoint( ConstraintState cs )
    {
        if (! _elementStack._hasConstraints)
            _elementStack._savePoint = cs;

        _elementStack._hasConstraints = true;
    }

    private static XmlObject newValue(SchemaType st, String value)
    {
        try {
            return st.newValue(value);
        }
        catch (IllegalArgumentException e) {
            // This is a bit hacky. newValue throws XmlValueOutOfRangeException which is 
            // unchecked and declared in typeimpl. I can only catch its parent class, 
            // typeimpl is built after common.

            // Ignore these exceptions. Assume that validation will catch them.
            return null;
        }
    }

    /**
     * Return the simple type for schema type. If the schema type is already
     * simple, just return it. If it is a complex type with simple content,
     * return the simple type it extends.
     */
    static SchemaType getSimpleType(SchemaType st) 
    {
        assert st.isSimpleType() || st.getContentType() == SchemaType.SIMPLE_CONTENT : 
            st + " does not have simple content.";

        while (! st.isSimpleType() )
            st = st.getBaseType();

        return st;
    }

    static boolean hasSimpleContent(SchemaType st)
    {
        return st.isSimpleType() || st.getContentType() == SchemaType.SIMPLE_CONTENT;
    }

    public abstract class ConstraintState {
        ConstraintState _next;

        ConstraintState()
        {
            setSavePoint(_constraintStack);
            _next = _constraintStack;
            _constraintStack = this;
        }

        abstract void element(Event e, SchemaType st);
        abstract void endElement(Event e);
        abstract void attr(Event e, QName name, SchemaType st, String value);
        abstract void text(Event e, SchemaType st, String value, boolean emptyContent);
        abstract void remove(Event e);

    }

    public class SelectorState extends ConstraintState {
        SchemaIdentityConstraint _constraint;
        Set _values = new LinkedHashSet();
        XPath.ExecutionContext _context;

        SelectorState(SchemaIdentityConstraint constraint, Event e, SchemaType st) {
            _constraint = constraint;
            _context = new XPath.ExecutionContext();
            _context.init((XPath)_constraint.getSelectorPath());

            if ( ( _context.start() & XPath.ExecutionContext.HIT ) != 0 )
                createFieldState(e, st);
        }

        void addFields(XmlObjectList fields, Event e) 
        {
            if (_constraint.getConstraintCategory() == SchemaIdentityConstraint.CC_KEYREF)
                _values.add(fields);
            else if (_values.contains(fields))
            {
                if (_constraint.getConstraintCategory() == SchemaIdentityConstraint.CC_UNIQUE)
                    emitError(e, XmlErrorCodes.IDENTITY_CONSTRAINT_VALID$DUPLICATE_UNIQUE,
                        new Object[] { fields, QNameHelper.pretty(_constraint.getName()) });
                else
                    emitError(e, XmlErrorCodes.IDENTITY_CONSTRAINT_VALID$DUPLICATE_KEY,
                        new Object[] { fields, QNameHelper.pretty(_constraint.getName()) });
            }
            else
                _values.add(fields);
        }

        void element(Event e, SchemaType st) 
        {
            if ( ( _context.element(e.getName()) & XPath.ExecutionContext.HIT) != 0 )
                createFieldState(e, st);
        }

        void endElement(Event e) 
        {
            _context.end();
        }

        void createFieldState(Event e, SchemaType st) {
            new FieldState(this, e, st);
        }

        void remove(Event e) {
            // Bubble up key, unique values to keyrefs
            for (ConstraintState cs = _next ; cs != null ; cs = cs._next )
            {
                if (cs instanceof KeyrefState)
                {
                    KeyrefState kr = (KeyrefState)cs;
                    if (kr._constraint.getReferencedKey() == this._constraint)
                        kr.addKeyValues(_values, true);
                }
            }
        }

        void attr(Event e, QName name, SchemaType st, String value) {}
        void text(Event e, SchemaType st, String value, boolean emptyContent) {}
    }

    public class KeyrefState extends SelectorState {
        Map _keyValues = new HashMap();
        private Object CHILD_ADDED = new Object();
        private Object CHILD_REMOVED = new Object();
        private Object SELF_ADDED = new Object();

        KeyrefState(SchemaIdentityConstraint constraint, Event e, SchemaType st) {
            super(constraint, e, st);
        }

        void addKeyValues(final Set values, boolean child)
        {
            /** If the key values are added by children, then if two or
             more children add the same value, the value dissapears from the map
             but if is added by the element in question directly then it will
             be present in the map regardless of what children contained */
            for (Iterator it = values.iterator(); it.hasNext();)
            {
                Object key = it.next();
                Object value = _keyValues.get(key);
                if (value == null)
                    _keyValues.put(key, child ? CHILD_ADDED : SELF_ADDED);
                else if (value == CHILD_ADDED)
                {
                    if (child)
                        _keyValues.put(key, CHILD_REMOVED);
                    else
                        _keyValues.put(key, SELF_ADDED);
                }
                else if (value == CHILD_REMOVED)
                {
                    if (!child)
                        _keyValues.put(key, SELF_ADDED);
                }
            }
        }

        private boolean hasKeyValue(Object key)
        {
            Object value = _keyValues.get(key);
            return value != null && value != CHILD_REMOVED;
        }

        void remove(Event e) {
            // First check if there are any keys at the same stack level as this
            // that may contribute key values to me
            for (ConstraintState cs = _next ; cs != null && cs != _elementStack._savePoint ; cs = cs._next)
            {
                if (cs instanceof SelectorState)
                {
                    SelectorState sel = (SelectorState)cs;
                    if (sel._constraint == _constraint.getReferencedKey())
                        addKeyValues(sel._values, false);
                }
            }


            // validate all values have been seen
            for (Iterator it = _values.iterator() ; it.hasNext() ; )
            {

                XmlObjectList fields = (XmlObjectList)it.next();
                if (fields.unfilled() < 0 && ! hasKeyValue(fields))
                {
                    // KHK: cvc-identity-constraint.4.3 ?
                	emitError(e, XmlErrorCodes.IDENTITY_CONSTRAINT_VALID$KEYREF_KEY_NOT_FOUND,
                	    new Object[] {fields, QNameHelper.pretty(_constraint.getName())});
                    return;
                }
            }
        }
    }

    public class FieldState extends ConstraintState {
        SelectorState _selector;
        XPath.ExecutionContext[] _contexts;
        boolean[] _needsValue;
        XmlObjectList _value;

        FieldState(SelectorState selector, Event e, SchemaType st) {

            // System.out.println("Creating new Field State for: " + e.getName());

            _selector = selector;
            SchemaIdentityConstraint ic = selector._constraint;

            int fieldCount = ic.getFields().length;
            _contexts = new XPath.ExecutionContext[fieldCount];
            _needsValue = new boolean[fieldCount];
            _value = new XmlObjectList(fieldCount);

            for (int i = 0 ; i < fieldCount ; i++)
            {
                _contexts[i] = new XPath.ExecutionContext();
                _contexts[i].init((XPath)ic.getFieldPath(i));
                if ( ( _contexts[i].start() & XPath.ExecutionContext.HIT ) != 0 )
                {
                    // System.out.println("hit for element: " + e.getName());

                    if (!hasSimpleContent(st))
                        // KHK: cvc-identity-constraint.3
                        emitError(e, "Identity constraint field must have simple content");
                    else
                        _needsValue[i] = true;
                }
            }

        }

        void element(Event e, SchemaType st)
        {
            for (int i = 0 ; i < _contexts.length ; i++) {
                if (_needsValue[i])
                {
                    // KHK: cvc-identity-constraint.3
                    emitError(e, "Identity constraint field must have simple content");
                    _needsValue[i] = false;
                }
            }

            for (int i = 0 ; i < _contexts.length ; i++) {
                if ( ( _contexts[i].element(e.getName()) & XPath.ExecutionContext.HIT) != 0 )
                {
                    if (! hasSimpleContent(st))
                        // KHK: cvc-identity-constraint.3
                        emitError(e, "Identity constraint field must have simple content");
                    else
                        _needsValue[i] = true;
                }
            }
        }

        void attr(Event e, QName name, SchemaType st, String value) {

            // Null value indicates previously reported validation problem
            if (value == null) return;

            for (int i = 0 ; i < _contexts.length ; i++) {
                if ( _contexts[i].attr(name) ) {
                    XmlObject o = newValue(st, value);

                    // Ignore invalid values. Assume that validation catches these
                    if (o == null) return;

                    boolean set = _value.set(o, i);

                    // KHK: ?
                    if (! set)
                        emitError(e, "Multiple instances of field with xpath: '" 
                            + _selector._constraint.getFields()[i] + "' for a selector");
                }

            }
        }


        void text(Event e, SchemaType st, String value, boolean emptyContent) {

            // Null value indicates previously reported validation problem
            if (value == null && !emptyContent) return;

            for (int i = 0 ; i < _contexts.length ; i++) {
                if ( _needsValue[i] ) {

                    if (emptyContent || !hasSimpleContent(st))
                    {
                        // KHK: cvc-identity-constraint.3
                        emitError(e, "Identity constraint field must have simple content");
                        return;
                    }

                    SchemaType simpleType = getSimpleType(st);
                    XmlObject o = newValue(simpleType, value);

                    // Ignore invalid values. Assume that validation catches these
                    if (o == null) return;

                    boolean set = _value.set(o, i);

                    // KHK: ?
                    if (! set)
                        emitError(e, "Multiple instances of field with xpath: '" 
                            + _selector._constraint.getFields()[i] + "' for a selector");
                }
            }
        }

        void endElement(Event e) {
            // reset any  _needsValue flags 
            // assume that if we didn't see the text, it was because of another validation
            // error, so don't emit another one.
            for (int i = 0 ; i < _needsValue.length ; i++)
            {
                _contexts[i].end();
                _needsValue[i] = false;
            }

        }

        void remove(Event e) 
        {

            if (_selector._constraint.getConstraintCategory() == SchemaIdentityConstraint.CC_KEY &&
                _value.unfilled() >= 0 )
            {
                // KHK: cvc-identity-constraint.4.2.1 ?
                // keys must have all values supplied
                emitError(e, "Key " + QNameHelper.pretty(_selector._constraint.getName()) + " is missing field with xpath: '" + _selector._constraint.getFields()[_value.unfilled()] + "'");
            }
            else
            {
                // Finished. Add these fields to the selector state
                _selector.addFields(_value, e);
            }
        }

    }

    public class IdState extends ConstraintState
    {
        Set _values = new LinkedHashSet();

        IdState() { }

        void attr(Event e, QName name, SchemaType st, String value)
        {
            handleValue(e, st, value);
        }

        void text(Event e, SchemaType st, String value, boolean emptyContent)
        {
            if (emptyContent)
                return;

            handleValue(e, st, value);
        }

        private void handleValue(Event e, SchemaType st, String value)
        {

            // Null value indicates previously reported validation problem
            if (value == null) return;

            if (st == null || st.isNoType())
            {
                // ignore invalid values. Assume that validation catches these
                return;
            }

            if (XmlID.type.isAssignableFrom(st))
            {
                XmlObjectList xmlValue = new XmlObjectList(1);
                XmlObject o = newValue(XmlID.type, value);

                // Ignore invalid values. Assume that validation catches these
                if (o == null) return;

                xmlValue.set(o, 0);

                if (_values.contains(xmlValue))
                    emitError(e, XmlErrorCodes.ID_VALID$DUPLICATE, new Object[] { value });
                else
                    _values.add(xmlValue);
            }
        }

        void element(Event e, SchemaType st) {}
        void endElement(Event e){}
        void remove(Event e){}

    }

    public class IdRefState extends ConstraintState
    {
        IdState _ids;
        List _values;

        IdRefState(IdState ids)
        {
            _ids = ids;
            _values = new ArrayList();
        }

        private void handleValue(Event e, SchemaType st, String value)
        {
            // Null value indicates previously reported validation problem
            if (value == null) return;

            if (st == null || st.isNoType())
            {
                // ignore invalid values. Assume that validation catches these
                return;
            }
            if (XmlIDREFS.type.isAssignableFrom(st))
            {
                XmlIDREFS lv = (XmlIDREFS)newValue(XmlIDREFS.type, value);

                // Ignore invalid values. Assume that validation catches these
                if (lv == null) return;

                List l = lv.xgetListValue();

                // Add one value for each idref in the list
                for (int i = 0 ; i < l.size() ; i++)
                {
                    XmlObjectList xmlValue = new XmlObjectList(1);
                    XmlIDREF idref = (XmlIDREF)l.get(i);
                    xmlValue.set(idref, 0);
                    _values.add(xmlValue);
                }
            }
            else if (XmlIDREF.type.isAssignableFrom(st))
            {
                XmlObjectList xmlValue = new XmlObjectList(1);
                XmlIDREF idref = (XmlIDREF)st.newValue(value);

                // Ignore invalid values. Assume that validation catches these
                if (idref == null) return;

                xmlValue.set(idref, 0);
                _values.add(xmlValue);
            }
        }

        void attr(Event e, QName name, SchemaType st, String value) 
        {
            handleValue(e, st, value);
        }
        void text(Event e, SchemaType st, String value, boolean emptyContent) 
        {
            if (emptyContent)
                return;

            handleValue(e, st, value);
        }
        void remove(Event e) 
        { 
            // Validate each ref has a corresponding ID
            for (Iterator it = _values.iterator() ; it.hasNext() ; )
            {
                Object o = it.next();
                if (! _ids._values.contains(o))
                {
                    // KHK: cvc-id.1
                    emitError(e, "ID not found for IDRef value '" + o + "'");
                }
            }
        }
        void element(Event e, SchemaType st) { }
        void endElement(Event e) { }
    }

    private static class ElementState {
        ElementState _next;
        boolean _hasConstraints;
        ConstraintState _savePoint;
    }
}

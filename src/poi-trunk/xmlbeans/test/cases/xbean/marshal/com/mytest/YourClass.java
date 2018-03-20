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

package com.mytest;

import org.apache.xmlbeans.impl.marshal.util.ArrayUtils;
import org.apache.xmlbeans.ObjectFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.Date;

public class YourClass
    implements ObjectFactory
{
    private YourClass myBoss = null;

    private MyClass myClass;
    private MySubClass mySubClass = new MySubClass();

    private float myFloat;
    private float attrib;
    private boolean someBool;

    private List bools = newBoolList();
    private List strs = newStringList();

    private long[] longArray;// = {RND.nextLong(), RND.nextLong()};

    private boolean[] booleanArray;// = {true, false, true};
    private String[] stringArray = {"ONE:" + RND.nextInt(), "TWO:" + RND.nextInt()};
    private MyClass[] myClassArray;//{new MyClass(), new MyClass()};

    private QName qn = new QName("URI" + RND.nextInt(), "LNAME" + RND.nextInt());
    private QName qn2 = new QName("URI" + RND.nextInt(), "LNAME" + RND.nextInt());


    private String[] wrappedArrayOne = {"W1", "W2"};
    private String[][] wrappedArrayTwo = {wrappedArrayOne, null, wrappedArrayOne};

    private SimpleContentExample simpleContentExample;


    private ModeEnum modeEnum;
    private IntEnum intEnum;
    private IntegerEnum integerEnum;

    private String[] simpleStringArray = {"ONE:" + RND.nextInt(),
                                          "TWO:" + RND.nextInt()};

    private long[] simpleLongArray = {RND.nextLong(),
                                      RND.nextLong()};

    final byte[] bytes = new byte[]{1, 2, 3, 4, 5, 6};
    private byte[][] simpleHexBinArray = {bytes, bytes};

    private Date someDate;

    public String stringField = "FLD-" + RND.nextInt();
    public int intField = RND.nextInt();

    public Object[] objectArray;
    public Object[] objectArrayTwo;

    //hack alert
    static final Random RND = new Random();

    private List newStringList()
    {
        ArrayList l = new ArrayList();
        l.add("STRONE:" + RND.nextInt());
        l.add("STRTWO:" + RND.nextInt());
        l.add(null);
        l.add("STRTHREE:" + RND.nextInt());
        return l;
    }

    private List newBoolList()
    {
        ArrayList l = new ArrayList();
        l.add(Boolean.valueOf(RND.nextBoolean()));
        l.add(Boolean.valueOf(RND.nextBoolean()));
        l.add(Boolean.valueOf(RND.nextBoolean()));
        l.add(Boolean.valueOf(RND.nextBoolean()));
        l.add(Boolean.valueOf(RND.nextBoolean()));
        l.add(Boolean.valueOf(RND.nextBoolean()));
        l.add(Boolean.valueOf(RND.nextBoolean()));
        l.add(Boolean.valueOf(RND.nextBoolean()));
//        l.add(null);
//        l.add(Boolean.TRUE);
//        l.add(Boolean.FALSE);
        return l;
    }


    //generic factory
    public Object createObject(Class type)
    {
        if (type == null) throw new IllegalArgumentException("null type");

        if (type.equals(MyClass.class)) {
            return new MyClass();
        } else if (type.equals(MySubClass.class)) {
            return new MySubClass();
        } else if (type.equals(MySubSubClass.class)) {
            return new MySubSubClass();
        } else if (type.equals(YourClass.class)) {
            return new YourClass();
        } else {
            throw new AssertionError("unknown type: " + type);
        }
    }

    public float getMyFloat()
    {
        return myFloat;
    }

    public void setMyFloat(float myFloat)
    {
        this.myFloat = myFloat;
    }

    public YourClass getMyBoss()
    {
        return myBoss;
    }

    public void setMyBoss(YourClass myBoss)
    {
        this.myBoss = myBoss;
    }

    public MyClass getMyClass()
    {
        return myClass;
    }

    public void setMyClass(MyClass myClass)
    {
        this.myClass = myClass;
    }

    public boolean isSomeBool()
    {
        return someBool;
    }

    public void setSomeBool(boolean someBool)
    {
        this.someBool = someBool;
    }

    public List getBools()
    {
        return bools;
    }

    public void setBools(List bools)
    {
        this.bools = bools;
    }


    /**
     *  @xsdgen:attribute.name Attrib
     */
    public float getAttrib()
        throws MyException
    {
        return attrib;
    }

    public void setAttrib(float attrib)
        throws MyException
    {
//        if (attrib < 0.001f)
//           throw new MyException("too small: " + attrib);
        this.attrib = attrib;
    }

    public List getStrs()
    {
        return strs;
    }

    public void setStrs(List strs)
    {
        this.strs = strs;
    }

    public long[] getLongArray()
    {
        return longArray;
    }

    public void setLongArray(long[] longArray)
    {
        this.longArray = longArray;
    }


    public String[] getStringArray()
    {
        return stringArray;
    }

    public void setStringArray(String[] stringArray)
    {
        this.stringArray = stringArray;
    }

    public MyClass[] getMyClassArray()
    {
        return myClassArray;
    }

    public void setMyClassArray(MyClass[] myClassArray)
    {
        this.myClassArray = myClassArray;
    }

    public boolean[] getBooleanArray()
    {
        return booleanArray;
    }

    public void setBooleanArray(boolean[] booleanArray)
    {
        this.booleanArray = booleanArray;
    }


    public MySubClass getMySubClass()
    {
        return mySubClass;
    }

    public void setMySubClass(MySubClass mySubClass)
    {
        this.mySubClass = mySubClass;
    }

    public QName getQn()
    {
        return qn;
    }

    public void setQn(QName qn)
    {
        this.qn = qn;
    }

    public QName getQn2()
    {
        return qn2;
    }

    public void setQn2(QName qn2)
    {
        this.qn2 = qn2;
    }

    public String[] getWrappedArrayOne()
    {
        return wrappedArrayOne;
    }

    public void setWrappedArrayOne(String[] wrappedArrayOne)
    {
        this.wrappedArrayOne = wrappedArrayOne;
    }

    public String[][] getWrappedArrayTwo()
    {
        return wrappedArrayTwo;
    }

    public void setWrappedArrayTwo(String[][] wrappedArrayTwo)
    {
        this.wrappedArrayTwo = wrappedArrayTwo;
    }

    public SimpleContentExample getSimpleContentExample()
    {
        return simpleContentExample;
    }

    public void setSimpleContentExample(SimpleContentExample simpleContentExample)
    {
        this.simpleContentExample = simpleContentExample;
    }

    public ModeEnum getModeEnum()
    {
        return modeEnum;
    }

    public void setModeEnum(ModeEnum modeEnum)
    {
        this.modeEnum = modeEnum;
    }

    public IntEnum getIntEnum()
    {
        return intEnum;
    }

    public void setIntEnum(IntEnum intEnum)
    {
        this.intEnum = intEnum;
    }

    public IntegerEnum getIntegerEnum()
    {
        return integerEnum;
    }

    public void setIntegerEnum(IntegerEnum integerEnum)
    {
        this.integerEnum = integerEnum;
    }

    public String[] getSimpleStringArray()
    {
        return simpleStringArray;
    }

    public void setSimpleStringArray(String[] simpleStringArray)
    {
        this.simpleStringArray = simpleStringArray;
    }

    public long[] getSimpleLongArray()
    {
        return simpleLongArray;
    }

    public void setSimpleLongArray(long[] simpleLongArray)
    {
        this.simpleLongArray = simpleLongArray;
    }

    public Date getSomeDate()
    {
        return someDate;
    }

    public void setSomeDate(Date someDate)
    {
        this.someDate = someDate;
    }

    public byte[][] getSimpleHexBinArray()
    {
        return simpleHexBinArray;
    }

    public void setSimpleHexBinArray(byte[][] simpleHexBinArray)
    {
        this.simpleHexBinArray = simpleHexBinArray;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof YourClass)) return false;

        final YourClass yourClass = (YourClass)o;

        if (attrib != yourClass.attrib) return false;
        if (myFloat != yourClass.myFloat) return false;
        if (someBool != yourClass.someBool) return false;
        if (!Arrays.equals(booleanArray, yourClass.booleanArray)) return false;

        if (strs != null ? !strs.equals(yourClass.strs) : yourClass.strs != null) return false;
        if (bools != null ? !bools.equals(yourClass.bools) : yourClass.bools != null) return false;

        if (!Arrays.equals(longArray, yourClass.longArray)) return false;
        if (myClass != null ? !myClass.equals(yourClass.myClass) : yourClass.myClass != null) return false;
        if (!Arrays.equals(myClassArray, yourClass.myClassArray)) return false;
        if (!Arrays.equals(stringArray, yourClass.stringArray)) return false;
        if (!Arrays.equals(simpleStringArray, yourClass.simpleStringArray)) return false;
        if (!Arrays.equals(simpleLongArray, yourClass.simpleLongArray)) return false;
        if (!Arrays.equals(wrappedArrayOne, yourClass.wrappedArrayOne)) return false;

        if (qn != null ? !qn.equals(yourClass.qn) : yourClass.qn != null) return false;
        if (qn2 != null ? !qn2.equals(yourClass.qn2) : yourClass.qn2 != null) return false;

        if (simpleContentExample != null ?
            !simpleContentExample.equals(yourClass.simpleContentExample) : yourClass.simpleContentExample != null)
            return false;

        if (modeEnum != null ? !modeEnum.equals(yourClass.modeEnum) : yourClass.modeEnum != null) return false;
        if (intEnum != null ? !intEnum.equals(yourClass.intEnum) : yourClass.intEnum != null) return false;
        if (integerEnum != null ? !integerEnum.equals(yourClass.integerEnum) : yourClass.integerEnum != null) return false;

        if (stringField != null ? !stringField.equals(yourClass.stringField) : yourClass.stringField != null) return false;
        if (intField != yourClass.intField) return false;


        return true;
    }

    public int hashCode()
    {
        int result;
        result = (myClass != null ? myClass.hashCode() : 0);
        result = 29 * result + Float.floatToIntBits(myFloat);
        result = 29 * result + Float.floatToIntBits(attrib);
        result = 29 * result + (someBool ? 1 : 0);
//        result = 29 * result + (bools != null ? bools.hashCode() : 0);
//        result = 29 * result + (strs != null ? strs.hashCode() : 0);
        return result;
    }


    public String toString()
    {
        return "com.mytest.YourClass{" +
            "myClass=" + myClass +
            ", stringField=" + stringField +
            ", myFloat=" + myFloat +
            ", attrib=" + attrib +
            ", someBool=" + someBool +
            ", qn=" + qn +
            ", qn2=" + qn2 +
            ", modeEnum=" + modeEnum +
            ", intEnum=" + intEnum +
            ", integerEnum=" + integerEnum +
            ", simpleContentExample=" + simpleContentExample +
            ", bools=" + (bools == null ? null : "size:" + bools.size() + bools) +
            ", strs=" + (strs == null ? null : "size:" + strs.size() + strs) +
            ", longArray=" + ArrayUtils.arrayToString(longArray) +
            ", booleanArray=" + ArrayUtils.arrayToString(booleanArray) +
            ", stringArray=" + ArrayUtils.arrayToString(stringArray) +
            ", simpleStringArray=" + ArrayUtils.arrayToString(simpleStringArray) +
            ", simpleLongArray=" + ArrayUtils.arrayToString(simpleLongArray) +
            ", simpleHexBinArray=" + ArrayUtils.arrayToString(simpleHexBinArray) +
            ", wrappedArrayOne=" + ArrayUtils.arrayToString(wrappedArrayOne) +
            ", myClassArray=" + ArrayUtils.arrayToString(myClassArray) +
            "}";
    }


}

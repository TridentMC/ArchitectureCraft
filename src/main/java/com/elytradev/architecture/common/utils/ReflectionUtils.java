/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.architecture.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    public static Class classForName(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getFieldDef(Class cls, String unobfName, String obfName) {
        try {
            Field field;
            try {
                field = cls.getDeclaredField(unobfName);
            } catch (NoSuchFieldException e) {
                field = cls.getDeclaredField(obfName);
            }
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Cannot find field %s or %s of %s", unobfName, obfName, cls.getName()),
                    e);
        }
    }

    public static Object getField(Object obj, String unobfName, String obfName) {
        Field field = getFieldDef(obj.getClass(), unobfName, obfName);
        return getField(obj, field);
    }

    public static Object getField(Object obj, Field field) {
        try {
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getIntField(Object obj, Field field) {
        try {
            return field.getInt(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setField(Object obj, String unobfName, String obfName, Object value) {
        Field field = getFieldDef(obj.getClass(), unobfName, obfName);
        setField(obj, field, value);
    }

    public static void setField(Object obj, Field field, Object value) {
        try {
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setIntField(Object obj, Field field, int value) {
        try {
            field.setInt(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethodDef(Class cls, String unobfName, String obfName, Class... params) {
        try {
            Method meth;
            try {
                meth = cls.getDeclaredMethod(unobfName, params);
            } catch (NoSuchMethodException e) {
                meth = cls.getDeclaredMethod(obfName, params);
            }
            meth.setAccessible(true);
            return meth;
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Cannot find method %s or %s of %s", unobfName, obfName, cls.getName()),
                    e);
        }
    }

    public static Object invokeMethod(Object target, Method meth, Object... args) {
        try {
            return meth.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

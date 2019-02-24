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

package com.tridevmc.architecture.common.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static String[] split(String sep, String string) {
        List<String> list = new ArrayList<String>();
        String[] result = new String[0];
        int i = 0;
        while (i < string.length()) {
            int j = string.indexOf(sep, i);
            if (j < 0)
                j = string.length();
            list.add(string.substring(i, j));
            i = j + sep.length();
        }
        result = list.toArray(result);
        return result;
    }

    public static String join(String sep, String[] strings) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String s : strings) {
            if (first)
                first = false;
            else
                result.append(sep);
            result.append(s);
        }
        return result.toString();
    }

}

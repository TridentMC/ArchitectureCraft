//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - String Utilities
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture;

import java.util.ArrayList;
import java.util.List;

public class BaseStringUtils {

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

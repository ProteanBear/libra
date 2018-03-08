package com.github.proteanbear.libra.utils;

import java.util.UUID;

/**
 * String processing tools
 *
 * @author ProteanBear
 */
public class StringUtils
{
    /**
     * Is the string blank?
     *
     * @param string The string.
     * @return When string is blank,the result is true.
     */
    public static boolean isBlank(String string)
    {
        return (string==null || "".equals(string.trim()));
    }

    /**
     * Is not the string blank?
     *
     * @param string The string.
     * @return When string is not blank,the result is true.
     */
    public static boolean isNotBlank(String string)
    {
        return (string!=null&&!"".equals(string.trim()));
    }

    /**
     * Generate uuid
     *
     * @return uuid without '-'.All lowercase.
     */
    public static String uuid()
    {
        return UUID.randomUUID().toString().replace("-","");
    }
}
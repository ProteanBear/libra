package com.github.proteanbear.libra.framework;

/**
 * Enumeration, used to record the key used in the framework.
 *
 * @author ProteanBear
 */
public enum LibraKey
{
    //The key of task configuration.
    CONFIG("JobConfig");

    /**
     * The key.
     */
    private String key;

    /**
     * Constructor
     *
     * @param key The key.
     */
    LibraKey(String key)
    {
        this.key=key;
    }

    /**
     * @return The key.
     */
    @Override
    public String toString()
    {
        return key;
    }
}
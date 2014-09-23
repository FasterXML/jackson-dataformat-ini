package com.fasterxml.jackson.dataformat.ini;

/**
 * Container object for various settings that determine how INI file
 * contents are encoded; including quoting.
 */
public class IniConfig
{
    private final static IniConfig STD = new IniConfig();
    
    public static IniConfig std() {
        return STD;
    }
}

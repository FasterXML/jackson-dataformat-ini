package com.fasterxml.jackson.dataformat.ini;

import com.fasterxml.jackson.core.base.ParserMinimalBase;

public class IniParser
    extends ParserMinimalBase
{
    /**
     * Enumeration that defines all togglable features for CSV parsers
     */
    public enum Feature {
        // Placeholder: can't have Enums without entries
        _BOGUS(false),
        ;

        final boolean _defaultState;
        final int _mask;
        
        public static int collectDefaults()
        {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }
        
        private Feature(boolean defaultState) {
            _defaultState = defaultState;
            _mask = (1 << ordinal());
        }
        
        public boolean enabledByDefault() { return _defaultState; }
        public int getMask() { return _mask; }
    }
}

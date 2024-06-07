

package com.inksetter.twist.exec;

import java.util.Map;

public interface SymbolSource {
    public static SymbolSource of(Map<String, Object> map) {
        return new SimpleContext(map);
    }
    
    Object lookup(String name);
}
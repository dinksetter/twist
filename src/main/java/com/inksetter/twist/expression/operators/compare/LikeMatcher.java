package com.inksetter.twist.expression.operators.compare;

public class LikeMatcher {
    public LikeMatcher(String pattern) {
        _p = pattern.toCharArray();
    }

    public boolean match(String s) {
        return (_testMatch(s, 0, 0));
    }

    //
    // Implementation
    //
    private boolean _testMatch(String s, int sPos, int pPos) {
        // Go through the entire pattern string;
        while (pPos < _p.length) {
            // If we hit a wildcard, do some special processing
            final char p = _p[pPos];
            switch(p) {
                case '%':
                    // For each "substring" created by "stripping off" each
                    // matching character from the string to be matched, and
                    // recursively matching it with the rest of the pattern
                    // string.
                    while (sPos < s.length ()) {
                        if (_testMatch(s, sPos, pPos + 1)) return true;
                        sPos++;
                    }

                    break;

                case '_':
                    if (sPos == s.length()) return false;
                    sPos++;
                    break;

                default:
                    // If there's pattern left, but there's no string left,
                    // it's not a match.  Also, if the characters don't match
                    // each other, it's not a match (duh!)
                    if (sPos == s.length() || s.charAt(sPos) != p) {
                        return false;
                    }

                    // Move along...
                    sPos++;
                    break;
            }
            // OK, we've matched as much as we can, continue on with
            // the next pattern character.
            pPos++;
        }

        // We've made it to the end of the pattern.  Is there string left?
        return sPos >= s.length();
    }

    private final char[] _p;
}

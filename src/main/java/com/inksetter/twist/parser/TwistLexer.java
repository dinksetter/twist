package com.inksetter.twist.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexer for TWIST script syntax.  This splits input up into tokens to be used by the parser.
 * 
 * @author dinksett
 * @version $Revision: 283720 $
 */
public class TwistLexer {
    
    public TwistLexer(CharSequence in) {
        _in = in;
        _length = in.length();
        _pos = 0;
        _line = 0;
    }
    
    /**
     * The actual tokens returned by the lexer. Tokens are snapshots into the original script string.  We keep track of the beginning position, the ending position, and the leading whitespace.
     */
    public class TwistToken {
        public TwistTokenType getType() {
            return _type;
        }
        
        public String getValue() {
            return _in.subSequence(_beginToken, _end).toString();
        }
        
        public String toString() {
            return _in.subSequence(_beginWhitespace, _end).toString();
        }
        
        public String getLeadingWhitespace() {
            return _in.subSequence(_beginWhitespace, _beginToken).toString();
        }
        
        //
        // Implementation
        //
        private TwistToken(TwistTokenType type, int beginWhitespace, int beginToken) {
            _type = type;
            _beginToken = beginToken;
            _beginWhitespace = beginWhitespace;
            _end = _pos;
        }
        
        private final TwistTokenType _type;
        private final int _beginToken;
        private final int _beginWhitespace;
        private final int _end;
    }

    /**
     * A reference into the current lexer state.
     */
    public static class Reference {
        private Reference(int pos) {
            _refPos = pos;
        }
        private final int _refPos;
    }
    
    /**
     * Marks the current place in the input string. This can be used to get a portion
     * of the input string later.
     * @return
     */
    public Reference markPlace() {
        return new Reference(_currentToken._beginWhitespace);
    }
    
    /**
     * Gets a portion of the input as a string.  This can be used to get large, complex
     * parsed areas as text.
     * @param mark
     * @return
     */
    public String getTextSinceMark(Reference mark) {
        return _in.subSequence(mark._refPos, _currentToken._beginToken).toString();
    }

    public void reset(TwistToken token) {
        _currentToken = token;
        _pos = token._end;
    }
    
    /**
     * Return the current token under the lexer's thumb.
     * @return
     */
    public TwistToken current() {
        return _currentToken;
    }
    
    /**
     * Return the type of the current token of the lexer.  This is equivalent to calling
     * current().getType().
     * @return
     */
    public TwistTokenType tokenType() {
        return _currentToken._type;
    }

    /**
     * Gets the line on which the current token sits.
     * @return
     */
    public int getLine() {
        return _line;
    }
    
    /**
     * Gets the position within the line at which the current token sits.
     * @return
     */
    public int getLinePos() {
        return _linePos - (_pos - _currentToken._beginToken);
    }
    
    /**
     * Advance the token lexer and return the next token in our string.  This method removes
     * comments from the stream.
     * @return
     * @throws TwistLexException
     */
    public TwistToken next() throws TwistLexException {
        do {
            _currentToken = _nextToken();
        } while (_currentToken._type == TwistTokenType.COMMENT);

        return _currentToken;
    }

    /**
     * Return all remaining tokens in this statement.  Any tokens already
     * returned will not be processed again.  Note that this method does
     * not remove comments from the stream.
     *     
     * @return an array of token objects representing the individual 
     * tokens in the local syntax statement.
     * 
     * @throws TwistLexException if there was a problem parsing the SQL statement.
     */
    public TwistToken[] getAllTokens() throws TwistLexException {
        List<TwistToken> tokens = new ArrayList<>();
        TwistToken token;
        do {
            token = _nextToken();
            tokens.add(token);
        } while (token.getType() != TwistTokenType.END);
        
        return tokens.toArray(new TwistToken[tokens.size()]);
    }
    
    /**
     * Builds a string from an array of script elements (keywords, etc.).
     * @param elements a list of token objects that make up a script.
     * @return the resulting script string.
     */
    public static String getString(TwistToken[] elements) {
        StringBuilder buf = new StringBuilder();
        for (TwistToken element : elements) {
            buf.append(element);
        }
        return buf.toString();
    }

    /**
     * Builds a string from a collection of script elements (keywords, etc.).
     * @param elements a Collection object containing token objects that make up a script.
     * @return the resulting script string.
     */
    public static String getString(Collection<TwistToken> elements) {
        StringBuilder buf = new StringBuilder();
        for (TwistToken e : elements) {
            buf.append(e);
        }
        return buf.toString();
    }
    
    //
    // Implementation
    //
    
    /**
     * Return the next token.
     * @return a TwistToken object representing the next token in the script. When the end of the statement is reached, a token of type
     * <code>TwistTokenType.END</code> is returned.  Subsequent calls, after the
     * end token is returned will also return an end token.
     * @throws TwistLexException if there was a problem parsing the SQL
     * statement.
     */
    private TwistToken _nextToken() throws TwistLexException {
        // Keep track of where we started
        _begin = _pos;
        _skipWhitespace();
        
        // If we're done, mark the end of the statement
        if (!_hasNext()) {
            return new TwistToken(TwistTokenType.END, _begin, _pos);
        }
        
        // We need to keep track of where this token's significant text began
        _startOfToken = _pos;
        
        // Read the first character
        char c = _nextChar();
        
        if (Character.isDigit(c)) {
            return _readNumeric(c);
        }
        else if (Character.isLetter(c) || c == '_') {
            return _readIdentifier();
        }
        else {
            switch (c) {
            case '\'':
            case '"':
                return new TwistToken(_readStringLiteral(c), _begin, _startOfToken);
            case '.':
                return new TwistToken(TwistTokenType.DOT, _begin, _startOfToken);
            case ';':
                return new TwistToken(TwistTokenType.SEMICOLON, _begin, _startOfToken);
            case '|': 
                if (_hasNext() && _peekChar() == '|') {
                    // This is to go over the | character
                    _nextChar();
                    return new TwistToken(TwistTokenType.OR, _begin, _startOfToken);
                }
                else {
                    throw new TwistLexException(_line + 1, _linePos + 1, "Unrecognized identifier: " + c);
                }
            case '&':
                if (_hasNext() && _peekChar() == '&') {
                    // This is to go over the & character
                    _nextChar();
                    return new TwistToken(TwistTokenType.AND, _begin, _startOfToken);
                }
                else {
                    throw new TwistLexException(_line + 1, _linePos + 1, "Unrecognized identifier: " + c);
                }
            case '(':
                return new TwistToken(TwistTokenType.OPEN_PAREN, _begin, _startOfToken);
            case ')':
                return new TwistToken(TwistTokenType.CLOSE_PAREN, _begin, _startOfToken);
            case '{':
                return new TwistToken(TwistTokenType.OPEN_BRACE, _begin, _startOfToken);
            case '}':
                return new TwistToken(TwistTokenType.CLOSE_BRACE, _begin, _startOfToken);
            case '[':
                return new TwistToken(TwistTokenType.OPEN_BRACKET, _begin, _startOfToken);
            case ']':
                return new TwistToken(TwistTokenType.CLOSE_BRACKET, _begin, _startOfToken);
            case '*':
                return new TwistToken(TwistTokenType.STAR, _begin, _startOfToken);
            case '+':
                return new TwistToken(TwistTokenType.PLUS, _begin, _startOfToken);
            case '-':
                return new TwistToken(TwistTokenType.MINUS, _begin, _startOfToken);
            case '?':
                return new TwistToken(TwistTokenType.QUESTION, _begin, _startOfToken);
            case ':':
                return new TwistToken(TwistTokenType.COLON, _begin, _startOfToken);
            case '/':
                if (_hasNext() && (_peekChar() == '*')) {
                    // This is to go over the * character
                    _nextChar();
                    do {
                        c = _nextChar();
                    } while (c != '*' || _peekChar() != '/');
                    // This is to go over the / character
                    _nextChar();
                    return new TwistToken(TwistTokenType.COMMENT, _begin, _startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.SLASH, _begin, _startOfToken);
                }
            case '%':
                return new TwistToken(TwistTokenType.PERCENT, _begin, _startOfToken);
            case ',':
                return new TwistToken(TwistTokenType.COMMA, _begin, _startOfToken);
            case '=':
                if (_hasNext() && _peekChar() == '=') {
                    // This is to go over the = character
                    _nextChar();
                    return new TwistToken(TwistTokenType.EQ, _begin, _startOfToken);
                }
                else if (_hasNext() && _peekChar() == '~') {
                    // This is to go over the = character
                    _nextChar();
                    return new TwistToken(TwistTokenType.MATCH, _begin, _startOfToken);
                }

                else {
                    return new TwistToken(TwistTokenType.ASSIGNMENT, _begin, _startOfToken);
                }
            case '>':
                if (_hasNext() && _peekChar() == '=') {
                    // This is to go over the = character
                    _nextChar();
                    return new TwistToken(TwistTokenType.GE, _begin, _startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.GT, _begin, _startOfToken);
                }
            case '<':
                if (_hasNext() && _peekChar() == '=') {
                    // This is to go over the = character
                    _nextChar();
                    return new TwistToken(TwistTokenType.LE, _begin, _startOfToken);
                }
                else if (_hasNext() && _peekChar() == '>') {
                    // This is to go over the > character
                    _nextChar();
                    return new TwistToken(TwistTokenType.NE, _begin, _startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.LT, _begin, _startOfToken);
                }
            case '!':
                if (_hasNext() && _peekChar() == '=') {
                    // This is to go over the = character
                    _nextChar();
                    return new TwistToken(TwistTokenType.NE, _begin, _startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.BANG, _begin, _startOfToken);
                }
            default:
                throw new TwistLexException(_line + 1, _linePos + 1, "Unrecognized identifier: " + c);
            }
        }
    }
    
    private TwistToken _readNumeric(char c) throws TwistLexException {
        // If the first character is a plus or minus, skip over it.
        if (c == '+' || c == '-') {
            _nextChar();
        }
        
        // As long as the next character will be a digit, we should include it.
        while (_hasNext() && Character.isDigit(_peekChar())) {
            _nextChar();
        }
        
        // If the next character is a decimal point, continue.
        if (_hasNext() && _peekChar() == '.' ) {
            _nextChar();
        }
        
        // Now we have numbers after the decimal point.       
        while (_hasNext() && Character.isDigit(_peekChar())) {
            _nextChar();
        }
        
        // Scientific notation -- ##.##e[+-]###
        if (_hasNext() && (_peekChar() == 'e' || _peekChar() == 'E')) {
            // We expect another number if we see the exponential notation
            _nextChar();
            
            if (_peekChar() == '+' || _peekChar() == '-') {
                _nextChar();
            }
            
            while (_hasNext() && Character.isDigit(_peekChar())) {
                _nextChar();
            }
        }
        else if (_hasNext() && (Character.isLetter(_peekChar()) || _peekChar() == '_')) {
            return _readIdentifier();
        }
        return new TwistToken(TwistTokenType.NUMBER, _begin, _startOfToken);
    }
    
    private boolean _isValidIdentifier(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')||
                c == '_' || Character.isDigit(c));
    }
    
    private TwistToken _readIdentifier() throws TwistLexException {
        while (_hasNext() && _isValidIdentifier(_peekChar())) {
             _nextChar();
        }
        
        String word = _in.subSequence(_startOfToken, _pos).toString();
        TwistTokenType wordType = _RESERVED.get(word.toLowerCase());
        if (wordType == null) {
            wordType = TwistTokenType.IDENTIFIER;
        }

        return new TwistToken(wordType, _begin, _startOfToken);
    }
    
    private TwistTokenType _readStringLiteral(char c) throws TwistLexException {
        
        char lookfor = c;

        do {
            c = _nextChar();

            while (c == lookfor && _hasNext() && _peekChar() == lookfor) {
                _nextChar();
                c = _nextChar();
            }
        } while (c != lookfor);

        if (lookfor == '\'') {
            return TwistTokenType.SINGLE_STRING;
        }
        else {
            return TwistTokenType.DOUBLE_STRING;
        }
    }
    
    private void _skipWhitespace() throws TwistLexException {
        while (_pos < _length && Character.isWhitespace(_peekChar())) {
            _nextChar();
        }
    }
    
    private char _nextChar() throws TwistLexException {
        if (_pos >= _length) {
            throw new TwistLexException(_line + 1, _linePos + 1, "Unexpected end of text");
        }
        _linePos++;
        char next = _in.charAt(_pos++);

        if (next == '\n') {
            _line++;
            _linePos = 0;
        }
        
        return next;
    }
    
    private char _peekChar() {
        return _in.charAt(_pos);
    }
    
    private boolean _hasNext() {
        return _pos < _length;
    }

    //
    // Implementation
    //
    private final CharSequence _in;
    private final int _length;
    private int _pos;
    private int _line;
    private int _linePos;
    private int _begin;
    private int _startOfToken;
    private TwistToken _currentToken;
    
    private final static Map<String, TwistTokenType> _RESERVED = new HashMap<>();
    
    static {
        _RESERVED.put("if", TwistTokenType.IF);
        _RESERVED.put("else", TwistTokenType.ELSE);
        _RESERVED.put("not", TwistTokenType.NOT);
        _RESERVED.put("like", TwistTokenType.LIKE);
        _RESERVED.put("try", TwistTokenType.TRY);
        _RESERVED.put("true", TwistTokenType.TRUE);
        _RESERVED.put("false", TwistTokenType.FALSE);
        _RESERVED.put("catch", TwistTokenType.CATCH);
        _RESERVED.put("finally", TwistTokenType.FINALLY);
    }
}

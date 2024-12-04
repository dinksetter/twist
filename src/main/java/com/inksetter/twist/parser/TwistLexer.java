package com.inksetter.twist.parser;

import java.util.*;

/**
 * Lexer for TWIST script syntax.  This splits input up into tokens to be used by the parser.
 * 
 * @author dinksett
 * @version $Revision: 283720 $
 */
public class TwistLexer {
    private final CharSequence in;
    private final int length;
    private int pos;
    private int line;
    private int linePos;
    private int begin;
    private int startOfToken;
    private TwistToken currentToken;

    private final static Map<String, TwistTokenType> RESERVED = new HashMap<>();

    static {
        RESERVED.put("if", TwistTokenType.IF);
        RESERVED.put("else", TwistTokenType.ELSE);
        RESERVED.put("not", TwistTokenType.NOT);
        RESERVED.put("like", TwistTokenType.LIKE);
        RESERVED.put("for", TwistTokenType.FOR);
        RESERVED.put("try", TwistTokenType.TRY);
        RESERVED.put("true", TwistTokenType.TRUE);
        RESERVED.put("false", TwistTokenType.FALSE);
        RESERVED.put("catch", TwistTokenType.CATCH);
        RESERVED.put("return", TwistTokenType.RETURN);
        RESERVED.put("def", TwistTokenType.DEF);
        RESERVED.put("finally", TwistTokenType.FINALLY);
    }

    public TwistLexer(CharSequence in) {
        this.in = in;
        length = in.length();
        pos = 0;
        line = 0;
    }
    
    /**
     * The actual tokens returned by the lexer. Tokens are snapshots into the original script string.  We keep track of the beginning position, the ending position, and the leading whitespace.
     */
    public class TwistToken {
        public TwistTokenType getType() {
            return _type;
        }
        
        public String getValue() {
            return in.subSequence(_beginToken, _end).toString();
        }
        
        public String toString() {
            return in.subSequence(_beginWhitespace, _end).toString();
        }
        
        public String getLeadingWhitespace() {
            return in.subSequence(_beginWhitespace, _beginToken).toString();
        }
        
        //
        // Implementation
        //
        private TwistToken(TwistTokenType type, int beginWhitespace, int beginToken) {
            _type = type;
            _beginToken = beginToken;
            _beginWhitespace = beginWhitespace;
            _end = pos;
        }
        
        private final TwistTokenType _type;
        private final int _beginToken;
        private final int _beginWhitespace;
        private final int _end;
    }
    
    /**
     * Return the current token under the lexer's thumb.
     * @return the current token
     */
    public TwistToken current() {
        return currentToken;
    }
    
    /**
     * Return the type of the current token of the lexer.  This is equivalent to calling
     * current().getType().
     * @return the type of the current token
     */
    public TwistTokenType tokenType() {
        return currentToken._type;
    }

    /**
     * Gets the line on which the current token sits.
     * @return the current line number of the parsed input.
     */
    public int getLine() {
        return line;
    }
    
    /**
     * Gets the position within the line at which the current token sits.
     * @return the line position (in characters) of the current token
     */
    public int getLinePos() {
        return linePos - (pos - (currentToken != null ? currentToken._beginToken : 0));
    }

    public int getPos() {
        return pos;
    }
    
    /**
     * Advance the token lexer and return the next token in our string.  This method removes
     * comments from the stream.
     * @return the next token (which will become the current token)
     */
    public TwistToken next() throws ScriptTokenException {
        do {
            currentToken = nextToken();
        } while (currentToken._type == TwistTokenType.COMMENT);

        return currentToken;
    }

    //
    // Implementation
    //
    
    /**
     * Return the next token.
     * @return a TwistToken object representing the next token in the script. When the end of the statement is reached, a token of type
     * <code>TwistTokenType.END</code> is returned.  Subsequent calls, after the
     * end token is returned will also return an end token.
     * @throws ScriptTokenException if there was a problem parsing the SQL
     * statement.
     */
    private TwistToken nextToken() throws ScriptTokenException {
        // Keep track of where we started
        begin = pos;
        skipWhitespace();
        
        // If we're done, mark the end of the statement
        if (!hasNext()) {
            return new TwistToken(TwistTokenType.END, begin, pos);
        }
        
        // We need to keep track of where this token's significant text began
        startOfToken = pos;
        
        // Read the first character
        char c = nextChar();
        
        if (Character.isDigit(c)) {
            return readNumeric(c);
        }
        else if (Character.isLetter(c) || c == '_') {
            return readIdentifier();
        }
        else {
            switch (c) {
            case '\'':
            case '"':
                if (hasNext(2) && peekChar(1) == c && peekChar(2) == c) {
                    // Skip over the next two characters
                    nextChar();
                    nextChar();
                    return new TwistToken(readMultiLineString(c), begin, startOfToken);
                }
                else {
                    return new TwistToken(readStringLiteral(c), begin, startOfToken);
                }
            case '.':
                return new TwistToken(TwistTokenType.DOT, begin, startOfToken);
            case ';':
                return new TwistToken(TwistTokenType.SEMICOLON, begin, startOfToken);
            case '|': 
                if (hasNext() && peekChar() == '|') {
                    // This is to go over the | character
                    nextChar();
                    return new TwistToken(TwistTokenType.OR, begin, startOfToken);
                }
                else {
                    throw new ScriptTokenException(this, "Unrecognized identifier: " + c);
                }
            case '&':
                if (hasNext() && peekChar() == '&') {
                    // This is to go over the & character
                    nextChar();
                    return new TwistToken(TwistTokenType.AND, begin, startOfToken);
                }
                else {
                    throw new ScriptTokenException(this, "Unrecognized identifier: " + c);
                }
            case '(':
                return new TwistToken(TwistTokenType.OPEN_PAREN, begin, startOfToken);
            case ')':
                return new TwistToken(TwistTokenType.CLOSE_PAREN, begin, startOfToken);
            case '{':
                return new TwistToken(TwistTokenType.OPEN_BRACE, begin, startOfToken);
            case '}':
                return new TwistToken(TwistTokenType.CLOSE_BRACE, begin, startOfToken);
            case '[':
                return new TwistToken(TwistTokenType.OPEN_BRACKET, begin, startOfToken);
            case ']':
                return new TwistToken(TwistTokenType.CLOSE_BRACKET, begin, startOfToken);
            case '*':
                if (hasNext() && peekChar() == '=') {
                    nextChar();
                    return new TwistToken(TwistTokenType.STARASSIGN, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.STAR, begin, startOfToken);
                }
            case '+':
                if (hasNext() && peekChar() == '=') {
                    nextChar();
                    return new TwistToken(TwistTokenType.PLUSASSIGN, begin, startOfToken);
                }
                else if (hasNext() && peekChar() == '+') {
                    nextChar();
                    return new TwistToken(TwistTokenType.INCREMENT, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.PLUS, begin, startOfToken);
                }
            case '-':
                if (hasNext() && peekChar() == '=') {
                    nextChar();
                    return new TwistToken(TwistTokenType.MINUSASSIGN, begin, startOfToken);
                }
                else if (hasNext() && peekChar() == '-') {
                    nextChar();
                    return new TwistToken(TwistTokenType.DECREMENT, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.MINUS, begin, startOfToken);
                }
            case '?':
                if (hasNext() && peekChar() == ':') {
                    nextChar();
                    return new TwistToken(TwistTokenType.ELVIS, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.QUESTION, begin, startOfToken);
                }
            case ':':
                return new TwistToken(TwistTokenType.COLON, begin, startOfToken);
            case '/':
                if (hasNext() && (peekChar() == '*')) {
                    // This is to go over the * character
                    nextChar();
                    do {
                        c = nextChar();
                    } while (c != '*' || peekChar() != '/');
                    // This is to go over the / character
                    nextChar();
                    return new TwistToken(TwistTokenType.COMMENT, begin, startOfToken);
                }
                else if (hasNext() && peekChar() == '/') {
                    // This is to go over the / character
                    nextChar();
                    while (hasNext() && peekChar() != '\n') {
                        nextChar();
                    }
                    return new TwistToken(TwistTokenType.COMMENT, begin, startOfToken);
                }
                else if (hasNext() && peekChar() == '=') {
                    nextChar();
                    return new TwistToken(TwistTokenType.SLASHASSIGN, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.SLASH, begin, startOfToken);
                }
            case '%':
                if (hasNext() && peekChar() == '=') {
                    nextChar();
                    return new TwistToken(TwistTokenType.PERCENTASSIGN, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.PERCENT, begin, startOfToken);
                }
            case ',':
                return new TwistToken(TwistTokenType.COMMA, begin, startOfToken);
            case '=':
                if (hasNext() && peekChar() == '=') {
                    // This is to go over the = character
                    nextChar();
                    if (hasNext() && peekChar() == '~') {
                        // This is to go over the = character
                        nextChar();
                        return new TwistToken(TwistTokenType.MATCH, begin, startOfToken);
                    }
                    else {
                        return new TwistToken(TwistTokenType.EQ, begin, startOfToken);
                    }
                }
                else if (hasNext() && peekChar() == '~') {
                    // This is to go over the = character
                    nextChar();
                    return new TwistToken(TwistTokenType.FIND, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.ASSIGNMENT, begin, startOfToken);
                }
            case '>':
                if (hasNext() && peekChar() == '=') {
                    // This is to go over the = character
                    nextChar();
                    return new TwistToken(TwistTokenType.GE, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.GT, begin, startOfToken);
                }
            case '<':
                if (hasNext() && peekChar() == '=') {
                    // This is to go over the = character
                    nextChar();
                    return new TwistToken(TwistTokenType.LE, begin, startOfToken);
                }
                else if (hasNext() && peekChar() == '>') {
                    // This is to go over the > character
                    nextChar();
                    return new TwistToken(TwistTokenType.NE, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.LT, begin, startOfToken);
                }
            case '!':
                if (hasNext() && peekChar() == '=') {
                    // This is to go over the = character
                    nextChar();
                    return new TwistToken(TwistTokenType.NE, begin, startOfToken);
                }
                else if (hasNext() && peekChar() == '~') {
                    nextChar();
                    return new TwistToken(TwistTokenType.NMATCH, begin, startOfToken);
                }
                else {
                    return new TwistToken(TwistTokenType.BANG, begin, startOfToken);
                }
            default:
                throw new ScriptTokenException(this, "Unrecognized identifier: " + c);
            }
        }
    }
    
    private TwistToken readNumeric(char c) throws ScriptTokenException {
        // If the first character is a plus or minus, skip over it.
        if (c == '+' || c == '-') {
            nextChar();
        }
        
        // As long as the next character will be a digit, we should include it.
        while (hasNext() && Character.isDigit(peekChar())) {
            nextChar();
        }
        
        // If the next character is a decimal point, continue.
        if (hasNext() && peekChar() == '.' ) {
            nextChar();
        }
        
        // Now we have numbers after the decimal point.       
        while (hasNext() && Character.isDigit(peekChar())) {
            nextChar();
        }
        
        // Scientific notation -- ##.##e[+-]###
        if (hasNext() && (peekChar() == 'e' || peekChar() == 'E')) {
            // We expect another number if we see the exponential notation
            nextChar();
            
            if (peekChar() == '+' || peekChar() == '-') {
                nextChar();
            }
            
            while (hasNext() && Character.isDigit(peekChar())) {
                nextChar();
            }
        }
        else if (hasNext() && (Character.isLetter(peekChar()) || peekChar() == '_')) {
            return readIdentifier();
        }
        return new TwistToken(TwistTokenType.NUMBER, begin, startOfToken);
    }
    
    private boolean isValidIdentifier(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')||
                c == '_' || Character.isDigit(c));
    }
    
    private TwistToken readIdentifier() throws ScriptTokenException {
        while (hasNext() && isValidIdentifier(peekChar())) {
             nextChar();
        }
        
        String word = in.subSequence(startOfToken, pos).toString();
        TwistTokenType wordType = RESERVED.get(word.toLowerCase());
        if (wordType == null) {
            wordType = TwistTokenType.IDENTIFIER;
        }

        return new TwistToken(wordType, begin, startOfToken);
    }
    
    private TwistTokenType readStringLiteral(char c) throws ScriptTokenException {
        
        char lookfor = c;

        do {
            c = nextChar();

            while (c == lookfor && hasNext() && peekChar() == lookfor) {
                nextChar();
                c = nextChar();
            }
        } while (c != lookfor);

        if (lookfor == '\'') {
            return TwistTokenType.SINGLE_STRING;
        }
        else {
            return TwistTokenType.DOUBLE_STRING;
        }
    }

    private TwistTokenType readMultiLineString(char lookfor) throws ScriptTokenException {
        char c;

        do {
            c = nextChar();

            if (c == lookfor && hasNext(2) && peekChar(1) == lookfor && peekChar(2) == lookfor) {
                nextChar();
                nextChar();
                return TwistTokenType.MULTI_STRING;
            }
        } while (hasNext());
        throw new ScriptTokenException(this, "unexpected end of string");
    }
    
    private void skipWhitespace() throws ScriptTokenException {
        while (pos < length && Character.isWhitespace(peekChar())) {
            nextChar();
        }
    }
    
    private char nextChar() throws ScriptTokenException {
        if (pos >= length) {
            throw new ScriptTokenException(this, "Unexpected end of text");
        }
        linePos++;
        char next = in.charAt(pos++);

        if (next == '\n') {
            line++;
            linePos = 0;
        }
        
        return next;
    }
    
    private char peekChar() {
        return in.charAt(pos);
    }

    private char peekChar(int ahead) {
        return in.charAt(pos + (ahead - 1));
    }

    private boolean hasNext() {
        return pos < length;
    }

    private boolean hasNext(int ahead) {
        return (pos + (ahead - 1)) < length;
    }
}

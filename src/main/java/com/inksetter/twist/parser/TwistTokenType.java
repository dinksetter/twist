package com.inksetter.twist.parser;

public enum TwistTokenType {
    END,
    COMMENT,
    IDENTIFIER,
    NUMBER,
    DOUBLE_STRING,
    SINGLE_STRING,
    MULTI_STRING,
    SEMICOLON,
    ASSIGNMENT,
    STARASSIGN, PLUSASSIGN, MINUSASSIGN, SLASHASSIGN, PERCENTASSIGN,
    INCREMENT, DECREMENT,
    DOT,
    OPEN_PAREN, CLOSE_PAREN,
    OPEN_BRACE, CLOSE_BRACE,
    OPEN_BRACKET, CLOSE_BRACKET, COMMA,

    // operators
    EQ, NE, LT, GT, LE, GE, MATCH, FIND, NMATCH,
    BANG, AND, OR,
    STAR, PLUS, MINUS, SLASH, PERCENT,
    NOT, LIKE, QUESTION, COLON, ELVIS,

    // reserved words
    NULL_TOKEN,
    IF,
    ELSE,
    FOR,
    TRY,
    CATCH,
    DEF,
    FINALLY,
    TRUE, FALSE,
    RETURN
}

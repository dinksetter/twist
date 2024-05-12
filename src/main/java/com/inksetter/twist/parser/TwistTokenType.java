package com.inksetter.twist.parser;

public enum TwistTokenType {
    END,
    COMMENT,
    IDENTIFIER,
    NUMBER,
    DOUBLE_STRING,
    SINGLE_STRING,
    SEMICOLON,
    ASSIGNMENT,
    DOT,
    OPEN_PAREN, CLOSE_PAREN,
    OPEN_BRACE, CLOSE_BRACE,
    OPEN_BRACKET, CLOSE_BRACKET, COMMA,

    // operators
    EQ, NE, LT, GT, LE, GE, MATCH,
    BANG, AND, OR,
    STAR, PLUS, MINUS, SLASH, PERCENT,
    NOT, LIKE, QUESTION, COLON,

    // reserved words
    NULL_TOKEN,
    IF,
    ELSE,
    FOR,
    TRY,
    CATCH,
    FINALLY,
    TRUE, FALSE,
}

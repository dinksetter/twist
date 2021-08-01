package com.inksetter.twist.parser;

public enum TwistTokenType {
    EOF,
    VARWORD,
    NUMBER,
    DOUBLE_STRING,
    SINGLE_STRING,
    SEMICOLON,
    ASSIGNMENT,
    DOT,
    OPEN_PAREN, CLOSE_PAREN,
    OPEN_BRACE, CLOSE_BRACE,
    OPEN_BRACKET, CLOSE_BRACKET,
    EQ, NE, LT, GT, LE, GE, LIKE,
    AND, OR, NOT,
    NULL_TOKEN,
    BANG,
    STAR, PLUS, MINUS, SLASH, PERCENT,
    IF,
    ELSE,
    TRY,
    CATCH,
    FINALLY,
    TRUE, FALSE,
    COMMA,
    COMMENT, QUESTION, COLON
}

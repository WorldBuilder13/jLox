/*
    This code is copied from the book Crating Intepreter by Robert Nystrom

    Changes to the base code I've made: (changes will be kept on branches of main)
    +TBD
*/

package com.craftinginterpreters.lox;

enum TokenType{
    //single-character tokens. < Book comment
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    //One or two character tokens. < Book comment
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    //Literals. < Book comment
    IDENTIFIER, STRING, NUMBER,
    
    //Keywords. < Book comment
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}
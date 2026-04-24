package com.craftinginterpreters.lox;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0; //first character in lexeme
    private int current = 0; //current character being considered
    private int line = 1; //source line that current is on

    Scanner(String source){
        this.source = source;
    }

    //creates Tokens and adds them to tokens list
    List<Token> scanTokens(){
        while (!isAtEnd()){
            // we are at the beginnign of the next lexeme < Book comment
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    //reades lexeme and compares to toekn
    private void scanToken(){
        char c = advance();
        switch(c){
            //single character tokens
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=')?BANG_EQUAL:BANG);
                break;
            default:
                Lox.error(line, "Unexpected character.");
                break;
                //TODO: the error reporting her will spam many small messages, lump together
        }
    }

    // helper functions
    // handles when all characters have been consumed
    private boolean isAtEnd(){
        return current >= source.length();
    }
    // consumes next character in stream and consumes it
    private char advance(){
        return source.charAt(current++);
    }

    //wrapper for null literal
    private void addToken(TokenType type){
        addToken(type, null);
    }

    //adds token to tokens list
    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    
}

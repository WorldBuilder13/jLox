/*
    This code is copied from the book Crating Intepreter by Robert Nystrom

    Changes to the base code I've made: (changes will be kept on branches of main)
    +TBD
*/

package com.craftinginterpreters.lox;



import static com.craftinginterpreters.lox.TokenType.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    private int start = 0; //first character in lexeme
    private int current = 0; //current character being considered
    private int line = 1; //source line that current is on

    //list of keywords, adds them to keywords map
    static { //static initialization block
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

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
        switch(c){ //book uses standard switch, rule switch might make it cleaner
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
            case '=':
                addToken(match('-')?EQUAL_EQUAL:EQUAL);
                break;
            case '<':
                addToken(match('=')?LESS_EQUAL:LESS);
                break;
            case '>':
                addToken(match('=')?GREATER_EQUAL:GREATER);
                break;
            case '/':
                if(match('/')){
                    // A comment goes until the end of the line. < Book Comment
                    while(peek() != '\n'&& !isAtEnd()) advance();
                }
                else{
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                //ignore whitespace
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if(isDigit(c)){ //checking for number
                    number();
                }
                else if (isAlpha(c)){ //checking for identifier
                    identifier();
                }
                else{
                    Lox.error(line, "Unexpected character.");
                }
                break;
            //TODO:  the error reporting her will spam many small messages, lump together
        }
    }

    // helper functions

    //handles identifiiers
    private void identifier(){
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current); //pulls identifier 
        TokenType type = keywords.get(text); //pulls type of word from keywords map
        if(type == null) type = IDENTIFIER; // if identifier wasn't in reserved keywords
        addToken(type);
    }

    // handles converting string to number token. numbers must start and end with digit
    // Lox uses doubles
    // good: 0.1, 1, 2.5
    // bad .1, 2.
    private void number(){
        while(isDigit(peek())) advance();

        //Look for a fractional part. <Book Comment
        if(peek() == '.' && isDigit(peekNext())){ //uses peekNext to confirm ther are digits after .
            //Consume the "." < Book Comment
            advance();
            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start,current))); // .parseDouble returns double primitive. .valueof would return Double object
    }

    

    //used to compare see if following char expands on current eg. >=
    private boolean match(char expected){
        if(isAtEnd()) return false; // no other character
        if(source.charAt(current) != expected) return false; //character doesn't expand on

        current++;
        return true;
    }

    //looks at next char but doesn't consume
    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    //looks to char ahead
    private char peekNext(){
        if(current + 1 >= source.length()) return '\0'; // check that end of source isn't reached
        return source.charAt(current + 1); 
    }

    // determines if c is a letter, used to build identifiers
    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            c == '_';
    }

    //determines if c is alphanumeric, used to build identifiers
    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    

    // tests if character is a number
    private boolean isDigit(char c){
        return c >= '0' && c <= '9'; // does not use Character.isDigit() because it allows other characters
    }
    
    // handles when all characters have been consumed
    private boolean isAtEnd(){
        return current >= source.length();
    }
    // consumes next character in stream and consumes it
    private char advance(){
        return source.charAt(current++); //returns the current char then moves to the next
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

    //handles string literal tokens
    private void string(){
        while(peek() != '"'&&!isAtEnd()){
            if(peek() =='\n') line++;
            advance();
        }
        if(isAtEnd()){
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ". < Book Comment
        advance();
        //Trim the currounding quotes < Book Comment
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    
}

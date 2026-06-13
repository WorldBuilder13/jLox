/*
    This code is copied from the book Crating Intepreter by Robert Nystrom

    Changes to the base code I've made: (changes will be kept on branches of main)
    +TBD

    //TODO:
    + Chapter 6 challenges - implement in branch
        - support for c-style comma operator (challenge 1)
        - support for c-style ?: (conditional or "ternary" operator) (challenge 2)
        - add error productions and handling for binary operators missing left-hand operand (challenge 3)
*/

package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;
import java.util.ArrayList;
import java.util.List;


//handles errors with a "panic mode"
class Parser {
    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0;

    /*
    Grammar - recursive descent, parses top down with operation precedence being bottom-up
    program     ->  declaration*EOF;
    declaration ->  varDecl | statement;
    varDecl     ->  "var" IDENTIFIER ("=" expression)?";";
    statement   ->  exprStmt | ifStmt | printStmt | block;
    exprStmt    ->  expression ";";
    ifStmt      -> "if" "(" expression ")" statement ("else" statement)?;
    printStmt   ->  "print" expression ";";
    block       ->  "{" declaration* "}";
    expression  ->  equality | assignment;
    assignment  ->  IDENTIFIER "=" assignment | equality ;
    equality    ->  comparison (("!="|"==")comaprison)*;
    comparison  ->  term ((">" | ">=" | "<" | "<=") term)*;
    term        ->  factor (("-" | "+")factor)*;
    factor      ->  unary (("/" | "*") unary)*;
    unary       ->  ("!"|"-") unary | primary;
    primary     ->  NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER;
    */

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declaration());
        }

        return statements;
    }

    //methods for each production of grammar
    //expression  ->  equality;
    private Expr expression(){
        return assignment();
    }

    //declaration ->  varDecl | statement;
    private Stmt declaration(){
        try {
            if(match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    //equality    ->  comparison (("!="|"==")comaprison)*;
    // left-associative nested tree
    private Expr equality(){
        Expr expr = comparison(); //consumes everything until != or ==

        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous(); //match stepped current forward, this assigns the != or ==
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Stmt statement(){
        if(match(IF)) return ifStatement();
        if(match(PRINT)) return printStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block()); 

        return expressionStatement();
    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(ELSE)){
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement(){
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expect variabl3e name.");
        Expr initializer = null;

        if(match(EQUAL)){
            initializer = expression();
        }

        consume(SEMICOLON, "expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    // assignment  ->  IDENTIFIER "=" assignment | equality ;
    private Expr assignment(){
        Expr expr = equality();

        if(match(EQUAL)){
            Token equals = previous();
            Expr value = assignment();
            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    // comparison  ->  term ((">" | ">=" | "<" | "<=") term)*;
    private Expr comparison(){
        Expr expr = term();

        //handles ()*
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous(); // assigns: >, >=, <, or <=
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // term ->  factor (("-" | "+")factor)*;
    private Expr term(){
        Expr expr = factor();

        while(match(MINUS, PLUS)){
            Token operator = previous(); // assigns + or -
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor   ->  unary (("/" | "*") unary)*;
    private Expr factor(){
        Expr expr = unary();

        while(match(SLASH,STAR)){
            Token operator = previous(); // assigns / or *
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary ->  ("!"|"-") unary | primary;
    // first checks if current is ! or -, it recursively calls itself if so
    // if there isn't ! or -, it returns primary
    private Expr unary(){
        if(match(BANG, MINUS)){
            Token operator = previous(); // assigns + or -
            Expr right = unary(); // calls unary on remainder of tokens
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    //  primary ->  NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER;
    private Expr primary(){
        //returns for each terminal
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        //returns number or string literals
        if(match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression."); // looks for closing ")"
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");

    }


    // helper methods
    // used to resolve *, consumes token
    private boolean match(TokenType... types){
        for(TokenType type:types){
            if(check(type)){
                advance();
                return true;
            }
        }

        return false;
    }

    //looks for closing token type, prints message if not found
    private Token consume(TokenType type,String message){
        if(check(type)) return advance();
        
        throw error(peek(), message);
    }

    // checks next token without consuming
    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    // returns current token and moves to the next token
    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    // returns if reached end of tokens
    private boolean isAtEnd(){
        return peek().type == EOF;
    }

    // looks at current token without consuming
    private Token peek(){
        return tokens.get(current);
    }

    // returns the token before previous
    private Token previous(){
        return tokens.get(current - 1);
    }

    //error handling
    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    //handles returning to processing after an error
    private void synchronize(){
        advance();

        //advances forward until after a semicolon or current is CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, or RETURN
        while(!isAtEnd()){
            if (previous().type == SEMICOLON) return;

            switch(peek().type){
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}

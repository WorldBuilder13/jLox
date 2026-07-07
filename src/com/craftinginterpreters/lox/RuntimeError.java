/*
    This code is copied from the book Crating Intepreter by Robert Nystrom

    Changes to the base code I've made: (changes will be kept on branches of main)
    +TBD
*/

package com.craftinginterpreters.lox;

class RuntimeError extends RuntimeException{
    final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}

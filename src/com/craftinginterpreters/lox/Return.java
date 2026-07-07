/*
    This code is copied from the book Crating Intepreter by Robert Nystrom

    Changes to the base code I've made: (changes will be kept on branches of main)
    +TBD
*/

package com.craftinginterpreters.lox;

class Return extends RuntimeException{
    final Object value;

    Return(Object value){
        super(null, null, false, false);
        this.value = value;
    }
}

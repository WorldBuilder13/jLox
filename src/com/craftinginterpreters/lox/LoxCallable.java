/*
    This code is copied from the book Crating Intepreter by Robert Nystrom

    Changes to the base code I've made: (changes will be kept on branches of main)
    +TBD
*/

package com.craftinginterpreters.lox;

import java.util.List;

interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}

/*
    This code is copied from the book Crating Intepreter by Robert Nystrom

    Changes to the base code I've made: (changes will be kept on branches of main)
    +TBD
*/

package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter(); //static so global variables will carry through REPL session
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if(args.length > 1){
            System.out.println("Usage:jlox[script]");
            System.exit(64); //From C standard - 64 is wrong number of arguments error
        }
        else if(args.length == 1){
            runFile(args[0]);
        }
        else{
            runPrompt(); //runs if there are no arguments
        }
    }

    // takes a file path, reads all bytes into array, converts array to one string, and finally calls run on string
    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));     // puts all of the binary of the file into an array by bytes
        run(new String(bytes, Charset.defaultCharset()));       // converts bytes array to a string and passes to run()
        //Indicate an error in the exit code. < Book comment
        if(hadError) System.exit(65);
        if(hadRuntimeError) System.exit(70);
    }

    //reads a line from the user and then calls run on it
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;){
            System.out.print(">");
            String line = reader.readLine();
            if(line == null) break;
            run(line);
            hadError = false; //resets flag if previous loop failed
        }
    }

    // takes a string, scanns it for tokens, parses the tokens, and finally interprets
    private static void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        //Stop if there ws a syntax error.
        if(hadError) return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message){ // TODO: add public access modifier? not presetn in book
        report(line, "", message);
    }

    private static void report(int line, String where, String message){
        System.err.println("[line" + line + "]Error" + where + ": " + message);
        hadError = true;
    }

    //used in Parser
    static void error(Token token, String message){
        if(token.type == TokenType.EOF){
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error){
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }


}

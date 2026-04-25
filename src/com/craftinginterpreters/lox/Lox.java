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
    static boolean hadError = false;

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

    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        //Indicate an error in the exit code. < Book comment
        if(hadError) System.exit(65);
    }

    //reads a line from the user (file address) and then calls run on it
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

    private static void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        //For now, just print the tokens < Comment from book
        for(Token token: tokens){
            System.out.println(token);
        }
    }

    static void error(int line, String message){ // TODO: add public access modifier? not presetn in book
        report(line, "", message);
    }

    private static void report(int line, String where, String message){
        System.err.println("[line" + line + "]Error" + where + ": " + message);
        hadError = true;
    }


}

/*
    This code is copied from the book Crating Intepreter by Robert Nystrom

    Changes to the base code I've made: (changes will be kept on branches of main)
    +TBD

    This tool generates the classes for Expr, Visitor and their derived classes.
*/

package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException{
        if (args.length !=1){
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64); //From C standard - 64 is wrong number of arguments error
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Assign : Token name, Expr value",
            "Binary : Expr left, Token operator, Expr right",
            "Call: Expr callee, Token paren, List<Expr> arguments", // paren tracks closing paren for location in error messages
            "Grouping : Expr expression",
            "Literal : Object value",
            "Logical : Expr left, Token operator, Expr right",
            "Unary : Token operator, Expr right",
            "Variable : Token name"
        ));
        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block : List<Stmt> statements",
           "Expression : Expr expression",
           "Function: Token name, List<Token> params, List<Stmt> body",
           "If: Expr condition, Stmt thenBranch," + " Stmt elseBranch",
           "Print : Expr expression",
           "Return : Token keyword, Expr value",
           "Var: Token name, Expr initializer",
           "While: Expr condition, Stmt body"
        ));
    }

    // opens writer and prints head of file. 
    // calls defineVisitor to print methods and finer details
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException{
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        // not original to book, added to put comment at top of generate file
        writer.println("/*");
        writer.println("This code is copied from the book Crating Intepreter by Robert Nystrom");
        writer.println();
        writer.println("Changes to the base code I've made: (changes will be kept on branches of main)");
        writer.println("+TBD");
        writer.println("*/");

        // back to original code 
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types); // calls visitor to handle methods for classes

        // The AST classes < Book Comment
        for (String type: types){
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }


        //the base accept() method. < Book Comment
        writer.println();
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);");
        writer.println("}");
        writer.close();
    }

    // prints visitor class interface then derived classes for each type
    private static void defineVisitor( PrintWriter writer, String baseName, List<String> types){
        writer.println("\tinterface Visitor<R> {");

        for (String type: types){
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("\t}");
        writer.println();
    }

    // prints prints class for each type
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList){
        writer.println("\tstatic class " + className + " extends " + baseName + "{");

        //store paremeters in fields < Book Comment
        String[] fields = fieldList.split(", ");

        //fields < Book Comment
        for (String field:fields){
            writer.println("\t\tfinal " + field + ";");
        }
        writer.println();

        //constructor < Book Comment
        writer.println("\t\t" + className + "(" + fieldList + ") {");
        for (String field: fields){
            String name = field.split(" ")[1];
            writer.println("\t\t\tthis." + name + " = " + name + ";");
        }

        writer.println("\t\t}");

        //Visitor pattern < Book Comment
        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\t<R> R accept(Visitor<R> visitor){");
        writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
        writer.println("\t\t}");

        

        writer.println("\t}");
        writer.println();
    }
}

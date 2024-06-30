# Description
The compiler translates syntactically and semantically correct MicroJava programs into MicroJava bytecode, which can be executed on the MicroJava Virtual Machine (VM). The project was developed as part of the Compiler Construction 1 course at the Faculty of Electrical Engineering, Belgrade.

## Project Structure
The compiler comprises four main components:
1. Lexical Analysis
2. Syntax Analysis
3. Semantic Analysis
4. Code Generation

## Features
Lexical Analysis:
* Implements a lexical analyzer (scanner) using JFlex.
* Recognizes language lexemes and returns a set of tokens for further processing.
* Detects and handles lexical errors, providing detailed error messages.

Syntax Analysis:
* Utilizes an LALR(1) parser generator (AST-CUP) to build the abstract syntax tree (AST).
* Supports error recovery during parsing, allowing continued analysis after encountering syntax errors.

Semantic Analysis:
* Performs semantic checks on the AST to ensure code correctness.
* Reports semantic errors with detailed messages.

Code Generation:
* Translates the AST into executable bytecode for the MicroJava VM.
* Ensures generated code is optimized and adheres to the MicroJava execution environment.

## Usage
* Lexical Analysis: Run the JFlex tool on the mjlexer.flex file to generate the lexer.
* Syntax Analysis: Use the AST-CUP tool on the mjparser.cup file to generate the parser.
* Semantic Analysis and Code Generation: Execute the compiler to translate MicroJava source code into bytecode.

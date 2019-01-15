# Kotlin ANTLR grammar converter

## Description
The tool converts Kotlin grammar in ANTLR format to text file or XML file for Kotlin website.

The project contains two run configurations for generate XML file and text file.

## Input arguments

- `-l`/`--lexerFile` — path to ANTLR 4 lexer grammar file (.g4),
- `-p`/`--parserFile` — path to ANTLR 4 parser grammar file (.g4),
- `--text`/`--xml` — target file format,
- `-o`/`--output` — path to output file.

## Run

Before run it's nessasary to set working directory as `grammar` (the working directory has already been set in the proposed configurations).

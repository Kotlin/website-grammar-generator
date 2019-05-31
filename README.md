# Kotlin website grammar generator

## Description

The tool converts Kotlin grammar in ANTLR format to text file or XML file for the Kotlin website.

Source grammar files are located in the [Kotlin specification repository](https://github.com/Kotlin/kotlin-spec/tree/master/grammar/src/main/antlr).

Generated grammar is used on the Kotlin website: https://kotlinlang.org/docs/reference/grammar.html

## Input arguments

- `--xml`/`--text` — target file format,
- `-o`/`--output` — path to output file (`grammar.xml` or `grammar.txt`),
- `-g`/`--grammarFilesPath` — path to folder with grammar files (`KotlinLexer.g4`, `KotlinParser.g4` and `UnicodeClasses.g4`).

## Run

The tool can be run using gradle (`./gradlew run`) or IDE (run `main` method with arguments).

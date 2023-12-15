# Kotlin website grammar generator

[![JetBrains team project](https://jb.gg/badges/team.svg)](https://github.com/JetBrains#jetbrains-on-github)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

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

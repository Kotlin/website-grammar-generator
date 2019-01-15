# Kotlin ANTLR grammar converter

## Description
The tool converts Kotlin grammar in ANTLR format to text file or XML file for Kotlin website.

The project contains two run configurations for generate XML file and text file.

Grammar files are taken from [Kotlin spec repo](https://github.com/JetBrains/kotlin-spec/tree/spec-rework/src/grammar).

## Input arguments

- `--text`/`--xml` — target file format,
- `-o`/`--output` — path to output file.

## Run

Before run it's nessasary to set working directory as `grammar` (the working directory has already been set in the proposed configurations).

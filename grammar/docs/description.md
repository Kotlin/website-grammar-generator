## Description

### Notation

The notation used on this page corresponds to the ANTLR 4 notation with a few exceptions for convenience of reading:
- omitted lexer rule actions and commands,
- omitted lexical modes.

Short description:
- operator `|` denotes _alternative_,
- operator `*` denotes _iteration_ (zero or more),
- operator `+` denotes _iteration_ (one or more),
- operator `?` denotes _option_ (zero or one),
- operator `..` denotes _range_ (from left to right),
- operator `~` denotes _negation_.

### Grammar source files

Kotlin grammar source files (in ANTLR format) are in [Kotlin specification repository](https://github.com/JetBrains/kotlin-spec/tree/spec-rework){:target="_blank"}:
- **[KotlinLexer.g4](https://github.com/JetBrains/kotlin-spec/blob/spec-rework/src/grammar/KotlinLexer.g4){:target="_blank"}** describes [lexical structure](#lexical-structure);
- **[UnicodeClasses.g4](https://github.com/JetBrains/kotlin-spec/blob/spec-rework/src/grammar/UnicodeClasses.g4){:target="_blank"}** describes the characters that can be used in identifiers (these rules are omitted on this page for convenience of reading);
- **[KotlinParser.g4](https://github.com/JetBrains/kotlin-spec/blob/spec-rework/src/grammar/KotlinParser.g4){:target="_blank"}** describes [syntax](#syntax).

The grammar on this page corresponds to the grammar files above.

### Symbols and naming

_Terminal symbol_ names start with an uppercase letter, e.g. [Identifier](#Identifier).<br>
_Non-terminal symbol_ names start with a lowercase letter, e.g. [kotlinFile](#kotlinFile).<br>

Symbol definitions may be prepended with _attributes_:
- `start` attribute denotes a start symbol (see [kotlinFile](#kotlinFile) and [script](#script)),
- `helper` attribute denotes a lexer fragment rule (used only inside other terminal symbols).

Also for convenience of reading some simplifications are made:
- lexer rules consisting of one string literal element is inlined to the place of use,
- new line tokens are excluded (new lines are not allowed in some places, see source grammar files for details).

### Scope

The grammar corresponds to the latest stable version of the Kotlin compiler excluding lexer and parser rules for experimental features that are disabled by default.
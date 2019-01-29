package org.jetbrains.kotlin.grammargenerator

import org.antlr.v4.tool.Grammar
import org.antlr.v4.tool.LexerGrammar
import org.antlr.v4.tool.Rule
import org.jetbrains.kotlin.grammargenerator.generators.*
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor
import java.io.File


enum class ConvertType { TEXT, XML }

object Runner {
    private lateinit var lexerGrammarText: String
    private lateinit var parserGrammarText: String

    private const val KOTLIN_SPEC_LEXER_GRAMMAR_FILENAME = "KotlinLexer.g4"
    private const val KOTLIN_SPEC_PARSER_GRAMMAR_FILENAME = "KotlinParser.g4"

    private fun getInstanceGenerator(convertType: ConvertType, lexerRules: Map<String, Rule>, parserRules: Map<String, Rule>) =
            when (convertType) {
                ConvertType.TEXT -> TextGenerator(lexerRules, parserRules)
                ConvertType.XML -> XmlGenerator(lexerRules, parserRules, lexerGrammarText.lines(), parserGrammarText.lines())
            } as Generator<Any, Any>

    private fun getGrammarSetFromGrammarFiles(grammarFilesPath: String): Pair<LexerGrammar, Grammar> {
        lexerGrammarText = File("$grammarFilesPath$KOTLIN_SPEC_LEXER_GRAMMAR_FILENAME").readText()
        parserGrammarText = File("$grammarFilesPath$KOTLIN_SPEC_PARSER_GRAMMAR_FILENAME").readText()

        return Pair(LexerGrammar(lexerGrammarText), Grammar(parserGrammarText))
    }

    fun run(convertType: ConvertType, outputFile: String, grammarFilesPath: String) {
        val (lexerGrammar, parserGrammar) = getGrammarSetFromGrammarFiles(grammarFilesPath)
        val generator = getInstanceGenerator(convertType, lexerGrammar.rules, parserGrammar.rules)
        val visitor = GrammarVisitor(generator)
        val result = generator.run { buffer ->
            buffer.generateNotationDescription()
            buffer.generateParserRules(visitor)
            buffer.generateLexerRules(visitor)
        }

        File(outputFile).writeText(result)
    }
}

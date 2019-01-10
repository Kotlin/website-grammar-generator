package org.jetbrains.kotlin.grammargenerator

import org.antlr.v4.tool.Grammar
import java.io.File
import com.xenomachina.argparser.ArgParser
import org.antlr.v4.tool.LexerGrammar
import org.jetbrains.kotlin.grammargenerator.generators.TextGenerator
import org.jetbrains.kotlin.grammargenerator.generators.XmlGenerator
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor

enum class ConvertType { TEXT, XML }
enum class GrammarType { PARER, LEXER }

fun main(args: Array<String>) {
    val parser = ArgParser(args)
    val grammarFile by parser.storing("-f", "--file", help="path to ANTLR 4 grammar file (.g4)")
    val outputFile by parser.storing("-o", "--output", help="path to converted file")
    val convertType by parser.mapping("--text" to ConvertType.TEXT, "--xml" to ConvertType.XML, help = "convert type (--text or --xml)")
    val grammarType by parser.mapping("--parser" to GrammarType.PARER, "--lexer" to GrammarType.LEXER, help = "grammar type (--parser or --lexer)")

    val grammarText = File(grammarFile).readText()
    val grammar = when (grammarType) {
        GrammarType.PARER -> Grammar(grammarText)
        GrammarType.LEXER -> LexerGrammar(grammarText)
    }

    val rules = grammar.rules.filter { it.value.mode != "Inside" && !it.value.isFragment }.values
    val fragments = grammar.rules.filter { it.value.mode != "Inside" && it.value.isFragment }.values

    val generator = when (convertType) {
        ConvertType.TEXT -> TextGenerator()
        ConvertType.XML -> XmlGenerator()
    }
    val visitor = GrammarVisitor(generator)

    val result = generator.run(rules.map { it.ast.visit(visitor) }, fragments.map { it.ast.visit(visitor) })

    File(outputFile).writeText(result)
}

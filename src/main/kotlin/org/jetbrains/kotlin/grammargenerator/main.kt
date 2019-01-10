package org.jetbrains.kotlin.grammargenerator

import java.io.File
import com.xenomachina.argparser.ArgParser
import org.antlr.v4.tool.Grammar
import org.antlr.v4.tool.LexerGrammar
import org.jetbrains.kotlin.grammargenerator.generators.TextGenerator
import org.jetbrains.kotlin.grammargenerator.generators.XmlGenerator
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor

enum class ConvertType { TEXT, XML }

fun main(args: Array<String>) {
    val parser = ArgParser(args)
    val lexerGrammarFile by parser.storing("-l", "--lexerFile", help="path to ANTLR 4 lexer grammar file (.g4)")
    val parserGrammarFile by parser.storing("-p", "--parserFile", help="path to ANTLR 4 parser grammar file (.g4)")
    val outputFile by parser.storing("-o", "--output", help="path to converted file")
    val convertType by parser.mapping("--text" to ConvertType.TEXT, "--xml" to ConvertType.XML, help = "convert type (--text or --xml)")

    val lexerGrammar = LexerGrammar(File(lexerGrammarFile).readText())
    val parserGrammar = Grammar(File(parserGrammarFile).readText())

    val parserRules = parserGrammar.rules.values
    val lexerRules = lexerGrammar.rules.filter { !it.value.isFragment }.values

    val lexerHelpers = lexerGrammar.rules.filter { it.value.isFragment }.values

    val lexerGenerator = when (convertType) {
        ConvertType.TEXT -> TextGenerator()
        ConvertType.XML -> XmlGenerator()
    }
    val parserGenerator = when (convertType) {
        ConvertType.TEXT -> TextGenerator(lexerGrammar.rules.filter { !it.value.isFragment })
        ConvertType.XML -> XmlGenerator(lexerGrammar.rules.filter { !it.value.isFragment })
    }
    val lexerVisitor = GrammarVisitor(lexerGenerator)
    val parserVisitor = GrammarVisitor(parserGenerator)

    val parserResult = parserGenerator.run(parserRules.map { it.ast.visit(parserVisitor) })
    val lexerResult = lexerGenerator.run(lexerRules.filter {
        !parserGenerator.usedLexerRules.contains(it.name) && it.mode != "Inside" && !it.name.startsWith("UNICODE_CLASS")
    }.map {it.ast.visit(lexerVisitor) }, lexerHelpers.map { it.ast.visit(lexerVisitor) })

    File(outputFile).writeText(parserResult + lexerResult)
}

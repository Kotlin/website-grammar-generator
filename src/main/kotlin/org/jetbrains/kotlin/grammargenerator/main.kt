package org.jetbrains.kotlin.grammargenerator

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

fun main(args: Array<String>) {
    val parser = ArgParser(args)
    val convertType by parser.mapping("--text" to ConvertType.TEXT, "--xml" to ConvertType.XML, help = "convert type (--text or --xml)").default(ConvertType.XML)
    val outputFile by parser.storing("-o", "--output", help = "path to converted file").default("grammar.xml")
    val grammarFilesPath by parser.storing("-g", "--grammarFilesPath", help = "path to folder with grammar files (KotlinLexer.g4 and KotlinParser.g4)").default("")

    Runner.run(convertType, outputFile, grammarFilesPath)
}

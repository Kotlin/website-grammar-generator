package org.jetbrains.kotlin.grammargenerator

import com.xenomachina.argparser.ArgParser

fun main(args: Array<String>) {
    val parser = ArgParser(args)
    val convertType by parser.mapping("--text" to ConvertType.TEXT, "--xml" to ConvertType.XML, help = "convert type (--text or --xml)")
    val outputFile by parser.storing("-o", "--output", help = "path to converted file")

    Runner.run(convertType, outputFile)
}

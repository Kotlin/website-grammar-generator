package org.jetbrains.kotlin.grammargenerator

import org.antlr.v4.tool.Grammar
import org.antlr.v4.tool.LexerGrammar
import org.antlr.v4.tool.Rule
import org.jetbrains.kotlin.grammargenerator.generators.*
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHubBuilder
import java.io.File
import java.util.stream.Collectors
import java.io.BufferedReader
import java.io.InputStreamReader


enum class ConvertType { TEXT, XML }

object Runner {
    private lateinit var lexerGrammarText: String
    private lateinit var parserGrammarText: String

    private val githubApi = GitHubBuilder().build()

    private const val KOTLIN_SPEC_REPO = "JetBrains/kotlin-spec"
    private const val KOTLIN_SPEC_BRANCH = "spec-rework"
    private const val KOTLIN_SPEC_LEXER_GRAMMAR_PATH = "src/grammar/KotlinLexer.g4"
    private const val KOTLIN_SPEC_UNICODE_CLASSES_FILENAME = "UnicodeClasses.g4"
    private const val KOTLIN_SPEC_UNICODE_CLASSES_PATH = "src/grammar/$KOTLIN_SPEC_UNICODE_CLASSES_FILENAME"
    private const val KOTLIN_SPEC_PARSER_GRAMMAR_PATH = "src/grammar/KotlinParser.g4"

    private fun getInstanceGenerator(convertType: ConvertType, lexerRules: Map<String, Rule>, parserRules: Map<String, Rule>) =
            when (convertType) {
                ConvertType.TEXT -> TextGenerator(lexerRules, parserRules)
                ConvertType.XML -> XmlGenerator(lexerRules, parserRules, lexerGrammarText.lines(), parserGrammarText.lines())
            } as Generator<Any, Any>

    private fun getRepoFileContent(repo: GHRepository, path: String): String {
        val fileContentStream = repo.getFileContent(path, KOTLIN_SPEC_BRANCH)

        return BufferedReader(
                InputStreamReader(fileContentStream.read())
        ).lines().collect(Collectors.joining(System.lineSeparator()))
    }

    private fun getGrammarSet(): Pair<LexerGrammar, Grammar> {
        val repo = githubApi.getRepository(KOTLIN_SPEC_REPO)

        lexerGrammarText = getRepoFileContent(repo, KOTLIN_SPEC_LEXER_GRAMMAR_PATH)
        parserGrammarText = getRepoFileContent(repo, KOTLIN_SPEC_PARSER_GRAMMAR_PATH)

        File(KOTLIN_SPEC_UNICODE_CLASSES_FILENAME).writeText(getRepoFileContent(repo, KOTLIN_SPEC_UNICODE_CLASSES_PATH))

        return Pair(LexerGrammar(lexerGrammarText), Grammar(parserGrammarText))
    }

    fun run(convertType: ConvertType, outputFile: String) {
        val (lexerGrammar, parserGrammar) = getGrammarSet()
        val generator = getInstanceGenerator(convertType, lexerGrammar.rules, parserGrammar.rules)
        val visitor = GrammarVisitor(generator)
        val result = generator.run { buffer ->
            buffer.generateNotationDescription()
            buffer.generateParserRules(visitor)
            buffer.generateLexerRules(visitor)
        }

        File(outputFile).writeText(result)
        File(KOTLIN_SPEC_UNICODE_CLASSES_FILENAME).delete()
    }
}

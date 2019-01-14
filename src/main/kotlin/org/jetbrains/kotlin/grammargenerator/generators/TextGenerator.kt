package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.GrammarAST
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.LENGTH_FOR_RULE_SPLIT
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor

private typealias ITextGenerator = Generator<String, StringBuilder>

class TextGenerator(override val lexerRules: Map<String, Rule>, override val parserRules: Map<String, Rule>) : ITextGenerator {
    override val usedLexerRules = mutableSetOf<String>()
    override lateinit var currentMode: GeneratorType

    companion object {
        val ls: String = System.lineSeparator()
    }

    private fun addGreedyMarker(isGreedy: Boolean) = if (isGreedy) "" else "?"

    private fun joinThroughLength(nodes: List<String>) = nodes.reduce { acc, s ->
        acc + (if (acc.substringAfterLast(ls).length + s.length < LENGTH_FOR_RULE_SPLIT) "" else ls) + " $s"
    }

    private fun groupUsingPipe(nodes: List<Any>, groupingBracketsNeed: Boolean): String {
        val leftSeparator = if (groupingBracketsNeed) " " else ls
        val groupedNodes = nodes.joinToString("$leftSeparator| ") { it as String }

        return if (groupingBracketsNeed) "($groupedNodes)" else groupedNodes
    }

    private fun getVisitedRules(rules: Map<String, Rule>, visitor: GrammarVisitor) =
            rules.entries.associate { (ruleName, rule) ->
                ruleName to Pair(rule, rule.ast.visit(visitor) as String)
            }

    override fun optional(child: String, isGreedy: Boolean) = "$child?" + addGreedyMarker(isGreedy)

    override fun plus(child: String, isGreedy: Boolean) = "$child+" + addGreedyMarker(isGreedy)

    override fun star(child: String, isGreedy: Boolean) = "$child*" + addGreedyMarker(isGreedy)

    override fun not(child: String) = "~$child"

    override fun range(childLeft: String, childRight: String) = "$childLeft..$childRight"

    override fun rule(children: List<String>, ruleName: String, lineNumber: Int) =
            "$ruleName$ls: " + children.joinToString("") + "$ls;" + ls.repeat(2)

    override fun block(groupingBracketsNeed: Boolean, children: List<String>) = groupUsingPipe(children, groupingBracketsNeed)

    override fun set(groupingBracketsNeed: Boolean, children: List<String>) = groupUsingPipe(children, groupingBracketsNeed)

    override fun alt(children: List<String>) = joinThroughLength(children)

    override fun root() = ""

    override fun pred() = ""

    override fun charsSet(node: GrammarAST) = node.text

    override fun ruleRef(node: RuleRefAST): String = node.text

    override fun terminal(node: TerminalAST): String = getLexerRule(node) ?: node.text

    override fun run(builder: ITextGenerator.(StringBuilder) -> Unit) = StringBuilder().also { builder(it) }.toString()

    override fun StringBuilder.generateNotationDescription() {}

    override fun StringBuilder.generateLexerRules(visitor: GrammarVisitor) {
        currentMode = GeneratorType.LEXER

        val lexerRulesVisited = filterLexerRules(getVisitedRules(lexerRules, visitor), usedLexerRules)

        this.append(
                lexerRulesVisited.map { (_, ruleInfo) ->
                    val (rule, elementText) = ruleInfo

                    (if (rule.isFragment) "[helper] " else "") + elementText
                }.joinToString("")
        )
    }

    override fun StringBuilder.generateParserRules(visitor: GrammarVisitor) {
        currentMode = GeneratorType.PARSER

        val parserRulesVisited = getVisitedRules(parserRules, visitor)

        this.append(
                parserRulesVisited.map { (_, ruleInfo) -> ruleInfo.second }.joinToString("")
        )
    }
}

package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.LENGTH_FOR_RULE_SPLIT
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor

private typealias ITextGenerator = Generator<String, StringBuilder>

class TextGenerator(
        override val lexerRules: Map<String, Rule>,
        override val parserRules: Map<String, Rule>
) : ITextGenerator {
    override val usedLexerRules = mutableSetOf<String>()

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

    override fun ruleRef(node: RuleRefAST): String = node.text

    override fun terminal(node: TerminalAST): String = getLexerRule(node) ?: node.text

    override fun run(builder: ITextGenerator.(StringBuilder) -> Unit) = StringBuilder().also { builder(it) }.toString()

    override fun StringBuilder.generateNotationDescription() {}

    override fun StringBuilder.generateLexerRules(visitor: GrammarVisitor) {
        this.append(
                lexerRules.map { (_, ruleInfo) ->
                    (if (ruleInfo.isFragment) "[helper] " else "") + ruleInfo.ast.visit(visitor)
                }.joinToString("")
        )
    }

    override fun StringBuilder.generateParserRules(visitor: GrammarVisitor) {
        this.append(parserRules.map { (_, rule) -> rule.ast.visit(visitor) }.joinToString(""))
    }
}

package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST

class XmlGenerator(private val lexerRules: Map<String, Rule>? = null) : Generator {
    override val usedLexerRules = mutableSetOf<String>()
    private fun addGreedyMarker(isGreedy: Boolean): Nothing = TODO()
    private fun joinThroughLength(nodes: List<Any>): Nothing = TODO()
    private fun groupUsingPipe(nodes: List<Any>, leftSeparator: String = " "): Nothing = TODO()

    override fun optional(child: Any, isGreedy: Boolean): Nothing = TODO()
    override fun plus(child: Any, isGreedy: Boolean): Nothing = TODO()
    override fun star(child: Any, isGreedy: Boolean): Nothing = TODO()
    override fun not(child: Any): Nothing = TODO()
    override fun range(childLeft: Any, childRight: Any): Nothing = TODO()
    override fun rule(ruleName: String, children: List<Any>): Nothing = TODO()
    override fun block(groupingBracketsNeed: Boolean, children: List<Any>): Nothing = TODO()
    override fun set(groupingBracketsNeed: Boolean, children: List<Any>): Nothing = TODO()
    override fun alt(children: List<Any>): Nothing = TODO()
    override fun root(): Nothing = TODO()
    override fun pred(): Nothing = TODO()
    override fun ruleRef(node: RuleRefAST): Nothing = TODO()
    override fun terminal(node: TerminalAST): Nothing = TODO()

    override fun run(rules: List<Any>, fragments: List<Any>): Nothing = TODO()
    override fun run(rules: List<Any>): Nothing = TODO()
}

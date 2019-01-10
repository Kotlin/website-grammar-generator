package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST

interface Generator {
    fun optional(child: Any, isGreedy: Boolean): Any
    fun plus(child: Any, isGreedy: Boolean): Any
    fun star(child: Any, isGreedy: Boolean): Any
    fun not(child: Any): Any
    fun range(childLeft: Any, childRight: Any): Any
    fun rule(ruleName: String, children: List<Any>): Any
    fun block(groupingBracketsNeed: Boolean, children: List<Any>): Any
    fun set(groupingBracketsNeed: Boolean, children: List<Any>): Any
    fun alt(children: List<Any>): Any
    fun root(): Any
    fun pred(): Any
    fun ruleRef(node: RuleRefAST): Any
    fun terminal(node: TerminalAST): Any
    fun run(rules: List<Any>): String
    fun run(rules: List<Any>, fragments: List<Any>): String
    val usedLexerRules: Set<String>
}

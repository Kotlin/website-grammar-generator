package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.parse.ANTLRParser
import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.GrammarAST
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor

interface Generator<T> {
    val usedLexerRules: MutableSet<String>
    val lexerRules: Map<String, Rule>?

    fun optional(child: T, isGreedy: Boolean): T
    fun plus(child: T, isGreedy: Boolean): T
    fun star(child: T, isGreedy: Boolean): T
    fun not(child: T): T
    fun range(childLeft: T, childRight: T): T
    fun rule(ruleName: String, children: List<T>): T
    fun block(groupingBracketsNeed: Boolean, children: List<T>): T
    fun set(groupingBracketsNeed: Boolean, children: List<T>): T
    fun alt(children: List<T>): T
    fun root(): T
    fun pred(): T
    fun ruleRef(node: RuleRefAST): T
    fun terminal(node: TerminalAST): T
    fun run(visitor: GrammarVisitor, rules: List<Rule>, fragments: List<Rule>): String

    fun getLexerRule(node: TerminalAST): String? {
        if (lexerRules == null)
            return null

        val rule = lexerRules!![node.token.text]

        if (rule == null || !isSimpleLexerRule(rule.ast.childrenAsArray[1]))
            return null

        return rule.tokenRefs.single().also { usedLexerRules.add(rule.name) }
    }

    companion object {
        const val LENGTH_FOR_RULE_SPLIT = 120

        val rootNodes = setOf("kotlinFile", "script")

        private val supportedNodes = setOf(
                ANTLRParser.TOKEN_REF,
                ANTLRParser.LEXER_CHAR_SET,
                ANTLRParser.RULE_REF,
                ANTLRParser.BLOCK,
                ANTLRParser.ALT,
                ANTLRParser.SET,
                ANTLRParser.RULE,
                ANTLRParser.STRING_LITERAL,
                ANTLRParser.RULEMODIFIERS,
                ANTLRParser.POSITIVE_CLOSURE,
                ANTLRParser.CLOSURE,
                ANTLRParser.OPTIONAL
        )

        fun isSimpleLexerRule(rule: GrammarAST): Boolean {
            if (rule.children != null && rule.childrenAsArray.count { supportedNodes.contains(it.type) } > 1)
                return false

            if (rule.children == null || rule.children.size == 0)
                return true

            return isSimpleLexerRule(rule.childrenAsArray[0])
        }
    }
}

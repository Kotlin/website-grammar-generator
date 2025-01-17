package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.parse.ANTLRParser
import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.GrammarAST
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor

interface Generator<T, K> {
    val usedLexerRules: MutableSet<String>
    val lexerRules: Map<String, Rule>
    val parserRules: Map<String, Rule>
    var currentMode: GeneratorType

    fun optional(child: T, isGreedy: Boolean): T
    fun plus(child: T, isGreedy: Boolean): T
    fun star(child: T, isGreedy: Boolean): T
    fun not(child: T): T
    fun range(childLeft: T, childRight: T): T
    fun rule(children: List<T>, ruleName: String, lineNumber: Int): T
    fun block(groupingBracketsNeed: Boolean, children: List<T>): T
    fun set(groupingBracketsNeed: Boolean, children: List<T>): T
    fun alt(groupingBracketsNeed: Boolean, children: List<T>): T
    fun root(): T
    fun pred(): T
    fun ruleRef(node: RuleRefAST): T
    fun charsSet(node: GrammarAST): T
    fun terminal(node: TerminalAST): T
    fun run(builder: Generator<T, K>.(K) -> Unit): String

    fun K.generateLexerRules(visitor: GrammarVisitor)
    fun K.generateParserRules(visitor: GrammarVisitor)
    fun K.generateNotationDescription()

    fun getLexerRule(node: TerminalAST): String? {
        val rule = lexerRules[node.token.text]

        if (rule == null || !isSimpleLexerRule(rule.ast.childrenAsArray[1]))
            return null

        return rule.tokenRefs.singleOrNull()?.also { usedLexerRules.add(rule.name) }
    }

    fun filterLexerRules(rules: Map<String, Rule>, usedRules: Set<String>) =
            rules.filter { (ruleName, rule) ->
                rule.mode != "Inside" && !ruleName.startsWith("UNICODE_CLASS")
            }

    companion object {
        const val LENGTH_FOR_RULE_SPLIT = 80

        val rootNodes = setOf("kotlinFile", "script")
        val excludedRules = setOf("NL")

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
                return rule.type == ANTLRParser.STRING_LITERAL

            return isSimpleLexerRule(rule.childrenAsArray[0])
        }
    }
}

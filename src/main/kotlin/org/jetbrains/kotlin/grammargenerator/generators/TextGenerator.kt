package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.parse.ANTLRParser
import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.GrammarAST
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST

class TextGenerator(private val lexerRules: Map<String, Rule>? = null) : Generator {
    override val usedLexerRules = mutableSetOf<String>()
    companion object {
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

        const val LENGTH_FOR_RULE_SPLIT = 120
        val ls = System.lineSeparator()
        val stopTerminals = setOf("NL")

        fun isSimpleLexerRule(rule: GrammarAST): Boolean {
//            if (rule.children != null && rule.childrenAsArray.count { println(it.altLabel); println(it.type);it.type != 1 } > 1)
                if (rule.children != null && rule.childrenAsArray.count { supportedNodes.contains(it.type) } > 1)
                return false

            if (rule.children == null || rule.children.size == 0)
                return true

            return isSimpleLexerRule(rule.childrenAsArray[0])
        }
    }

    fun getLexerRule(node: TerminalAST): String? {
        if (stopTerminals.contains(node.token.text) || lexerRules == null)
            return null

        val rule = lexerRules[node.token.text]

        if (rule == null || !isSimpleLexerRule(rule.ast.childrenAsArray[1]))
            return null

        return rule.tokenRefs.single().also { usedLexerRules.add(rule.name) }
    }

    private fun addGreedyMarker(isGreedy: Boolean) = if (isGreedy) "" else "?"

    private fun joinThroughLength(nodes: List<Any>) =
            nodes.run { joinToString(if (sumBy { (it as String).length } > LENGTH_FOR_RULE_SPLIT) ls else " ") }

    private fun groupUsingPipe(nodes: List<Any>, leftSeparator: String = " ") =
            nodes.joinToString("$leftSeparator| ") { it as String }

    override fun optional(child: Any, isGreedy: Boolean): Any {
        return (child as String) + "?" + addGreedyMarker(isGreedy)
    }
    override fun plus(child: Any, isGreedy: Boolean): Any {
        return (child as String) + "+" + addGreedyMarker(isGreedy)
    }
    override fun star(child: Any, isGreedy: Boolean): Any {
        return (child as String) + "*" + addGreedyMarker(isGreedy)
    }
    override fun not(child: Any): Any {
        return "~" + (child as String)
    }
    override fun range(childLeft: Any, childRight: Any): Any {
        return (childLeft as String) + "" + (childRight as String)
    }
    override fun rule(ruleName: String, children: List<Any>): Any {
        return ruleName + ls + ": " + children.joinToString("") + ls + ";" + ls.repeat(2)
    }

    override fun block(groupingBracketsNeed: Boolean, children: List<Any>): Any {
        val groupedNodes = groupUsingPipe(children, leftSeparator = if (groupingBracketsNeed) " " else ls)

        return if (groupingBracketsNeed) "($groupedNodes)" else groupedNodes
    }

    override fun set(groupingBracketsNeed: Boolean, children: List<Any>): Any {
        val groupedNodes = groupUsingPipe(children, leftSeparator = if (groupingBracketsNeed) " " else ls)

        return if (groupingBracketsNeed) "($groupedNodes)" else groupedNodes
    }

    override fun alt(children: List<Any>): Any {
        return joinThroughLength(children)
    }

    override fun root() = ""
    override fun pred() = ""

    override fun ruleRef(node: RuleRefAST) = node.text

    override fun terminal(node: TerminalAST) = getLexerRule(node) ?: node.text

    override fun run(rules: List<Any>, fragments: List<Any>): String {
        val fragmentsGenerated =
                if (fragments.isNotEmpty()) "[helper] " + fragments.joinToString("[helper] ") { it as String } else ""

        return run(rules) + fragmentsGenerated
    }

    override fun run(rules: List<Any>) = rules.joinToString("") { it as String }
}

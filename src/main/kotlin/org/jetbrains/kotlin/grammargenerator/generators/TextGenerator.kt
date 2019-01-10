package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST

class TextGenerator : Generator {
    companion object {
        const val LENGTH_FOR_RULE_SPLIT = 120
        val ls = System.lineSeparator()
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

    override fun ruleRef(node: RuleRefAST): Any {
        return node.text
    }

    override fun terminal(node: TerminalAST): Any {
        return node.text
    }

    override fun run(rules: List<Any>, fragments: List<Any>): String {
        val rulesGenerated = rules.joinToString("") { it as String }
        val fragmentsGenerated =
                if (fragments.isNotEmpty()) "[helper] " + fragments.joinToString("[helper] ") { it as String } else ""

        return rulesGenerated + fragmentsGenerated
    }
}

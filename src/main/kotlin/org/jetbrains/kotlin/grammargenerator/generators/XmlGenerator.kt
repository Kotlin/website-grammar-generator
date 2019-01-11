package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.LENGTH_FOR_RULE_SPLIT
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.rootNodes
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor
import org.jonnyzzz.kotlin.xml.dsl.XWriter
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

typealias RuleResult = Pair<Int, XWriter.() -> Unit>

class XmlGenerator(override val lexerRules: Map<String, Rule>? = null) : Generator<RuleResult> {
    override val usedLexerRules = mutableSetOf<String>()

    private fun addGreedyMarker(isGreedy: Boolean) = RuleResult(if (isGreedy) 0 else 1) {
        if (!isGreedy) {
            element("symbol") { cdata("?") }
        }
    }

    private fun joinThroughLength(
            elements: List<RuleResult>
    ) = RuleResult(elements.sumBy { (nodeLength, _) -> nodeLength }) {
        var bufferSize = 0
        elements.forEachIndexed { index, (contentLength, buildElement) ->
            if (index != 0) {
                bufferSize += contentLength
                if (bufferSize > LENGTH_FOR_RULE_SPLIT) {
                    element("crlf")
                    bufferSize = 0
                }
                element("whiteSpace")
            }
            buildElement()
        }
    }

    private fun groupUsingPipe(
            elements: List<RuleResult>,
            groupingBracketsNeed: Boolean
    ) = RuleResult(elements.sumBy { (nodeLength, _) -> nodeLength }) {
        if (groupingBracketsNeed) element("symbol") { cdata("(") }

        elements.forEachIndexed { index, (_, buildElement) ->
            if (index != 0) {
                if (groupingBracketsNeed) element("whiteSpace") else element("crlf")
                element("symbol") { cdata("|") }
                element("whiteSpace")
            }
            buildElement()
        }

        if (groupingBracketsNeed) element("symbol") { cdata(")") }
    }

    override fun optional(child: RuleResult, isGreedy: Boolean): RuleResult {
        val (greedyMarkerLength, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, buildChild) = child

        return RuleResult(childContentLength + 1 + greedyMarkerLength) {
            buildChild()
            element("symbol") { cdata("?") }
            addGreedyMarker()
        }
    }

    override fun plus(child: RuleResult, isGreedy: Boolean): RuleResult {
        val (greedyMarkerLength, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, buildChild) = child

        return RuleResult(childContentLength + 1 + greedyMarkerLength) {
            buildChild()
            element("symbol") { cdata("+") }
            addGreedyMarker()
        }
    }

    override fun star(child: RuleResult, isGreedy: Boolean): RuleResult {
        val (greedyMarkerLength, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, buildChild) = child

        return RuleResult(childContentLength + 1 + greedyMarkerLength) {
            buildChild()
            element("symbol") { cdata("*") }
            addGreedyMarker()
        }
    }

    override fun not(child: RuleResult): RuleResult {
        val (childContentLength, buildChild) = child

        return RuleResult(childContentLength + 1) {
            element("symbol") { cdata("~") }
            buildChild()
        }
    }

    override fun range(childLeft: RuleResult, childRight: RuleResult): RuleResult {
        val (childLeftContentLength, buildChildLeft) = childLeft
        val (childRightContentLength, buildChildRight) = childRight

        return RuleResult(childLeftContentLength + childRightContentLength + 2) {
            buildChildLeft()
            element("string") { cdata("..") }
            buildChildRight()
        }
    }

    override fun rule(ruleName: String, children: List<RuleResult>) = RuleResult(children.sumBy { it.first }) {
        if (rootNodes.contains(ruleName)) {
            element("annotation") {
                text("start")
            }
        }
        element("declaration") {
            attribute("name", ruleName)
        }
        element("description") {
            element("whitespace")
            element("whitespace")
            element("symbol") { cdata(":") }
            element("whitespace")
            children.forEach {
                (_, buildElement) -> buildElement()
            }
            element("crlf")
            element("whitespace")
            element("whitespace")
            element("other") { text(";") }
        }
    }

    override fun block(groupingBracketsNeed: Boolean, children: List<RuleResult>) = groupUsingPipe(children, groupingBracketsNeed)

    override fun set(groupingBracketsNeed: Boolean, children: List<RuleResult>) = groupUsingPipe(children, groupingBracketsNeed)

    override fun alt(children: List<RuleResult>) = joinThroughLength(children)

    override fun root() = RuleResult(0) {}

    override fun pred() = RuleResult(0) {}

    override fun ruleRef(node: RuleRefAST) = RuleResult(node.text.length) {
        element("identifier") {
            attribute("name", node.text)
        }
    }

    override fun terminal(node: TerminalAST) = (getLexerRule(node) ?: node.text).let { nodeText ->
        RuleResult(nodeText.length) {
            element("string") { cdata(nodeText) }
        }
    }

    override fun run(visitor: GrammarVisitor, rules: List<Rule>, fragments: List<Rule>): String {
        val xml = jdom("set") {
            element("doc") {
                text("some doc")
            }
            fragments.map { it.ast.visit(visitor) as RuleResult }.forEach { (_, buildElement): RuleResult ->
                element("item") {
                    element("annotation") {
                        text("helper")
                    }
                    buildElement()
                }
            }
            rules.map { it.ast.visit(visitor) as RuleResult }.forEach { (_, buildElement) ->
                element("item") {
                    buildElement()
                }
            }
        }

        return XMLOutputter(Format.getPrettyFormat()).outputString(xml)
    }
}

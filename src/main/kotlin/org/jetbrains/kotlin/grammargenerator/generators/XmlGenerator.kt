package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.runtime.tree.CommonTree
import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.GrammarAST
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.LENGTH_FOR_RULE_SPLIT
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.excludedRules
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.rootNodes
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor
import org.jonnyzzz.kotlin.xml.dsl.XWriter
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom
import java.io.File
import java.util.regex.Pattern

enum class GeneratorType { LEXER, PARSER }

data class ElementRenderResult(
        val contentLength: Int,
        val sectionName: String? = null,
        val buildElement: XWriter.(rule: Rule) -> Unit
)

private typealias IXmlGenerator = Generator<ElementRenderResult, XWriter>

class XmlGenerator(
        override val lexerRules: Map<String, Rule>,
        override val parserRules: Map<String, Rule>,
        private val lexerGrammarFileLines: List<String>,
        private val parserGrammarFileLines: List<String>
) : IXmlGenerator {
    companion object {
        const val SECTION_DECLARATION_OFFSET = 3
        const val DOCS_FOLDER = "grammar/docs"
        private const val UNICODE_CLASSES_GRAMMAR_FILE_PATH = "https://github.com/JetBrains/kotlin-spec/blob/master/grammar/src/main/antlr/UnicodeClasses.g4"

        private val sectionPattern = Pattern.compile("""^// SECTION: (?<section>[ \w]*?)$""")
        private val unicodeClassesToLineInGrammarFileMap = mapOf(
                "LL" to 9,
                "LM" to 613,
                "LO" to 673,
                "LT" to 999,
                "LU" to 1011,
                "ND" to 1605,
                "NL" to 1642
        )
        // ruleName -> numbers of top-level rule element, after which new line is added
        private val rulesWithCustomNewLines = mapOf(
                "propertyDeclaration" to setOf(3, 4, 5, 6, 8),
                "classDeclaration" to setOf(2, 4, 5, 6, 7),
                "functionDeclaration" to setOf(3, 4, 6, 8),
                "companionObject" to setOf(4, 5),
                "getter" to setOf(4, 5),
                "setter" to setOf(6, 7),
                "secondaryConstructor" to setOf(3),
                "forStatement" to setOf(1, 7),
                "anonymousFunction" to setOf(3, 5),
                "ifExpression" to setOf(4)
        )

        private fun getLinkToUnicodeClass(unicodeClass: String) =
                "<a href=\"$UNICODE_CLASSES_GRAMMAR_FILE_PATH#L${unicodeClassesToLineInGrammarFileMap[unicodeClass]}\" target=\"_black\">UNICODE_CLASS_$unicodeClass</a>"

        private fun renderUnicodeClassLink(unicodeClass: String): XWriter.() -> Unit = {
            element("other") {
                cdata(getLinkToUnicodeClass(unicodeClass))
            }
        }
    }

    override val usedLexerRules = mutableSetOf<String>()
    override lateinit var currentMode: GeneratorType

    private val Rule.excluded: Boolean
        get() = usedLexerRules.contains(this.name)

    private var currentRule: String? = null
    private val usagesMap = mutableMapOf<String, Pair<XWriter?, MutableSet<String>>>()
    private val customTerminalRenders = unicodeClassesToLineInGrammarFileMap.entries.associate { (unicodeClass, _) ->
        "UNICODE_CLASS_$unicodeClass" to renderUnicodeClassLink(unicodeClass)
    }

    private fun getVisitedRules(rules: Map<String, Rule>, visitor: GrammarVisitor) =
            rules.entries.associate { (ruleName, rule) ->
                ruleName to Pair(rule, rule.ast.visit(visitor) as ElementRenderResult)
            }

    private fun addGreedyMarker(isGreedy: Boolean) = ElementRenderResult(if (isGreedy) 0 else 1) {
        if (!isGreedy) {
            element("symbol") { cdata("?") }
        }
    }

    private fun removeSubtreeFromExcludedRule(node: CommonTree) {
        if (node.parent == null)
            return

        node.parent.children.remove(node)

        if (node.parent.children?.isEmpty() == true)
            removeSubtreeFromExcludedRule(node.parent)
    }

    private fun excludeRulesRecursive(node: GrammarAST) {
        if (node.children == null)
            return

        node.childrenAsArray.forEachIndexed { index, child ->
            if (excludedRules.contains(child.text)) {
                node.children.removeAt(index)
                removeSubtreeFromExcludedRule(node)
            } else {
                excludeRulesRecursive(child)
            }
        }
    }

    private fun excludeRules(rules: Map<String, Rule>) =
            rules.filterKeys { !excludedRules.contains(it) }.apply {
                forEach { (_, rule) -> excludeRulesRecursive(rule.ast) }
            }

    private fun joinThroughLength(
            elements: List<ElementRenderResult>,
            groupingBracketsNeed: Boolean
    ) = ElementRenderResult(elements.sumBy { (elementTextLength, _) -> elementTextLength + elements.size }) { rule ->
        var bufferSize = 0

        if (groupingBracketsNeed && elements.size > 1)
            element("symbol") { cdata("(") }

        elements.forEachIndexed { index, (elementTextLength, _, buildElement) ->
            if (index != 0) {
                bufferSize += elementTextLength
                if (bufferSize > LENGTH_FOR_RULE_SPLIT && !rulesWithCustomNewLines.contains(rule.name)) {
                    element("crlf")
                    bufferSize = 0
                    addWhitespace(3)
                } else if (rulesWithCustomNewLines.contains(rule.name) && rulesWithCustomNewLines[rule.name]!!.contains(index)) {
                    element("crlf")
                    addWhitespace(3)
                }
                addWhitespace()
            }
            buildElement(rule)
        }

        if (groupingBracketsNeed && elements.size > 1)
            element("symbol") { cdata(")") }
    }

    private fun groupUsingPipe(
            elements: List<ElementRenderResult>,
            groupingBracketsNeed: Boolean
    ) = ElementRenderResult(elements.sumBy { (elementTextLength, _) -> elementTextLength } + elements.size * 3) { rule ->
        if (groupingBracketsNeed && elements.size > 1)
            element("symbol") { cdata("(") }

        elements.forEachIndexed { index, (_, _, buildElement) ->
            if (index != 0) {
                if (groupingBracketsNeed) addWhitespace() else {
                    element("crlf")
                    addWhitespace(2)
                }
                element("symbol") { cdata("|") }
                addWhitespace()
            }
            buildElement(rule)
        }

        if (groupingBracketsNeed && elements.size > 1)
            element("symbol") { cdata(")") }
    }

    private fun getSectionDeclaration(ruleLineNumber: Int): String? {
        val targetGrammarFileLines = if (currentMode == GeneratorType.LEXER) lexerGrammarFileLines else parserGrammarFileLines
        val potentialSectionDeclarationLine = targetGrammarFileLines.getOrNull(ruleLineNumber - SECTION_DECLARATION_OFFSET) ?: return null

        return sectionPattern.matcher(potentialSectionDeclarationLine).let {
            if (it.find()) it.group("section") else null
        }
    }

    private fun runInContextBySectionInfo(
            rootContext: XWriter,
            currentContext: XWriter,
            rule: Rule,
            sectionName: String?,
            builder: XWriter.(Rule) -> Unit
    ) {
        if (sectionName != null) {
            rootContext.element("set") {
                attribute("file-name", "sectionName")
                addDoc(sectionName)
                builder(rule)
            }
        } else builder(currentContext, rule)
    }

    private fun XWriter.addDoc(docName: String) {
        val sectionDocFile = File("$DOCS_FOLDER/$docName.md")

        if (sectionDocFile.exists()) {
            element("doc") {
                cdata(sectionDocFile.readText())
            }
        }
    }

    private fun XWriter.addWhitespace(number: Int = 1) {
        element("string") {
            cdata("&nbsp;".repeat(number))
        }
    }

    private fun XWriter.generateRules(rules: Map<String, Pair<Rule, ElementRenderResult>>) {
        var currentContext = this
        var currentSectionName: String? = null

        rules.forEach { (_, ruleInfo) ->
            val (rule, result) = ruleInfo
            val (_, sectionName, buildElement) = result

            if (sectionName != null && sectionName != currentSectionName)
                currentSectionName = sectionName

            runInContextBySectionInfo(this, currentContext, rule, sectionName) {
                if (this != currentContext)
                    currentContext = this

                if (!rule.excluded) {
                    addDoc("$currentSectionName/${rule.name}")
                    element("item") {
                        if (rule.isFragment) {
                            element("annotation") {
                                text("helper")
                            }
                        }
                        buildElement(rule)
                    }
                }
            }
        }
    }

    private fun arrangeUsages() {
        usagesMap.values.filter { (_, usages) -> usages.isNotEmpty() }.forEach { (xwriter, usages) ->
            xwriter?.element("usages") {
                usages.forEach {
                    element("declaration") { text(it) }
                }
            }
        }
    }

    override fun optional(child: ElementRenderResult, isGreedy: Boolean): ElementRenderResult {
        val (greedyMarkerLength, _, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, _, buildChild) = child

        return ElementRenderResult(childContentLength + 1 + greedyMarkerLength) { rule ->
            buildChild(rule)
            element("symbol") { cdata("?") }
            addGreedyMarker(rule)
        }
    }

    override fun plus(child: ElementRenderResult, isGreedy: Boolean): ElementRenderResult {
        val (greedyMarkerLength, _, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, _, buildChild) = child

        return ElementRenderResult(childContentLength + 1 + greedyMarkerLength) { rule ->
            buildChild(rule)
            element("symbol") { cdata("+") }
            addGreedyMarker(rule)
        }
    }

    override fun star(child: ElementRenderResult, isGreedy: Boolean): ElementRenderResult {
        val (greedyMarkerLength, _, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, _, buildChild) = child

        return ElementRenderResult(childContentLength + 1 + greedyMarkerLength) { rule ->
            buildChild(rule)
            element("symbol") { cdata("*") }
            addGreedyMarker(rule)
        }
    }

    override fun not(child: ElementRenderResult): ElementRenderResult {
        val (childContentLength, _, buildChild) = child

        return ElementRenderResult(childContentLength + 1) { rule ->
            element("symbol") { cdata("~") }
            buildChild(rule)
        }
    }

    override fun range(childLeft: ElementRenderResult, childRight: ElementRenderResult): ElementRenderResult {
        val (childLeftContentLength, _, buildChildLeft) = childLeft
        val (childRightContentLength, _, buildChildRight) = childRight

        return ElementRenderResult(childLeftContentLength + childRightContentLength + 2) { rule ->
            buildChildLeft(rule)
            element("string") { cdata("..") }
            buildChildRight(rule)
        }
    }

    override fun rule(
            children: List<ElementRenderResult>,
            ruleName: String, lineNumber: Int
    ) = ElementRenderResult(children.sumBy { it.contentLength }, getSectionDeclaration(lineNumber)) { rule ->
        currentRule = ruleName

        if (rootNodes.contains(ruleName)) {
            element("annotation") {
                text("start")
            }
        }
        element("declaration") {
            usagesMap[ruleName] = Pair(this, usagesMap[ruleName]?.second ?: mutableSetOf())
            attribute("name", ruleName)
        }
        element("description") {
            addWhitespace(2)
            element("symbol") { cdata(":") }
            addWhitespace()
            children.forEach {
                (_, _, buildElement) -> buildElement(rule)
            }
            element("crlf")
            addWhitespace(2)
            element("other") { text(";") }
        }
    }

    override fun block(groupingBracketsNeed: Boolean, children: List<ElementRenderResult>) = groupUsingPipe(children, groupingBracketsNeed)

    override fun set(groupingBracketsNeed: Boolean, children: List<ElementRenderResult>) = groupUsingPipe(children, groupingBracketsNeed)

    override fun alt(groupingBracketsNeed: Boolean, children: List<ElementRenderResult>) = joinThroughLength(children, groupingBracketsNeed)

    override fun root() = ElementRenderResult(0) {}

    override fun pred() = ElementRenderResult(0) {}

    override fun ruleRef(node: RuleRefAST) = ElementRenderResult(node.text.length) {
        element("identifier") {
            attribute("name", node.text)
        }
        usagesMap.putIfAbsent(node.text, Pair(null, mutableSetOf()))
        usagesMap[node.text]?.second?.add(currentRule!!)
    }

    override fun charsSet(node: GrammarAST) = ElementRenderResult(node.text.length) {
        element("string") { cdata (node.text) }
    }

    override fun terminal(node: TerminalAST): ElementRenderResult {
        val lexerRule = getLexerRule(node)

        return (lexerRule ?: node.text).let { nodeText ->
            ElementRenderResult(nodeText.length) {
                if (lexerRule == null) {
                    usagesMap.computeIfAbsent(nodeText) { Pair(null, mutableSetOf()) }
                    usagesMap[node.text]?.second?.add(currentRule!!)
                }

                if (customTerminalRenders.contains(nodeText)) {
                    customTerminalRenders[nodeText]!!()
                } else if (lexerRules.contains(nodeText) && lexerRule == null) {
                    element("identifier") { attribute("name", nodeText) }
                } else {
                    element("string") { cdata(nodeText) }
                }
            }
        }
    }

    override fun run(builder: IXmlGenerator.(XWriter) -> Unit) =
            XMLOutputter(Format.getPrettyFormat()).outputString(
                    jdom("tokens") {
                        builder(this)
                        arrangeUsages()
                    }
            )

    override fun XWriter.generateNotationDescription() {
        element("set") {
            attribute("file-name", "description")
            addDoc("description")
        }
    }

    override fun XWriter.generateLexerRules(visitor: GrammarVisitor) {
        currentMode = GeneratorType.LEXER

        generateRules(rules = getVisitedRules(filterLexerRules(excludeRules(lexerRules), usedLexerRules), visitor))
    }

    override fun XWriter.generateParserRules(visitor: GrammarVisitor) {
        currentMode = GeneratorType.PARSER

        generateRules(rules = getVisitedRules(excludeRules(parserRules), visitor))
    }
}

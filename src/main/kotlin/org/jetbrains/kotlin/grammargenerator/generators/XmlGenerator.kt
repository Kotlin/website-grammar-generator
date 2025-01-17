package org.jetbrains.kotlin.grammargenerator.generators

import org.antlr.runtime.tree.CommonTree
import org.antlr.v4.tool.Rule
import org.antlr.v4.tool.ast.GrammarAST
import org.antlr.v4.tool.ast.RuleRefAST
import org.antlr.v4.tool.ast.TerminalAST
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.LENGTH_FOR_RULE_SPLIT
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.excludedRules
import org.jetbrains.kotlin.grammargenerator.generators.Generator.Companion.rootNodes
import org.jetbrains.kotlin.grammargenerator.generators.XmlGenerator.Companion.DOCS_FOLDER
import org.jetbrains.kotlin.grammargenerator.visitors.GrammarVisitor
import org.w3c.dom.Element
import java.io.File
import java.io.StringWriter
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

enum class GeneratorType { LEXER, PARSER }

data class ElementRenderResult(
    val contentLength: Int,
    val sectionName: String? = null,
    val buildElement: Element.(rule: Rule) -> Unit
)

private typealias IXmlGenerator = Generator<ElementRenderResult, Element>

class XmlGenerator(
    override val lexerRules: Map<String, Rule>,
    override val parserRules: Map<String, Rule>,
    private val lexerGrammarFileLines: List<String>,
    private val parserGrammarFileLines: List<String>
) : IXmlGenerator {
    companion object {
        const val SECTION_DECLARATION_OFFSET = 3
        const val DOCS_FOLDER = "grammar/docs"
        private const val UNICODE_CLASSES_GRAMMAR_FILE_PATH =
            "https://github.com/Kotlin/kotlin-spec/blob/master/grammar/src/main/antlr/UnicodeClasses.g4"

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

        private fun renderUnicodeClassLink(unicodeClass: String): Element.() -> Unit = {
            createCDataElement("other", getLinkToUnicodeClass(unicodeClass))
        }
    }

    private val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    private val transformer = TransformerFactory.newInstance().newTransformer().apply {
        setOutputProperty(OutputKeys.INDENT, "yes")
        setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    }

    override val usedLexerRules = mutableSetOf<String>()
    override lateinit var currentMode: GeneratorType

    private val Rule.excluded: Boolean
        get() = usedLexerRules.contains(this.name)

    private var currentRule: String? = null
    private val usagesMap = mutableMapOf<String, Pair<Element?, MutableSet<String>>>()
    private val customTerminalRenders = unicodeClassesToLineInGrammarFileMap.entries.associate { (unicodeClass, _) ->
        "UNICODE_CLASS_$unicodeClass" to renderUnicodeClassLink(unicodeClass)
    }

    private fun getVisitedRules(rules: Map<String, Rule>, visitor: GrammarVisitor) =
        rules.entries.associate { (ruleName, rule) ->
            ruleName to Pair(rule, rule.ast.visit(visitor) as ElementRenderResult)
        }

    private fun addGreedyMarker(isGreedy: Boolean) = ElementRenderResult(if (isGreedy) 0 else 1) {
        if (!isGreedy) {
            createSymbol("?")
        }
    }

    private fun removeSubtreeFromExcludedRule(node: CommonTree) {
        if (node.parent == null) return

        node.parent.children.remove(node)

        if (node.parent.children?.isEmpty() == true) {
            removeSubtreeFromExcludedRule(node.parent)
        }
    }

    private fun excludeRulesRecursive(node: GrammarAST) {
        if (node.children == null) return

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
    ) = ElementRenderResult(elements.sumOf { (elementTextLength, _) -> elementTextLength + elements.size }) { rule ->
        var bufferSize = 0

        if (groupingBracketsNeed && elements.size > 1) createSymbol("(")

        elements.forEachIndexed { index, (elementTextLength, _, buildElement) ->
            if (index != 0) {
                bufferSize += elementTextLength
                if (bufferSize > LENGTH_FOR_RULE_SPLIT && !rulesWithCustomNewLines.contains(rule.name)) {
                    createCrlf()
                    bufferSize = 0
                    addWhitespace(3)
                } else if (rulesWithCustomNewLines.contains(rule.name) && rulesWithCustomNewLines[rule.name]!!.contains(index)) {
                    createCrlf()
                    addWhitespace(3)
                }
                addWhitespace()
            }
            buildElement(rule)
        }

        if (groupingBracketsNeed && elements.size > 1) createSymbol(")")
    }

    private fun groupUsingPipe(
        elements: List<ElementRenderResult>,
        groupingBracketsNeed: Boolean
    ) = ElementRenderResult(elements.sumOf { (elementTextLength, _) -> elementTextLength } + elements.size * 3) { rule ->
            if (groupingBracketsNeed && elements.size > 1) createSymbol("(")

            elements.forEachIndexed { index, (_, _, buildElement) ->
                if (index != 0) {
                    if (groupingBracketsNeed) addWhitespace() else {
                        createCrlf()
                        addWhitespace(2)
                    }
                    createSymbol("|")
                    addWhitespace()
                }
                buildElement(rule)
            }

            if (groupingBracketsNeed && elements.size > 1) createSymbol(")")
        }

    private fun getSectionDeclaration(ruleLineNumber: Int): String? {
        val targetGrammarFileLines =
            if (currentMode == GeneratorType.LEXER) lexerGrammarFileLines else parserGrammarFileLines
        val potentialSectionDeclarationLine =
            targetGrammarFileLines.getOrNull(ruleLineNumber - SECTION_DECLARATION_OFFSET) ?: return null

        return sectionPattern.matcher(potentialSectionDeclarationLine).let {
            if (it.find()) it.group("section") else null
        }
    }

    private fun runInContextBySectionInfo(
        rootContext: Element,
        currentContext: Element,
        rule: Rule,
        sectionName: String?,
        builder: Element.(Rule) -> Unit
    ) {
        if (sectionName != null) {
            with(rootContext.ownerDocument) {
                createElement("set").apply {
                    setAttribute("file-name", "sectionName")
                    addDoc(sectionName)
                    builder(rule)
                    rootContext.appendChild(this)
                }
            }
        } else currentContext.builder(rule)
    }

    private fun Element.generateRules(rules: Map<String, Pair<Rule, ElementRenderResult>>) {
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
                    with(ownerDocument) {
                        createElement("item").apply {
                            if (rule.isFragment) {
                                createTextElement("annotation", "helper")
                            }
                            buildElement(rule)
                            currentContext.appendChild(this)
                        }
                    }
                }
            }
        }
    }

    private fun arrangeUsages() {
        usagesMap.values.filter { (_, usages) -> usages.isNotEmpty() }.forEach { (element, usages) ->
            if (element == null) return@forEach

            with(element.ownerDocument) {
                createElement("usages").apply {
                    usages.forEach {
                        createTextElement("declaration", it)
                    }
                    element.appendChild(this)
                }
            }
        }
    }

    override fun optional(child: ElementRenderResult, isGreedy: Boolean): ElementRenderResult {
        val (greedyMarkerLength, _, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, _, buildChild) = child

        return ElementRenderResult(childContentLength + 1 + greedyMarkerLength) { rule ->
            buildChild(rule)
            createSymbol("?")
            addGreedyMarker(rule)
        }
    }

    override fun plus(child: ElementRenderResult, isGreedy: Boolean): ElementRenderResult {
        val (greedyMarkerLength, _, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, _, buildChild) = child

        return ElementRenderResult(childContentLength + 1 + greedyMarkerLength) { rule ->
            buildChild(rule)
            createSymbol("+")
            addGreedyMarker(rule)
        }
    }

    override fun star(child: ElementRenderResult, isGreedy: Boolean): ElementRenderResult {
        val (greedyMarkerLength, _, addGreedyMarker) = addGreedyMarker(isGreedy)
        val (childContentLength, _, buildChild) = child

        return ElementRenderResult(childContentLength + 1 + greedyMarkerLength) { rule ->
            buildChild(rule)
            createSymbol("*")
            addGreedyMarker(rule)
        }
    }

    override fun not(child: ElementRenderResult): ElementRenderResult {
        val (childContentLength, _, buildChild) = child

        return ElementRenderResult(childContentLength + 1) { rule ->
            createSymbol("~")
            buildChild(rule)
        }
    }

    override fun range(childLeft: ElementRenderResult, childRight: ElementRenderResult): ElementRenderResult {
        val (childLeftContentLength, _, buildChildLeft) = childLeft
        val (childRightContentLength, _, buildChildRight) = childRight

        return ElementRenderResult(childLeftContentLength + childRightContentLength + 2) { rule ->
            buildChildLeft(rule)
            createCDataElement("string", "..")
            buildChildRight(rule)
        }
    }

    override fun rule(
        children: List<ElementRenderResult>,
        ruleName: String,
        lineNumber: Int
    ) = ElementRenderResult(children.sumOf { it.contentLength }, getSectionDeclaration(lineNumber)) { rule ->
        val parent = this@ElementRenderResult

        with(parent.ownerDocument) {
            currentRule = ruleName

            if (rootNodes.contains(ruleName)) {
                createTextElement("annotation", "start")
            }
            createElement("declaration").apply {
                usagesMap[ruleName] = Pair(this, usagesMap[ruleName]?.second ?: mutableSetOf())
                setAttribute("name", ruleName)
                parent.appendChild(this)
            }
            createElement("description").apply {
                addWhitespace(2)
                createSymbol(":")
                addWhitespace()
                children.forEach { (_, _, buildElement) ->
                    buildElement(rule)
                }
                createCrlf()
                addWhitespace(2)
                createTextElement("other", ";")
                parent.appendChild(this)
            }
        }
    }

    override fun block(groupingBracketsNeed: Boolean, children: List<ElementRenderResult>) =
        groupUsingPipe(children, groupingBracketsNeed)

    override fun set(groupingBracketsNeed: Boolean, children: List<ElementRenderResult>) =
        groupUsingPipe(children, groupingBracketsNeed)

    override fun alt(groupingBracketsNeed: Boolean, children: List<ElementRenderResult>) =
        joinThroughLength(children, groupingBracketsNeed)

    override fun root() = ElementRenderResult(0) {}

    override fun pred() = ElementRenderResult(0) {}

    override fun ruleRef(node: RuleRefAST) = ElementRenderResult(node.text.length) {
        with(ownerDocument) {
            createElement("identifier").apply {
                setAttribute("name", node.text)
                this@ElementRenderResult.appendChild(this)
            }
        }
        usagesMap.putIfAbsent(node.text, Pair(null, mutableSetOf()))
        usagesMap[node.text]?.second?.add(currentRule!!)
    }

    override fun charsSet(node: GrammarAST) = ElementRenderResult(node.text.length) {
        createCDataElement("string", node.text)
    }

    override fun terminal(node: TerminalAST): ElementRenderResult {
        val lexerRule = getLexerRule(node)

        return (lexerRule ?: node.text).let { nodeText ->
            ElementRenderResult(nodeText.length) {
                if (lexerRule == null) {
                    usagesMap.computeIfAbsent(nodeText) { Pair(null, mutableSetOf()) }
                    usagesMap[node.text]?.second?.add(currentRule!!)
                }

                when {
                    customTerminalRenders.contains(nodeText) -> {
                        customTerminalRenders[nodeText]!!()
                    }
                    lexerRules.contains(nodeText) && lexerRule == null -> {
                        with(ownerDocument) {
                            createElement("identifier").apply {
                                setAttribute("name", nodeText)
                                this@ElementRenderResult.appendChild(this)
                            }
                        }
                    }
                    else -> {
                        createCDataElement("string", nodeText)
                    }
                }
            }
        }
    }

    override fun run(builder: IXmlGenerator.(Element) -> Unit) = StringWriter().apply {
        val doc = documentBuilder.newDocument()
        doc.createElement("tokens").apply {
            builder(this)
            arrangeUsages()
            doc.appendChild(this)
        }
        transformer.transform(DOMSource(doc), StreamResult(this))
    }.toString()

    override fun Element.generateNotationDescription() {
        with(ownerDocument) {
            createElement("set").apply {
                setAttribute("file-name", "description")
                addDoc("description")
                this@generateNotationDescription.appendChild(this)
            }
        }
    }

    override fun Element.generateLexerRules(visitor: GrammarVisitor) {
        currentMode = GeneratorType.LEXER
        generateRules(rules = getVisitedRules(filterLexerRules(excludeRules(lexerRules), usedLexerRules), visitor))
    }

    override fun Element.generateParserRules(visitor: GrammarVisitor) {
        currentMode = GeneratorType.PARSER
        generateRules(rules = getVisitedRules(excludeRules(parserRules), visitor))
    }
}

private fun Element.createSymbol(symbol: String) =
    createCDataElement("symbol", symbol)

private fun Element.createCDataElement(name: String, content: String): Element =
    with(ownerDocument) {
        createElement(name).also { element ->
            createCDATASection(content).also { cdata ->
                element.appendChild(cdata)
            }
            this@createCDataElement.appendChild(element)
        }
    }

private fun Element.createTextElement(name: String, content: String): Element =
    with(ownerDocument) {
        createElement(name).also { element ->
            createTextNode(content).also { cdata ->
                element.appendChild(cdata)
            }
            this@createTextElement.appendChild(element)
        }
    }

private fun Element.createCrlf(): Element =
    with(ownerDocument) {
        createElement("crlf").also { element ->
            this@createCrlf.appendChild(element)
        }
    }

private fun Element.addWhitespace(count: Int = 1): Element =
    createCDataElement("string", "&nbsp;".repeat(count))

private fun Element.addDoc(docName: String) {
    val sectionDocFile = File("$DOCS_FOLDER/$docName.md")

    if (sectionDocFile.exists()) {
        createCDataElement("doc", sectionDocFile.readText())
    }
}

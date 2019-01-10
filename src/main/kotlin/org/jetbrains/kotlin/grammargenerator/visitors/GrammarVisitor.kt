package org.jetbrains.kotlin.grammargenerator.visitors

import org.antlr.v4.tool.ast.*
import org.jetbrains.kotlin.grammargenerator.generators.Generator

enum class NodeType(val tokenId: Int) {
    TOKEN(66),
    SEQUENCE(32),
    PRODUCTION(57),
    BLOCK(78),
    ALT(74),
    SET(98),
    RULE(94),
    LITERAL(62),
    RULE_MODIFIERS(96),
    AT_LEAST_ONCE(90), // +
    UNKNOWN(80), // *
    AT_MOST_ONCE(89); // ?

    companion object {
        private val map = NodeType.values().associateBy(NodeType::tokenId)
        fun fromValue(tokenId: Int): NodeType? = map[tokenId]
    }
}

fun isGroupingBracketsNeed(node: GrammarAST): Boolean {
    val nodeType = NodeType.fromValue(node.type)

    return (nodeType == NodeType.BLOCK || nodeType == NodeType.SET || nodeType == NodeType.ALT) && node.children.size > 1
}

fun isGroupingBracketsNeedRecursive(node: GrammarAST): Boolean {
    if (isGroupingBracketsNeed(node))
        return true

    node.children?.forEach {
        if (isGroupingBracketsNeedRecursive(it as GrammarAST)) return true
    }

    return false
}

fun isSingleChildOfRule(node: GrammarAST): Boolean {
    if (NodeType.fromValue(node.type) == NodeType.RULE) return true
    if (node.children.size > 1) return false

    return isSingleChildOfRule(node.parent as GrammarAST)
}

class GrammarVisitor(private val generator: Generator): GrammarASTVisitor {
    override fun visit(node: GrammarAST) =
            when (NodeType.fromValue(node.type)) {
                NodeType.SEQUENCE -> node.text
                NodeType.SET -> {
                    val groupingBracketsNeed = isGroupingBracketsNeedRecursive(node) && !isSingleChildOfRule(node.parent as GrammarAST)
                    generator.set(groupingBracketsNeed, node.childrenAsArray.map { it.visit(this) })
                }
                else -> ""
            }

    override fun visit(node: GrammarRootAST) = generator.root()

    override fun visit(node: RuleAST) = generator.rule(node.ruleName, node.childrenAsArray.map { it.visit(this) })

    override fun visit(node: BlockAST): Any {
        val parent = node.parent as GrammarAST
        val groupingBracketsNeed = isGroupingBracketsNeedRecursive(node) && !isSingleChildOfRule(parent)

        return generator.block(groupingBracketsNeed, node.childrenAsArray.map { it.visit(this) })
    }
    override fun visit(node: OptionalBlockAST) = generator.optional(node.childrenAsArray[0].visit(this), node.isGreedy)
    override fun visit(node: PlusBlockAST) = generator.plus(node.childrenAsArray[0].visit(this), node.isGreedy)
    override fun visit(node: StarBlockAST) = generator.star(node.childrenAsArray[0].visit(this), node.isGreedy)
    override fun visit(node: AltAST) = generator.alt(node.childrenAsArray.map { it.visit(this) })
    override fun visit(node: NotAST) = generator.not(node.childrenAsArray[0].visit(this))
    override fun visit(node: PredAST) = generator.pred()
    override fun visit(node: RangeAST) = generator.range(node.childrenAsArray[0].visit(this), node.childrenAsArray[1].visit(this))
    override fun visit(node: SetAST) = generator.set(false, node.childrenAsArray.map { it.visit(this) })
    override fun visit(node: RuleRefAST) = generator.ruleRef(node)
    override fun visit(node: TerminalAST) = generator.terminal(node)
}

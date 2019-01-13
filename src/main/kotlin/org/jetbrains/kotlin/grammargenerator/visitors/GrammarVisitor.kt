package org.jetbrains.kotlin.grammargenerator.visitors

import org.antlr.v4.parse.ANTLRParser
import org.antlr.v4.tool.ast.*
import org.jetbrains.kotlin.grammargenerator.generators.Generator

fun isGroupingBracketsNeed(node: GrammarAST): Boolean {
    return (node.type == ANTLRParser.BLOCK || node.type == ANTLRParser.SET || node.type == ANTLRParser.ALT) && node.children.size > 1
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
    if (node.type == ANTLRParser.RULE) return true
    if (node.children.size > 1) return false

    return isSingleChildOfRule(node.parent as GrammarAST)
}

class GrammarVisitor(private val generator: Generator<Any, Any>): GrammarASTVisitor {
    override fun visit(node: GrammarAST) =
            when (node.type) {
                ANTLRParser.LEXER_CHAR_SET -> generator.root()
                ANTLRParser.SET -> {
                    val groupingBracketsNeed = isGroupingBracketsNeedRecursive(node) && !isSingleChildOfRule(node.parent as GrammarAST)
                    generator.set(groupingBracketsNeed, node.childrenAsArray.map { it.visit(this) })
                }
                else -> generator.root()
            }

    override fun visit(node: GrammarRootAST) = generator.root()

    override fun visit(node: RuleAST) = generator.rule(node.childrenAsArray.map { it.visit(this) }, node.ruleName, node.line)

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

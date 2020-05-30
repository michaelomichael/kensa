package dev.kensa.parse

import dev.kensa.KensaException
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

sealed class Event<PT : ParseTree>(val parseTree: PT) {

    val lineNumber: Int
        get() = when (parseTree) {
            is ParserRuleContext -> parseTree.start.line
            is TerminalNode -> parseTree.symbol.line

            else -> throw KensaException("Could not get line number from parse tree of type [${parseTree.javaClass}")
        }

    class EnterTestMethod(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class ExitTestMethod(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class TerminalNodeEvent(parseTree: TerminalNode) : Event<TerminalNode>(parseTree)
    class EnterMethodInvocationEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class ExitMethodInvocationEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class IdentifierEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class EnterStatementEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class ExitStatementEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)

    sealed class LiteralEvent(parseTree: ParseTree, val value: String) : Event<ParseTree>(parseTree) {
        class StringLiteralEvent(parseTree: ParseTree, value: String) : LiteralEvent(parseTree, value)
        class NumberLiteralEvent(parseTree: ParseTree, value: String) : LiteralEvent(parseTree, value)
    }
}
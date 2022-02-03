package dev.kensa.parse.java

import dev.kensa.parse.Event.*
import dev.kensa.parse.Event.LiteralEvent.NumberLiteralEvent
import dev.kensa.parse.Event.LiteralEvent.StringLiteralEvent
import dev.kensa.parse.Java8Lexer.*
import dev.kensa.parse.Java8Parser
import dev.kensa.parse.Java8ParserBaseListener
import dev.kensa.parse.ParserStateMachine
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

class JavaMethodBodyParser(private val stateMachine: ParserStateMachine) : Java8ParserBaseListener() {

    //  For Debugging:
//    override fun enterEveryRule(ctx: ParserRuleContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
//    }
//
//    override fun exitEveryRule(ctx: ParserRuleContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
//    }

    override fun enterExpression(ctx: Java8Parser.ExpressionContext) {
        stateMachine.transition(EnterExpressionEvent(ctx))
    }

    override fun exitExpression(ctx: Java8Parser.ExpressionContext) {
        stateMachine.transition(ExitExpressionEvent(ctx))
    }

    override fun enterMethodBody(ctx: Java8Parser.MethodBodyContext) {
        stateMachine.transition(EnterTestMethodEvent(ctx))
    }

    override fun exitMethodBody(ctx: Java8Parser.MethodBodyContext) {
        stateMachine.transition(ExitTestMethodEvent(ctx))
    }

    override fun enterStatement(ctx: Java8Parser.StatementContext) {
        stateMachine.transition(EnterStatementEvent(ctx))
    }

    override fun exitStatement(ctx: Java8Parser.StatementContext) {
        stateMachine.transition(ExitStatementEvent(ctx))
    }

    override fun enterMethodInvocation(ctx: Java8Parser.MethodInvocationContext) {
        stateMachine.transition(EnterMethodInvocationEvent(ctx))
    }

    override fun exitMethodInvocation(ctx: Java8Parser.MethodInvocationContext) {
        stateMachine.transition(ExitMethodInvocationEvent(ctx))
    }

    override fun enterMethodInvocation_lfno_primary(ctx: Java8Parser.MethodInvocation_lfno_primaryContext) {
        stateMachine.transition(EnterMethodInvocationEvent(ctx))
    }

    override fun exitMethodInvocation_lfno_primary(ctx: Java8Parser.MethodInvocation_lfno_primaryContext) {
        stateMachine.transition(ExitMethodInvocationEvent(ctx))
    }

    override fun enterMethodInvocation_lf_primary(ctx: Java8Parser.MethodInvocation_lf_primaryContext) {
        stateMachine.transition(EnterMethodInvocationEvent(ctx))
    }

    override fun exitMethodInvocation_lf_primary(ctx: Java8Parser.MethodInvocation_lf_primaryContext) {
        stateMachine.transition(ExitMethodInvocationEvent(ctx))
    }

    override fun visitTerminal(node: TerminalNode) {
        when (node.symbol.type) {
            IntegerLiteral, FloatingPointLiteral -> stateMachine.transition(NumberLiteralEvent(node, node.text))
            CharacterLiteral, StringLiteral -> stateMachine.transition(StringLiteralEvent(node, stripStartEndQuotes(node.text)))
            Identifier -> stateMachine.transition(IdentifierEvent(node))

            else -> stateMachine.transition(TerminalNodeEvent(node))
        }
    }

    private fun stripStartEndQuotes(value: String): String = optionalQuotesRegex.matchEntire(value)?.groupValues?.get(1) ?: value

    companion object {
        private val optionalQuotesRegex = "^\"(.*)\"$|^(.*)$".toRegex()
    }
}
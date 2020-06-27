package ru.dlevin.lox

class RpnAstPrinter : Visitor<String> {
    fun print(expression: Expression): String {
        return expression.accept(this)
    }

    override fun visitBinaryExpr(expr: Binary): String {
        val left = expr.left.accept(this)
        val right = expr.right.accept(this)
        return "$left $right ${expr.operator.lexeme}"
    }

    override fun visitGroupingExpr(expr: Grouping): String {
        return expr.expression.accept(this)
    }

    override fun visitLiteralExpr(expr: Literal): String {
        return expr.value?.toString() ?: "nil";
    }

    override fun visitUnaryExpr(expr: Unary): String {
        val right = expr.right.accept(this)
        return "$right ${expr.operator.lexeme}"
    }
}

fun main() {
    val expression: Expression = Binary(
        Binary(
            Literal(1),
            Token(TokenType.PLUS, "+", null, 1),
            Literal(2)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Binary(
            Literal(4),
            Token(TokenType.MINUS, "-", null, 1),
            Literal(3)
        )
    )

    println(RpnAstPrinter().print(expression))
}
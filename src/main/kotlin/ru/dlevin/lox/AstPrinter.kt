package ru.dlevin.lox

class AstPrinter : Visitor<String> {
    fun print(expression: Expression): String {
        return expression.accept(this)
    }

    override fun visitBinaryExpr(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGroupingExpr(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Literal): String {
        return expr.value?.toString() ?: "nil";
    }

    override fun visitUnaryExpr(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private fun parenthesize(name: String, vararg expressions: Expression): String {
        return expressions.joinToString(separator = " ", prefix = "($name ", postfix = ")") { it.accept(this) }
    }
}

fun main() {
    val expression: Expression = Binary(
        Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Literal(123)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Grouping(
            Literal(45.67)
        )
    )

    println(AstPrinter().print(expression))
}
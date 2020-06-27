package ru.dlevin.lox


sealed class Expression {

    abstract fun <R> accept(visitor: Visitor<R>): R
}

data class Binary(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression() {

    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitBinaryExpr(this)
    }
}

data class Grouping(
    val expression: Expression
) : Expression() {

    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitGroupingExpr(this)
    }
}

data class Literal(
    val value: Any?
) : Expression() {

    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitLiteralExpr(this)
    }
}

data class Unary(
    val operator: Token,
    val right: Expression
) : Expression() {

    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitUnaryExpr(this)
    }
}

interface Visitor<R> {
    fun visitBinaryExpr(expr: Binary): R
    fun visitGroupingExpr(expr: Grouping): R
    fun visitLiteralExpr(expr: Literal): R
    fun visitUnaryExpr(expr: Unary): R
}

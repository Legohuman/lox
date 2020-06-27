package ru.dlevin.lox

import ru.dlevin.lox.TokenType.*


class AstEvaluator : Visitor<Any?> {
    fun evaluate(expression: Expression) {
        try {
            val value = expression.accept(this)
            println(stringify(value))
        } catch (error: EvaluateException) {
            LoxRunner.runtimeError(error)
        }
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        // Hack. Work around Java adding ".0" to integer-valued doubles.
        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }
        return obj.toString()
    }

    override fun visitBinaryExpr(expr: Binary): Any? {
        val left = expr.left.accept(this)
        val right = expr.right.accept(this)
        return when (val type = expr.operator.type) {
            PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw EvaluateException(expr.operator, "Operands must be two numbers or two strings.")
                }
            }
            MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                left as Double - right as Double
            }
            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double * right as Double
            }
            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double / right as Double
            }
            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double > right as Double
            }
            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double >= right as Double
            }
            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < right as Double
            }
            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double <= right as Double
            }
            BANG_EQUAL -> !isEqual(left, right);
            EQUAL_EQUAL -> isEqual(left, right);
            else -> throw EvaluateException(expr.operator, "Illegal operator type in binary expression.")
        }
    }

    override fun visitGroupingExpr(expr: Grouping): Any? {
        return expr.expression.accept(this)
    }

    override fun visitLiteralExpr(expr: Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = expr.right.accept(this)
        return when (expr.operator.type) {
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }
            BANG -> !isTruthy(right)
            else -> throw EvaluateException(expr.operator, "Illegal operator type in unary expression")
        }
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        return if (obj is Boolean) obj else true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        // nil is only equal to nil.
        if (a == null && b == null) return true
        return if (a == null) false else a == b
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw EvaluateException(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw EvaluateException(operator, "Operands must be numbers.")
    }
}

class EvaluateException(val token: Token, message: String) : RuntimeException(message)


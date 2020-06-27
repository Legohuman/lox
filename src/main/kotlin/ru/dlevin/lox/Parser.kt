package ru.dlevin.lox

import ru.dlevin.lox.TokenType.*

/**
 * Lox grammar
 *
 * expression → literal | unary | binary | grouping ;
 *
 * literal    → NUMBER | STRING | "false" | "true" | "nil" ;
 * grouping   → "(" expression ")" ;
 * unary      → ( "-" | "!" ) expression ;
 * binary     → expression operator expression ;
 * operator   → "==" | "!=" | "<" | "<=" | ">" | ">=" | "+"  | "-"  | "*" | "/" ;
 */
class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): Expression? {
        return try {
            expression()
        } catch (error: ParseError) {
            null
        }
    }

    private fun expression(): Expression {
        return equality()
    }

    private fun equality(): Expression {
        var expr: Expression = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator: Token = previous()
            val right: Expression = comparison()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(): Expression {
        var expr: Expression = addition()
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right: Expression = addition()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun addition(): Expression {
        var expr: Expression = multiplication()
        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right: Expression = multiplication()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun multiplication(): Expression {
        var expr: Expression = unary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right: Expression = unary()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expression {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right: Expression = unary()
            return Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expression {
        if (match(FALSE)) return Literal(false)
        if (match(TRUE)) return Literal(true)
        if (match(NIL)) return Literal(null)
        if (match(NUMBER, STRING)) {
            return Literal(previous().literal)
        }
        if (match(LEFT_PAREN)) {
            val expr: Expression = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Grouping(expr)
        }

        throw error(peek(), "Unexpected token.")
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        LoxRunner.error(token, message)
        return ParseError()
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        return if (isAtEnd()) false else peek().type === type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type === TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
            }
            advance()
        }
    }

}

private class ParseError : RuntimeException()
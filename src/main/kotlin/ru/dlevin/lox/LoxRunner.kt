package ru.dlevin.lox

import ru.dlevin.lox.TokenType.EOF
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


@Throws(IOException::class)
fun main(args: Array<String>) {
    when {
        args.size > 1 -> {
            println("Usage: jlox [script]")
            exitProcess(64)
        }
        args.size == 1 -> {
            LoxRunner.runFile(args[0])
        }
        else -> {
            LoxRunner.runPrompt()
        }
    }
}

object LoxRunner {

    private val evaluator = AstEvaluator()

    private var hadError = false
    private var hadRuntimeError = false

    @Throws(IOException::class)
    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        if (hadError) exitProcess(65);
        if (hadRuntimeError) exitProcess(70);
    }

    @Throws(IOException::class)
    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print("> ")
            run(reader.readLine())
            hadError = false;
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        val tree = Parser(tokens).parse()

        if (tree != null) {
            evaluator.evaluate(tree)
        }
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type === EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message)
        }
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println(
            "[line $line] Error$where: $message"
        )
        hadError = true
    }

    fun runtimeError(error: EvaluateException) {
        System.err.println(
            error.message.toString() + "\n[line " + error.token.line + "]"
        )
        hadRuntimeError = true
    }
}
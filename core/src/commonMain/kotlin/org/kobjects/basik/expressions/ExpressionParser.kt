package org.kobjects.basik.expressions

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser

object ExpressionParser : PrattParser<BasicTokenizer, Unit, Evaluable>(
    parsePrimary = { scanner, _ -> ExpressionParser.parsePrimary(scanner) },
    buildUnary = { _, _, name, operand ->
        when (name.uppercase()) {
            "+" -> operand
            "-" -> Builtin(Builtin.Kind.NEG, operand)
            "NOT" -> Builtin(Builtin.Kind.NOT, operand)
            else -> throw IllegalArgumentException("Unrecognized unary operator: '$name'")
        }
    },
    buildBinary = { _, _, name, left, right ->
        when (name.uppercase()) {
            "AND" -> Builtin(Builtin.Kind.AND, left, right)
            "OR" -> Builtin(Builtin.Kind.OR, left, right)
            else -> Builtin(name, left, right)
        }
    },
    Operator.Prefix(Builtin.Kind.NEG.precedence, "+", "-"),
    Operator.Infix(Builtin.Kind.POW.precedence, "^"),
    Operator.Infix(Builtin.Kind.MUL.precedence, "*", "/"),
    Operator.Infix(Builtin.Kind.ADD.precedence, "+", "-"),
    Operator.Infix(Builtin.Kind.EQ.precedence, "=", "<", "<=", "<>", ">", ">="),
    Operator.Infix(Builtin.Kind.AND.precedence, "AND", "And", "and"),
    Operator.Infix(Builtin.Kind.OR.precedence, "OR", "Or", "or"),
    Operator.Prefix(Builtin.Kind.NOT.precedence, "NOT", "Not", "not"),
) {
    private fun parsePrimary(tokenizer: BasicTokenizer): Evaluable =
        when (tokenizer.current.type) {
            TokenType.NUMBER ->
                Literal(tokenizer.consume().text.toDouble())
            TokenType.STRING -> {
                val text = tokenizer.consume().text
                Literal(
                    text.substring(1, text.length - 1)
                        .replace("\"\"", "\"")
                )
            }
            TokenType.IDENTIFIER -> {
                var name = tokenizer.consume().text
                if (name.equals("FN", ignoreCase = true) && tokenizer.current.type == TokenType.IDENTIFIER) {
                    name += " " + tokenizer.consume().text
                }

                if (tokenizer.tryConsume("(")) {
                    val params = parseParameterList(tokenizer, ")")
                    val builtin = Builtin.Kind.values().firstOrNull { it.toString().equals(name, ignoreCase = true) }
                    if (builtin == null) Parameterized(name, params)
                    else Builtin(builtin, *params.toTypedArray<Evaluable>())
                } else {
                    Variable(name.lowercase())
                }
            }
            TokenType.SYMBOL -> {
                if (!tokenizer.tryConsume("(")) {
                    throw tokenizer.exception("Unrecognized primary expression.")
                }
                val expr = parseExpression(tokenizer)
                tokenizer.consume(")")
                Builtin(Builtin.Kind.EMPTY, expr)
            }
            else ->
                throw tokenizer.exception("Unrecognized primary expression.")
    }


    fun parseParameterList(tokenizer: BasicTokenizer, endToken: String): List<Evaluable> {
        val parameters = mutableListOf<Evaluable>()
        if (tokenizer.current.text != endToken) {
            do {
                parameters.add(parseExpression(tokenizer, Unit))
            } while (tokenizer.tryConsume(","))
        }
        tokenizer.consume(endToken)
        return parameters
    }

    fun parseExpression(tokenizer: BasicTokenizer): Evaluable = parseExpression(tokenizer, Unit)
}

package org.kobjects.basik

import org.kobjects.basik.expressions.*

object Parser {


    fun parseStatement(tokenizer: BasicTokenizer, interpreter: Interpreter, lineNumber: Int, result: MutableList<Statement>): Boolean {

        fun addStatement(kind: Statement.Kind, vararg params: Evaluable, delimiters: List<String> = emptyList()) {
            result.add(Statement(lineNumber, result.size, kind, *params, delimiters = delimiters))
        }

        while (tokenizer.tryConsume(":")) {
            addStatement(Statement.Kind.EMPTY)
        }
        if (tokenizer.current.type == TokenType.EOF) {
            return false
        }

        var name = tokenizer.current.text
        if (tokenizer.tryConsume("GO", ignoreCase = true)) {  // GO TO, GO SUB -> GOTO, GOSUB
            name += tokenizer.current.text
        } else if (name == "?") {
            name = "PRINT"
        }
        var type: Statement.Kind? = null
        for (t in Statement.Kind.values()) {
            if (name.equals(t.name, ignoreCase = true)) {
                type = t
                break
            }
        }
        if (type == null) {
            type = Statement.Kind.LET
        } else {
            tokenizer.consume()
        }
        when (type) {
            Statement.Kind.RUN,
            Statement.Kind.RESTORE -> {
                if (tokenizer.current.type != TokenType.EOF &&
                    tokenizer.current.text != ":"
                ) addStatement(type, ExpressionParser.parseExpression(tokenizer))
                else addStatement(type)
            }
            Statement.Kind.DEF,
            Statement.Kind.GOTO,
            Statement.Kind.GOSUB,
            Statement.Kind.LOAD -> addStatement(
                type,
                ExpressionParser.parseExpression(tokenizer))
            Statement.Kind.NEXT -> {
                if (tokenizer.current.type == TokenType.EOF || tokenizer.current.text == ":") {
                    addStatement(type)
                } else {
                    do {
                        addStatement(type, ExpressionParser.parseExpression(tokenizer))
                    } while (tokenizer.tryConsume(","))
                }
            }
            Statement.Kind.DATA,
            Statement.Kind.DIM,
            Statement.Kind.READ -> {
                val expressions = mutableListOf<Evaluable>()
                do {
                    expressions.add(ExpressionParser.parseExpression(tokenizer))
                } while (tokenizer.tryConsume(","))
                addStatement(type, *expressions.toTypedArray())
            }
            Statement.Kind.FOR -> {
                val assignment = ExpressionParser.parseExpression(tokenizer)
                if (assignment !is Builtin || assignment.kind != Builtin.Kind.EQ ||
                    assignment.param[0] !is Variable) {
                    throw tokenizer.exception("Variable assignment expected after FOR")
                }
                tokenizer.consume( "TO")
                val end = ExpressionParser.parseExpression(tokenizer)
                if (tokenizer.tryConsume( "STEP", ignoreCase = true)) {
                    addStatement(
                        type,
                        assignment.param[0],
                        assignment.param[1],
                        end,
                        ExpressionParser.parseExpression(tokenizer),
                        delimiters = listOf(" = ", " TO ", " STEP ")
                    )
                } else addStatement(
                    type,
                    assignment.param[0],
                    assignment.param[1],
                    end,
                    delimiters = listOf(" = ", " TO ", " STEP ")
                )
            }
            Statement.Kind.IF -> {
                val condition = ExpressionParser.parseExpression(tokenizer)
                if (!tokenizer.tryConsume( "THEN", ignoreCase = true) && !tokenizer.tryConsume( "GOTO", ignoreCase = true)) {
                    throw tokenizer.exception("'THEN expected after IF-condition.'")
                }
                addStatement(type, condition, delimiters = listOf(" THEN "))
                if (tokenizer.current.type === TokenType.NUMBER) {
                    val target = tokenizer.consume().text.toDouble()
                    addStatement(Statement.Kind.GOTO, Literal(target))
                } else {
                    return true
                }
            }
            Statement.Kind.INPUT,
            Statement.Kind.PRINT -> {
                val args = mutableListOf<Evaluable>()
                val delimiter = mutableListOf<String>()
                while (tokenizer.current.type !== TokenType.EOF
                    && tokenizer.current.text != ":"
                ) {
                    if (tokenizer.current.text == "," || tokenizer.current.text == ";") {
                        delimiter.add(tokenizer.consume().text + " ")
                        if (delimiter.size > args.size) {
                            args.add(InvisibleStringLiteral)
                        }
                    } else {
                        args.add(ExpressionParser.parseExpression(tokenizer))
                    }
                }
                addStatement(type, *args.toTypedArray(), delimiters = delimiter)
            }
            Statement.Kind.LET -> {
                val assignment = ExpressionParser.parseExpression(tokenizer)
                if (assignment !is Builtin || assignment.param[0] !is Settable
                    || assignment.kind != Builtin.Kind.EQ
                ) {
                    throw tokenizer.exception(
                        "Unrecognized statement or illegal assignment: '$assignment'.")

                }
                addStatement(type, *assignment.param, delimiters = listOf(" = "))
            }
            Statement.Kind.ON -> {
                val expressions = mutableListOf<Evaluable>()
                expressions.add(ExpressionParser.parseExpression(tokenizer))
                var kind: String
                if (tokenizer.tryConsume("GOTO", ignoreCase = true)) {
                    kind = " GOTO "
                } else if (tokenizer.tryConsume( "GOSUB", ignoreCase = true)) {
                    kind = " GOSUB "
                } else {
                    throw tokenizer.exception("GOTO or GOSUB expected.")
                }
                do {
                    expressions.add(ExpressionParser.parseExpression(tokenizer))
                } while (tokenizer.tryConsume(","))
                addStatement(type, *expressions.toTypedArray(), delimiters = listOf(kind))
            }
            Statement.Kind.REM -> {
                val sb = StringBuilder()
                while (tokenizer.current.type != TokenType.EOF) {
                    sb.append(' ' /* support tokenizer.leadingWhitespace? */ )
                        .append(tokenizer.consume().text)
                }
                if (sb.isNotEmpty() && sb[0] == ' ') {
                    sb.deleteAt(0)
                }
                addStatement(type, Variable(sb.toString()))
                return false
            }
            else -> addStatement(type)
        }
        return tokenizer.tryConsume(":")
    }

    fun parseStatementList(tokenizer: BasicTokenizer, interpreter: Interpreter, lineNumber: Int): List<Statement> {
        val result = mutableListOf<Statement>()

        while (parseStatement(tokenizer, interpreter, lineNumber, result)) {
            //
        }

        if (tokenizer.current.type !== TokenType.EOF) {
            throw tokenizer.exception("Leftover input.")
        }
        return result.toList()
    }
}
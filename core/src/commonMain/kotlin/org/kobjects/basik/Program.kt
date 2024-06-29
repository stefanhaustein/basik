package org.kobjects.basik

import kotlinx.coroutines.yield

class Program {
    val statements = mutableListOf<Statement>()

    suspend fun eval(interpreter: Interpreter) {
        while(interpreter.nextStatementIndex < statements.size) {
            statements[interpreter.nextStatementIndex++].eval(interpreter)
            yield()
        }
    }


    fun indexOf(lineNumber: Int): Int {
        val index = statements.binarySearch {
            val delta = it.lineNumber.compareTo(lineNumber)
            if (delta != 0) delta else it.index.compareTo(0)
        }
        return if (index < 0) -index - 1 else index
    }


    fun setLine(lineNumber: Int, line: List<Statement>) {
        val index = indexOf(lineNumber)

        while (index < statements.size && statements[index].lineNumber == lineNumber) {
            statements.removeAt(index)
        }

        for (i in line.indices) {
            statements.add(index + i, line[i])
        }
    }

    fun find(
        kind: Statement.Kind,
        name: String?,
        position: IntArray
    ): Statement? {
        while (position[0] < statements.size) {
            val statement = statements[position[0]]
            if (statement.kind == kind &&
                    (name == null || statement.params.size == 0 || statement.params[1].toString().equals(name, ignoreCase = true))) {
                return statement
            }
            position[0]++
        }
        return null
    }

    override fun toString(): String {
        var currentLine = -1
        var wasIf = false
        val sb = StringBuilder()
        for (statement in statements) {
            if (statement.lineNumber != currentLine) {
                currentLine = statement.lineNumber
                if (!sb.isEmpty()) {
                    sb.append("\n")
                }
                sb.append(statement.lineNumber).append(' ')
            } else if (!wasIf) {
                sb.append(" : ")
            }
            sb.append(statement)
            wasIf = statement.kind == Statement.Kind.IF
        }
        sb.append('\n')
        return sb.toString()
    }

}
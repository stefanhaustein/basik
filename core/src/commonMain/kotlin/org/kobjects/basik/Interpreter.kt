package org.kobjects.basik

import org.kobjects.basik.expressions.*
import kotlin.random.Random


class Interpreter(
    val printFn: (String) -> Unit = { println(it) },
    val readFn: suspend (String) -> String = {
        if (it.isNotEmpty()) {
            printFn(it)
        }
        readln()
    },
    val loadFn: suspend (name: String) -> String = { throw UnsupportedOperationException() },
    val saveFn: suspend (name: String, content: String) -> Unit = { _, _ -> throw UnsupportedOperationException() },
) : Context {
    override val variables = mutableListOf<MutableMap<String, Any>>(mutableMapOf())

    val program = Program()

    val stack = mutableListOf<StackEntry>()

    var trace = false
    var pendingOutput = ""
    var nextStatementIndex = 0
    var currentStatement = Statement(-1, -1, Statement.Kind.END)

    var currentTabPos = 0
    var stoppedAt: Int? = null
    var dataPosition = IntArray(2)
    var dataStatement: Statement? = null
    var seeds = mutableMapOf<Int, Random>()

    fun clear() {
        variables.clear()
        variables.add(mutableMapOf())
    }

    fun continueCommand() {
        val stoppedAt = stoppedAt ?:
            throw IllegalStateException("Not stopped.")

        nextStatementIndex = stoppedAt
    }

    fun defFn(assignment: Evaluable) {
        val definition = FunctionDefinition(assignment)
        ((assignment as Builtin).param[0] as Settable).set(this, definition)
    }

    fun dump() {
        TODO("Not yet implemented")
    }

    fun forStatement(params: Array<out Evaluable>) {
        val loopVar = params[0] as Variable
        loopVar.set(this, params[1].evalDouble(this))
        val current = loopVar.evalDouble(this)
        val end = params[2].evalDouble(this)
        val step = if (params.size > 3) params[3].evalDouble(this) else 1.0
        if (signum(step) == signum(current.compareTo(end))) {
            val nextPosition = IntArray(1)
            if (program.find(
                    Statement.Kind.NEXT,
                    params[0].toString(),
                    nextPosition) == null) {
                throw RuntimeException("FOR without NEXT")
            }
            nextStatementIndex = nextPosition[0] + 1
        } else {
            stack.add(StackEntry(
                nextStatementIndex,
                loopVar,
                step = step,
                end = end
            ))
        }
    }

    fun gosub(lineNumber: Int) {
        stack.add(StackEntry(nextStatementIndex))
        goto(lineNumber)
    }

    fun goto(lineNumber: Int) {
        nextStatementIndex = program.indexOf(lineNumber)
    }

    fun ifStatement(condition: Boolean, goto: Evaluable?, elseGoto: Int) {
        if (!condition) {
            goto(elseGoto)
        } else if (goto != null) {
            goto(goto.evalInt(this))
        }
    }

    suspend fun input(params: Array<out Evaluable>, delimiters: List<String>) {
        if (pendingOutput.isNotEmpty()) {
            print("\n")
        }
        val label = StringBuilder()
        for (i in params.indices) {
            val child = params[i]
            if (child is Settable) {
                if (i <= 0 || i > delimiters.size || delimiters.get(i - 1) != ", ") {
                    label.append("? ")
                }
                val variable = child as Settable
                var value: Any
                if (label.isNotBlank()) {
                    printFn(label.toString())
                }
                val numeric = !variable.name.endsWith("$")
                while (true) {
                    value = readFn(if (numeric) "Please enter a number" else "Please enter text")
                    if (!numeric) {
                        break
                    }
                    try {
                        value = value.toDouble()
                        break
                    } catch (e: NumberFormatException) {
                        printFn("Not a number. Please enter a number.")
                    }
                }
                label.clear()
                variable.set(this, value)
            } else {
                label.append(child.eval(this))
            }
        }
    }

    suspend fun load(fileName: String) {
        val code = loadFn(fileName)
        val lines = code.split("\n")
        new()
        for (line in lines) {
            processInputLine(line)
        }
    }

    fun new() {
        clear()
        restore(null)
        stack.clear()
        program.statements.clear()
    }

    fun next(params: Array<out Evaluable>) {
        val name: String? = if (params.isEmpty()) null else params[0].toString()
        var entry: StackEntry
        while (true) {
            if (stack.isEmpty() || stack.get(stack.size - 1).forVariable == null) {
                throw IllegalStateException("NEXT $name without FOR.")
            }
            entry = stack.removeAt(stack.size - 1)
            if (name == null || entry.forVariable?.name == name) {
                break
            }
            val loopVariable = entry.forVariable!!
            val current = loopVariable.evalDouble(this) + entry.step
            loopVariable.set(this, current)
            if (signum(entry.step) != signum(current.compareTo(entry.end))) {
                stack.add(entry)
                nextStatementIndex = entry.statementIndex
                break
            }
        }
    }


    fun on(params: Array<out Evaluable>, gosub: Boolean) {
        val index = params[0].evalInt(this)
        if (index < params.size && index > 0) {
            val line = params[index].evalInt(this)
            if (gosub) {
                gosub(line)
            } else {
                goto(line)
            }
        }
    }

    fun print(s: String) {
        pendingOutput += s
        var cut = 0
        while (true) {
            val newLine = pendingOutput.indexOf("\n", cut)
            if (newLine == -1) {
                break
            }
            printFn(pendingOutput.substring(cut, newLine))
            cut = newLine + 1
        }
        pendingOutput = pendingOutput.substring(cut)
    }

    fun print(params: Array<out Evaluable>, delimiters: List<String>) {
        for (i in params.indices) {
            val value = params[i].eval(this)
            if (value is Double) {
                print((if (value < 0) "" else " ") + value + " ")
            } else {
                print(value.toString())
            }
            if (i < delimiters.size && delimiters[i] == ", ") {
                print("                    ".substring(0, 14 - currentTabPos % 14))
            }
        }
        if (delimiters.size < params.size &&
            (params.isEmpty() || !params[params.size - 1].toString().startsWith("TAB"))) {
            print("\n")
        }
    }

    suspend fun processInputLine(line: String): Boolean {
        val tokenizer = BasicTokenizer(line)
        return when (tokenizer.current.type) {
            TokenType.EOF -> false
            TokenType.NUMBER -> {
                val lineNumber = tokenizer.consume().text.toInt()
                program.setLine(lineNumber, if (tokenizer.current.type === TokenType.EOF) listOf() else  Parser.parseStatementList(tokenizer, this, lineNumber))
                false
            }
            else -> {
                val line = Parser.parseStatementList(tokenizer, this, -2)
                nextStatementIndex = -1
                for (statement in line) {
                    statement.eval(this)
                    if (nextStatementIndex != -1) {
                        program.eval(this)
                        if (nextStatementIndex != -1) {
                            break;
                        }
                    }
                }
                true
            }
        }
    }

    override fun random(seed: Int): Double =
        seeds.getOrPut(seed) { Random(seed) }.nextDouble()


    fun read(params: Array<out Evaluable>) {
        for (child in params) {
            while (dataStatement == null
                || dataPosition[1] >= dataStatement!!.params.size
            ) {
                dataPosition[1] = 0
                if (dataStatement != null) {
                    dataPosition[0]++
                }
                dataStatement = program.find(
                    Statement.Kind.DATA,
                    null,
                    dataPosition
                )
                if (dataStatement == null) {
                    throw IllegalStateException("Out of data.")
                }
            }
            (child as Settable).set(this, dataStatement!!.params[dataPosition[1]++].eval(this))
        }
    }

    fun restore(lineNumber: Int?) {
        dataStatement = null
        dataPosition.fill(0)
        if (lineNumber != null) {
            dataPosition[0] = program.indexOf(lineNumber)
        }
    }

    fun returnStatement() {
        while (!stack.isEmpty()) {
            val entry = stack.removeLast()
            if (entry.forVariable == null) {
                nextStatementIndex = entry.statementIndex
                return
            }
        }
        throw IllegalStateException("RETURN without GOSUB.")
    }

    suspend fun runCommand() {
        clear()
        nextStatementIndex = 0
        program.eval(this)
    }

    suspend fun runShell() {
        printFn("""Try '10 print "Hello World"'
            |Then type 'run' to execute the program.
            |To "load" an existing program, paste it into the command line""".trimMargin())
        while (true) {
            val s = readFn("READY.")
            try {
                for (line in s.split("\n")) {
                    processInputLine(line)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                printFn("Error in line ${currentStatement.lineNumber}, statement '$currentStatement': $e")
            }
        }
    }

    fun stop() {
        stoppedAt = nextStatementIndex
        goto(Int.MAX_VALUE)
    }

    suspend fun save(fileName: String) {
        saveFn(fileName, program.toString())
    }

    companion object {
        fun signum(value: Double): Int = if (value < 0.0) -1 else if (value > 0.0) 1 else 0
        fun signum(value: Int): Int = if (value < 0) -1 else if (value > 0) 1 else 0
    }
}
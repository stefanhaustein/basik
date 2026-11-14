package org.kobjects.basik.expressions

/** Array access or function call */
class Parameterized (
    override val name: String,
    val parameters: List<Evaluable>,
): Evaluable, Settable {
    private class ArrayDimension(val entries: MutableMap<Int, Any> = mutableMapOf())

    val defaultValue: Any
        get() = if (name.endsWith("$")) "" else 0.0

    override fun eval(ctx: Context): Any {
        if (parameters.size >= ctx.variables.size) {
            return defaultValue
        }

        val rootValue = ctx.variables[parameters.size].getOrElse(name) {
            return defaultValue
        }

        if (rootValue is FunctionDefinition) {
           return rootValue.eval(ctx, parameters)
        }

        var currentDimension = rootValue as ArrayDimension

        for (i in 0 until parameters.size - 1) {
            currentDimension = currentDimension.entries.getOrElse(parameters[i].evalInt(ctx)) {
                return defaultValue
            } as ArrayDimension
        }

        return currentDimension.entries.getOrElse(parameters.last().evalInt(ctx)) {
            defaultValue
        }
    }

    override fun set(ctx: Context, value: Any) {
         while (parameters.size >= ctx.variables.size) {
            ctx.variables.add(mutableMapOf())
        }

        if (value is FunctionDefinition) {
            ctx.variables[parameters.size][name] = value
            return
        }

        var currentDimension = ctx.variables[parameters.size].getOrPut(name) {
            ArrayDimension()
        } as ArrayDimension


        for (i in 0 until parameters.size - 1) {
            currentDimension = currentDimension.entries.getOrPut(parameters[i].evalInt(ctx)) {
                ArrayDimension()
            } as ArrayDimension
        }

        currentDimension.entries[parameters.last().evalInt(ctx)] = value
    }

    override fun toString() = "$name(${parameters.joinToString(", ")})"
}

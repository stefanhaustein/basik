package org.kobjects.basik.expressions

class Variable(
    override val name: String,
) : Evaluable, Settable {

    override fun eval(ctx: Context) =
        ctx.variables[0][name] ?: if (name.endsWith("$")) "" else 0.0

    override fun set(ctx: Context, value: Any) {
        ctx.variables[0][name] = value
    }

    override fun toString() = name
}
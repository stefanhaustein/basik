package org.kobjects.basik.expressions

class Literal(val value: Any) : Evaluable {

    override fun eval(ctx: Context) = value

    override fun toString() =
        if (value is String) "\"" + value.replace("\"", "\"\"") + "\""
        else value.toString()
}
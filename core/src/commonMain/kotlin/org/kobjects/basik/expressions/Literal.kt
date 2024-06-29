package org.kobjects.basik.expressions

class Literal(val value: Any) : Evaluable {

    override fun eval(ctx: Context) = value

    override fun toString() =
        if (value is String) "\"" + value.replace("\"", "\"\"") + "\""
        else if (value is Double && value.toLong() == value) value.toLong().toString()
        else value.toString()
}
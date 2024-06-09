package org.kobjects.basik

import org.kobjects.basik.expressions.Context
import org.kobjects.basik.expressions.Evaluable

object InvisibleStringLiteral : Evaluable {
    val INVISIBLE_STRING = ""

    override fun eval(ctx: Context) = INVISIBLE_STRING

    override fun toString() = INVISIBLE_STRING
}
package org.kobjects.basik.expressions

interface Settable {
    val name: String

    fun set(ctx: Context, value: Any)

}
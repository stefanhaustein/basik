package org.kobjects.basik.expressions

/**
 * Here, we use this class as parsing context and runtime context, but they could
 * be separate classes, in particular for compiled languages.
 */
interface Context {
    // The array index corresponds to the number of parameters.
    val variables: MutableList<MutableMap<String, Any>>
}
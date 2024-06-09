package org.kobjects.basik

import org.kobjects.basik.expressions.Variable

class StackEntry(
    val lineIndex: Int,
    val statementIndex: Int,
    val forVariable: Variable? = null,
    val step: Double = 1.0,
    val end: Double = 0.0
)
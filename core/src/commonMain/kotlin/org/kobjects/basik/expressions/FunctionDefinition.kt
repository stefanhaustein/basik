package org.kobjects.basik.expressions

class FunctionDefinition(assignment: Evaluable) {

    val parameterNames: List<String>
    val expression: Evaluable

    init {
        require(assignment is Builtin
                && assignment.kind == Builtin.Kind.EQ) { "Assignment expected after def." }

        val call = assignment.param[0]
        require(call is Parameterized) { "Function declaration expected; got: $call" }

        parameterNames = call.parameters.map {
            require(it is Variable) { "Parameter name expected; got $it" }
            it.name
        }

        expression = assignment.param[1]
    }


    fun eval(ctx: Context, params: List<Evaluable>): Any {
        val saved = arrayOfNulls<Any>(parameterNames.size)
        for (i in parameterNames.indices) {
            val param = parameterNames[i]
            saved[i] = ctx.variables[0][param]
            ctx.variables[0][param] = params[i].eval(ctx)
        }
        return try {
            expression.eval(ctx)
        } finally {
            for (i in parameterNames.indices) {
                val restore = saved[i]
                val name = parameterNames[i]
                if (restore == null) {
                    ctx.variables[0].remove(name)
                } else {
                    ctx.variables[0][name] = restore
                }
            }
        }
    }
}
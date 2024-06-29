package org.kobjects.basik.expressions

import org.kobjects.parsek.tokenizer.Lexer
import org.kobjects.parsek.tokenizer.RegularExpressions
import org.kobjects.parsek.tokenizer.Scanner

class BasicTokenizer(input: String) : Scanner<TokenType>(
    Lexer(
        input,
        RegularExpressions.WHITESPACE to { null },
        RegularExpressions.NUMBER to { TokenType.NUMBER },
        CSV_STRING to { TokenType.STRING },
        RegularExpressions.IDENTIFIER to { TokenType.IDENTIFIER },
        SYMBOL to { TokenType.SYMBOL },
        ),
    TokenType.EOF) {

    companion object {
        val SYMBOL = Regex("\\+|-|\\*|%|<=|>=|==|=|<>|<|>|\\^|!=|!|\\(|\\)|,|\\?|;|~|\\[|]|\\{|\\}|/|:")
        val CSV_STRING = Regex(""""([^"]*(""[^"]*)*)"""")
    }

}
package org.kobjects.basik.expressions

import org.kobjects.parsek.tokenizer.RegularExpressions
import org.kobjects.parsek.tokenizer.Scanner

class BasicTokenizer(input: String) : Scanner<TokenType>(
    input,
    TokenType.EOF,
    RegularExpressions.WHITESPACE to null,
    RegularExpressions.UNSIGNED_NUMBER to TokenType.NUMBER,
    RegularExpressions.CSV_STRING to TokenType.STRING,
    RegularExpressions.IDENTIFIER to TokenType.IDENTIFIER,
    SYMBOL to TokenType.SYMBOL,
) {

    companion object {
        val SYMBOL = Regex("\\+|-|\\*|%|<=|>=|==|=|<>|<|>|\\^|!=|!|\\(|\\)|,|\\?|;|~|\\[|]|\\{|\\}|/|:")
    }

}

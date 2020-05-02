package Lexers;

import com.intellij.openapi.options.SchemeImportException;

public interface ColorsLexer {
    ColorType yylex() throws java.io.IOException, SchemeImportException;
}

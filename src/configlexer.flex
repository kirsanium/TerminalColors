import com.intellij.openapi.options.SchemeImportException;
import java.awt.*;

%%

%{
    String colorString;
    int color;
    String colorIdentifier = null;
    boolean hexFound = false;
    int ansiAmount = 0;
    boolean ansiFound = false;
    boolean backgroundFound = false;
    boolean foregroundFound = false;
%}

%class ConfigColorsLexer
%type ColorType
%unicode
%yylexthrow SchemeImportException

    SchemeName = "[["~"]]"
    HexColor = [0-9a-f]{6}
    LineTerminator = \r|\n|\r\n
    InputCharacter = [^\r\n]
    WhiteSpace     = {LineTerminator} | [ \t\f]
    CursorSequence = "#" {HexColor} "\""

%state COLORS
%state READINGMODE
%state CURSORMODE
%state BGMODE

%%
    <YYINITIAL> {
    {SchemeName}                        { /* ignore */}
      "palette = \""                      {colorIdentifier = "ANSI"; yybegin(COLORS);}
      "background_color = \""             {colorIdentifier = "BACKGROUND"; yybegin(COLORS);}
      "foreground_color = \""             {colorIdentifier = "FOREGROUND"; yybegin(COLORS);}
      "cursor_color = \""                 {yybegin(CURSORMODE);}
      "background_image = "               {yybegin(BGMODE);}
      {WhiteSpace}                   { /* ignore */}
      {InputCharacter}               {throw new SchemeImportException("Scheme is not valid");}
      <<EOF>>                        {if (!ansiFound || !backgroundFound || !foregroundFound)
                                        throw new SchemeImportException("Scheme is not valid");
                                        return null;}
    }

    <COLORS> {
        #                            {yybegin(READINGMODE);}
    }

    <READINGMODE>
    {
        {HexColor}                   {if (!hexFound) {
                                      color = Integer.parseInt(yytext().toString(),16);
                                      hexFound = true;
                                      }
                                      else throw new SchemeImportException("Scheme is not valid");
                                      }
        :                             {if (!hexFound) {throw new SchemeImportException("Scheme is not valid");}
                                        yybegin(COLORS);
                                        if (colorIdentifier == "ANSI") {
                                        colorString = ColorType.ANSI[ansiAmount];
                                        ansiAmount++;
                                        }
                                        else throw new SchemeImportException("Scheme is not valid");
                                        hexFound = false;
                                        return new ColorType(colorString, new Color(color));
                                        }
        \"                            {if (!hexFound) {throw new SchemeImportException("Scheme is not valid");}
                                         if (colorIdentifier == "ANSI" && ansiAmount == 15) {ansiFound = true; colorString = ColorType.ANSI[ansiAmount];}
                                         else if (colorIdentifier == "BACKGROUND") {backgroundFound = true; colorString = ColorType.BACKGROUND;}
                                         else if (colorIdentifier == "FOREGROUND") {foregroundFound = true; colorString = ColorType.FOREGROUND;}
                                         else throw new SchemeImportException("Scheme is not valid");
                                         colorIdentifier = null;
                                         hexFound = false;
                                         yybegin(YYINITIAL);
                                         return new ColorType(colorString, new Color(color));
                                       }
        [^]           {throw new SchemeImportException("Scheme is not valid");}
    }

    <CURSORMODE>
    {
        {CursorSequence} {yybegin(YYINITIAL);}
    }

    <BGMODE>
    {
        {WhiteSpace}                   { /* ignore */}
        {InputCharacter}               { /* ignore */}
        <<EOF>>                        {if (!ansiFound || !backgroundFound || !foregroundFound)
                                                throw new SchemeImportException("Scheme is not valid");
                                                return null;}
    }
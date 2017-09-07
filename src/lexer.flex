import java.io.IOException;
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

    SchemeName = "[["~"]]"
    HexColor = [0-9a-f]{6}
    LineTerminator = \r|\n|\r\n
    InputCharacter = [^\r\n]
    WhiteSpace     = {LineTerminator} | [ \t\f]

%state COLORS
%state READINGMODE

%%
    <YYINITIAL> {
    {SchemeName}                        { /* ignore */}
      "palette = "                      {colorIdentifier = "ANSI";}
      "background_color = "             {colorIdentifier = "BACKGROUND";}
      "foreground_color = "             {colorIdentifier = "FOREGROUND";}

      \"                          { if (colorIdentifier != null) yybegin(COLORS);}
      {WhiteSpace}                   { /* ignore */}
      {InputCharacter}               { /* ignore */}
      <<EOF>>                        {if (!ansiFound || !backgroundFound || !foregroundFound)
                                        throw new IOException("Scheme is not valid");
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
                                      else throw new IOException("Scheme is not valid");
                                      }
        :                             {if (!hexFound) {throw new IOException("Scheme is not valid");}
                                        yybegin(COLORS);
                                        if (colorIdentifier == "ANSI") {
                                        colorString = ColorType.ANSI[ansiAmount];
                                        ansiAmount++;
                                        }
                                        else throw new IOException("Scheme is not valid");
                                        hexFound = false;
                                        return new ColorType(colorString, color);
                                        }
        \"                            {if (!hexFound) {throw new IOException("Scheme is not valid");}
                                         if (colorIdentifier == "ANSI" && ansiAmount == 15) {ansiFound = true; colorString = ColorType.ANSI[ansiAmount];}
                                         else if (colorIdentifier == "BACKGROUND") {backgroundFound = true; colorString = ColorType.BACKGROUND;}
                                         else if (colorIdentifier == "FOREGROUND") {foregroundFound = true; colorString = ColorType.FOREGROUND;}
                                         else throw new IOException("Scheme is not valid");
                                         colorIdentifier = null;
                                         hexFound = false;
                                         yybegin(YYINITIAL);
                                         return new ColorType(colorString, color);
                                       }
        [^]           {throw new IOException("Scheme is not valid");}
    }

    /* error fallback */
    //[^]                              { throw new Error("Illegal character <"+yytext()+">"); }
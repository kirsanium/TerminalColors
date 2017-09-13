import com.intellij.openapi.options.SchemeImportException;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;
%%

%{
private int red = -1;
private int green = -1;
private int blue = -1;
private int colorAmount = 0;
private String colorIdentifier;
private boolean colorNumberFound = false;
private boolean decColorFound = false;
private Color color;
private static Map<String, String> nameMap;
static {
  nameMap = new HashMap<>();
  nameMap.put("0", ColorType.FOREGROUND);
  nameMap.put("2", ColorType.BACKGROUND);
  nameMap.put("6", ColorType.ANSI[0]);
  nameMap.put("7", ColorType.ANSI[8]);
  nameMap.put("8", ColorType.ANSI[1]);
  nameMap.put("9", ColorType.ANSI[9]);
  nameMap.put("10", ColorType.ANSI[2]);
  nameMap.put("11", ColorType.ANSI[10]);
  nameMap.put("12", ColorType.ANSI[3]);
  nameMap.put("13", ColorType.ANSI[11]);
  nameMap.put("14", ColorType.ANSI[4]);
  nameMap.put("15", ColorType.ANSI[12]);
  nameMap.put("16", ColorType.ANSI[5]);
  nameMap.put("17", ColorType.ANSI[13]);
  nameMap.put("18", ColorType.ANSI[6]);
  nameMap.put("19", ColorType.ANSI[14]);
  nameMap.put("20", ColorType.ANSI[7]);
  nameMap.put("21", ColorType.ANSI[15]);
}
%}

%class RegColorsLexer
%type ColorType
%unicode
%yylexthrow SchemeImportException
%implements ColorsLexer

    DecColor = [0-9] | [1-9][0-9] | 1[0-9][0-9] | 2[0-4][0-9] | 25[0-5]
    ColorNumber = [0-9] | 1[0-9] | 2[01]
    LineTerminator = \r|\n|\r\n
    InputCharacter = [^\r\n]
    WhiteSpace     = {LineTerminator} | [ \t\f]
    Float = [0-9]+ "\." [0-9]+
    SchemePath = \[~\]
    Windows = "Windows Registry Editor Version " {Float}

%state COLORS
%state READINGMODE
%state WRONGMODE

%%
    <YYINITIAL> {
        {Windows} {/* ignore */}
        {SchemePath} {/* ignore */}

      "\"Colour"                        {yybegin(COLORS);}
      {WhiteSpace}                   { /* ignore */}
      //{InputCharacter}               { /* ignore */}
      <<EOF>>                        {if (colorAmount != 22)
                                        throw new SchemeImportException("Scheme is not valid");
                                        return null;}
      //[^]                            { System.out.println(yytext());}

    }

    <COLORS> {
        {ColorNumber}                 {colorAmount++;
                                        if (nameMap.containsKey(yytext().toString())){
                                        if (!colorNumberFound) {
                                        colorIdentifier = nameMap.get(yytext().toString());
                                        colorNumberFound = true;}
                                        else throw new SchemeImportException("Scheme is not valid");}
                                        else yybegin(WRONGMODE);
                                        }
        "\"=\""                       {if (colorNumberFound) yybegin(READINGMODE);
                                        else throw new SchemeImportException("Scheme is not valid");}
    }

    <READINGMODE>
    {
        {DecColor}                      {if (!decColorFound)
                                        {if (red == -1) red = Integer.parseInt(yytext().toString());
                                        else if (green == -1) green = Integer.parseInt(yytext().toString());
                                        else if (blue == -1) blue = Integer.parseInt(yytext().toString());
                                        else throw new SchemeImportException("Scheme is not valid");
                                        decColorFound = true;}
                                        else throw new SchemeImportException("Scheme is not valid");}
        ,                               {if (decColorFound && blue < 0) decColorFound = false;
                                        else throw new SchemeImportException("Scheme is not valid");}
        \"                              {if (red < 0 || green < 0 || blue < 0) throw new SchemeImportException("Scheme is not valid");
                                        else {
                                        color = new Color(red,green,blue);
                                        red = green = blue = -1;
                                        decColorFound = false;
                                        colorNumberFound = false;
                                        yybegin(YYINITIAL);
                                        return new ColorType(colorIdentifier, color);
                                        }}
    }

    <WRONGMODE>
    {
    "\"=\"" {/*ignore*/}
    {DecColor} {/*ignore*/}
    , {/*ignore*/}
    \" {yybegin(YYINITIAL);}
    }

    /* error fallback */
    //[^]                              { throw new Error("Illegal character <"+yytext()+">"); }
import com.intellij.openapi.options.SchemeImportException;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;
%%

%{
private int generalCounter = 0;
private int red = -1;
private int green = -1;
private int blue = -1;
private int colorAmount = 0;
private String colorIdentifier;
private boolean decColorFound = false;
private boolean eqFound = false;
private Color color;
private static Map<String, String> nameMap;
static {
  nameMap = new HashMap<>();
  nameMap.put("Foreground]", ColorType.FOREGROUND);
  nameMap.put("Background]", ColorType.BACKGROUND);
  nameMap.put("Color0]", ColorType.ANSI[0]);
  nameMap.put("Color1]", ColorType.ANSI[1]);
  nameMap.put("Color2]", ColorType.ANSI[2]);
  nameMap.put("Color3]", ColorType.ANSI[3]);
  nameMap.put("Color4]", ColorType.ANSI[4]);
  nameMap.put("Color5]", ColorType.ANSI[5]);
  nameMap.put("Color6]", ColorType.ANSI[6]);
  nameMap.put("Color7]", ColorType.ANSI[7]);
  nameMap.put("Color0Intense]", ColorType.ANSI[8]);
  nameMap.put("Color1Intense]", ColorType.ANSI[9]);
  nameMap.put("Color2Intense]", ColorType.ANSI[10]);
  nameMap.put("Color3Intense]", ColorType.ANSI[11]);
  nameMap.put("Color4Intense]", ColorType.ANSI[12]);
  nameMap.put("Color5Intense]", ColorType.ANSI[13]);
  nameMap.put("Color6Intense]", ColorType.ANSI[14]);
  nameMap.put("Color7Intense]", ColorType.ANSI[15]);
}
%}

%class ColorschemeColorsLexer
%type ColorType
%unicode
%yylexthrow SchemeImportException
%implements ColorsLexer

    DecColor           = [0-9] | [1-9][0-9] | 1[0-9][0-9] | 2[0-4][0-9] | 25[0-5]
    Modifier           = ("Intense"|"Faint")
    ColorNumber        = [0-7] ({Modifier})?
    ColorName          = ("Background" ({Modifier})? | "Foreground" ({Modifier})? | "Color" {ColorNumber}) "]"
    LineTerminator     = \r|\n|\r\n
    InputCharacter     = [^\r\n]
    WhiteSpace         = {LineTerminator} | [ \t\f]
    OpFloat            = "0" (\.[0-9]+)? | "1"
    Description        = "Description=" {InputCharacter}* {WhiteSpace}
    Opacity            = "Opacity=" {OpFloat} {LineTerminator}
    Wallpaper          = "Wallpaper=" {InputCharacter}* {WhiteSpace}
    Blur               = "Blur=" {InputCharacter}* {WhiteSpace}
    ColorRandomization = "ColorRandomization=" {InputCharacter}* {WhiteSpace}
    WrongSequence      = "Color=" {DecColor} "," {DecColor} "," {DecColor}

%state COLORS
%state READINGMODE, GENERALMODE
%state WRONGMODE

%%
    <YYINITIAL> {
      \[                                {yybegin(COLORS);}
      {WhiteSpace} {/*ignore*/}
      <<EOF>>                        {return null;}
      {InputCharacter} {/*ignore*/}
    }

    <COLORS> {
        {WhiteSpace} {/*ignore*/}
        "General]"                      {yybegin(GENERALMODE);}
        {ColorName}                     {colorAmount++;
                                            if (nameMap.containsKey(yytext().toString())) {
                                            colorIdentifier = nameMap.get(yytext().toString());
                                            yybegin(READINGMODE);
                                            }
                                            else yybegin(WRONGMODE);}
    }

    <GENERALMODE> {
        {LineTerminator}{LineTerminator} {yybegin(YYINITIAL);}
        {WhiteSpace} {/*ignore*/}
        {Description} {/*ignore*/}
        {Opacity} {/*ignore*/}
        {Wallpaper} {/*ignore*/}
        {Blur} {/*ignore*/}
        {ColorRandomization} {/*ignore*/}
        <<EOF>> {yybegin(YYINITIAL);}
    }

    <READINGMODE>
    {
        "Color=" {eqFound = true;}
        {DecColor}                      {if (eqFound) {
                                            if (!decColorFound)
                                            {if (red == -1) red = Integer.parseInt(yytext().toString());
                                            else if (green == -1) green = Integer.parseInt(yytext().toString());
                                            else if (blue == -1) blue = Integer.parseInt(yytext().toString());
                                            else throw new SchemeImportException("Scheme is not valid");
                                            if (red >= 0 && green >= 0 && blue >= 0) {
                                                color = new Color(red,green,blue);
                                                red = green = blue = -1;
                                                decColorFound = false;
                                                eqFound = false;
                                                yybegin(YYINITIAL);
                                                return new ColorType(colorIdentifier, color);
                                            }
                                            decColorFound = true;}
                                        else throw new SchemeImportException("Scheme is not valid");}
                                        else throw new SchemeImportException("Scheme is not valid");
                                        }
        ,                               {if (decColorFound && blue < 0) decColorFound = false;
                                        else throw new SchemeImportException("Scheme is not valid");}
        {WhiteSpace} {/*ignore*/}
        <<EOF>>                        {yybegin(YYINITIAL);}
    }

    <WRONGMODE>
    {
    {WhiteSpace} {}
    {WrongSequence} {yybegin(YYINITIAL);}
    }
import com.intellij.execution.process.ColoredOutputTypeRegistry;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.AttributesFlyweight;
import com.intellij.openapi.editor.colors.impl.EditorColorsManagerImpl;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.SchemeFactory;
import com.intellij.openapi.options.SchemeImportException;
import com.intellij.openapi.options.SchemeImportUtil;
import com.intellij.openapi.options.SchemeImporter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.jdom.Element;

public class ImportConsoleColorScheme implements SchemeImporter<EditorColorsScheme> {

    public ImportConsoleColorScheme() {
    }

    @NotNull
    @Override
    public String[] getSourceExtensions() {
        return new String[]{"reg", "colorscheme", "config", "itermcolors", "terminal"};
    }


    private Map<String, Comparable> initColorTypeMap() {
        Map<String, Comparable> colorsMap = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            colorsMap.put(ColorType.ANSI[i], ColoredOutputTypeRegistry.getAnsiColorKey(i));
        }
        colorsMap.put(ColorType.BACKGROUND, ConsoleViewContentType.CONSOLE_BACKGROUND_KEY);
        colorsMap.put(ColorType.FOREGROUND, ConsoleViewContentType.NORMAL_OUTPUT_KEY);
        return colorsMap;
    }

    @Nullable
    @Override
    public EditorColorsScheme importScheme(@NotNull Project project, @NotNull VirtualFile selectedFile, @NotNull EditorColorsScheme currentScheme, @NotNull SchemeFactory<EditorColorsScheme> schemeFactory) throws SchemeImportException {
        String name = selectedFile.getNameWithoutExtension();
        String extension = selectedFile.getExtension();
        EditorColorsManagerImpl manager = (EditorColorsManagerImpl) EditorColorsManagerImpl.getInstance();
        EditorColorsScheme newScheme = (EditorColorsScheme)manager.getGlobalScheme().clone();
        newScheme.setName(name);
        if (extension.equals("reg") || extension.equals("colorscheme") || extension.equals("config")) {
            try {
                newScheme = parseRegConfigColorschemeFile(selectedFile, newScheme, extension);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (extension.equals("itermcolors")) {
            newScheme = parseItermcolorsFile(selectedFile, newScheme);
        }
        else if (extension.equals("terminal")) {
            newScheme = parseTerminalFile(selectedFile, newScheme);
        }
        return newScheme;
    }

    private EditorColorsScheme parseRegConfigColorschemeFile(VirtualFile selectedFile, EditorColorsScheme newScheme, String extension) throws IOException, SchemeImportException {
        Map<String, Comparable> colorsMap = initColorTypeMap();
        Comparable key;
        AttributesFlyweight f;
        TextAttributes attrs;
        InputStream input = selectedFile.getInputStream();
        InputStreamReader isr = new InputStreamReader(input);
        ColorsLexer ccl;
        switch (extension) {
            case "reg": {
                ccl = new RegColorsLexer(isr);
                break;
            }
            case "colorscheme": {
                ccl = new ColorschemeColorsLexer(isr);
                break;
            }
            case "config": {
                ccl = new ConfigColorsLexer(isr);
                break;
            }
            default: throw new SchemeImportException("Wrong extension");
        }
        ColorType colorType = ccl.yylex();
        while(colorType != null) {
            key = colorsMap.get(colorType.getColorIdentifier());
            if (colorType.getColorIdentifier().equals(ColorType.BACKGROUND))
                newScheme.setColor((ColorKey)key, colorType.getColor());
            else {
                if (colorType.getColorIdentifier().equals(ColorType.FOREGROUND)) {
                    f = AttributesFlyweight.create(colorType.getColor(), null, 0, colorType.getColor(), EffectType.LINE_UNDERSCORE, null);
                    attrs = TextAttributes.fromFlyweight(f);
                    newScheme.setAttributes(ConsoleViewContentType.ERROR_OUTPUT_KEY, attrs);
                }
                f = AttributesFlyweight.create(colorType.getColor(), null, 0, null, null, null);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes((TextAttributesKey) key, attrs);
            }
            colorType = ccl.yylex();
        }
        return newScheme;
    }


//    private EditorColorsScheme parseColorschemeFile(VirtualFile selectedFile, EditorColorsScheme newScheme) throws IOException, SchemeImportException {
//        Map<String, Comparable> colorsMap = initColorTypeMap();
//        Comparable key;
//        AttributesFlyweight f;
//        TextAttributes attrs;
//        InputStream input = selectedFile.getInputStream();
//        InputStreamReader isr = new InputStreamReader(input);
//        ColorschemeColorsLexer ccl = new ColorschemeColorsLexer(isr);
//        ColorType colorType = ccl.yylex();
//        while(colorType != null) {
//            key = colorsMap.get(colorType.getColorIdentifier());
//            if (colorType.getColorIdentifier().equals(ColorType.BACKGROUND))
//                newScheme.setColor((ColorKey)key, colorType.getColor());
//            else {
//                if (colorType.getColorIdentifier().equals(ColorType.FOREGROUND)) {
//                    f = AttributesFlyweight.create(colorType.getColor(), null, 0, colorType.getColor(), EffectType.LINE_UNDERSCORE, null);
//                    attrs = TextAttributes.fromFlyweight(f);
//                    newScheme.setAttributes(ConsoleViewContentType.ERROR_OUTPUT_KEY, attrs);
//                }
//                f = AttributesFlyweight.create(colorType.getColor(), null, 0, null, null, null);
//                attrs = TextAttributes.fromFlyweight(f);
//                newScheme.setAttributes((TextAttributesKey) key, attrs);
//            }
//            colorType = ccl.yylex();
//        }
//        return newScheme;
//    }
//
//
//    private EditorColorsScheme parseConfigFile(VirtualFile selectedFile, EditorColorsScheme newScheme) throws IOException, SchemeImportException {
//        Map<String, Comparable> colorsMap = initColorTypeMap();
//        Comparable key;
//        AttributesFlyweight f;
//        TextAttributes attrs;
//        InputStream input = selectedFile.getInputStream();
//        InputStreamReader isr = new InputStreamReader(input);
//        ConfigColorsLexer ccl = new ConfigColorsLexer(isr);
//        ColorType colorType = ccl.yylex();
//        while(colorType != null) {
//            key = colorsMap.get(colorType.getColorIdentifier());
//            if (colorType.getColorIdentifier().equals(ColorType.BACKGROUND))
//                newScheme.setColor((ColorKey)key, colorType.getColor());
//            else {
//                if (colorType.getColorIdentifier().equals(ColorType.FOREGROUND)) {
//                    f = AttributesFlyweight.create(colorType.getColor(), null, 0, colorType.getColor(), EffectType.LINE_UNDERSCORE, null);
//                    attrs = TextAttributes.fromFlyweight(f);
//                    newScheme.setAttributes(ConsoleViewContentType.ERROR_OUTPUT_KEY, attrs);
//                }
//                f = AttributesFlyweight.create(colorType.getColor(), null, 0, null, null, null);
//                attrs = TextAttributes.fromFlyweight(f);
//                newScheme.setAttributes((TextAttributesKey) key, attrs);
//            }
//            colorType = ccl.yylex();
//        }
//        return newScheme;
//    }


    private EditorColorsScheme parseItermcolorsFile(VirtualFile selectedFile, EditorColorsScheme newScheme) throws SchemeImportException {
        Map<String, Comparable> colorsMap = new HashMap<>();
        colorsMap.put("Ansi 0 Color", ColoredOutputTypeRegistry.getAnsiColorKey(0));
        colorsMap.put("Ansi 1 Color", ColoredOutputTypeRegistry.getAnsiColorKey(1));
        colorsMap.put("Ansi 2 Color", ColoredOutputTypeRegistry.getAnsiColorKey(2));
        colorsMap.put("Ansi 3 Color", ColoredOutputTypeRegistry.getAnsiColorKey(3));
        colorsMap.put("Ansi 4 Color", ColoredOutputTypeRegistry.getAnsiColorKey(4));
        colorsMap.put("Ansi 5 Color", ColoredOutputTypeRegistry.getAnsiColorKey(5));
        colorsMap.put("Ansi 6 Color", ColoredOutputTypeRegistry.getAnsiColorKey(6));
        colorsMap.put("Ansi 7 Color", ColoredOutputTypeRegistry.getAnsiColorKey(7));
        colorsMap.put("Ansi 8 Color", ColoredOutputTypeRegistry.getAnsiColorKey(8));
        colorsMap.put("Ansi 9 Color", ColoredOutputTypeRegistry.getAnsiColorKey(9));
        colorsMap.put("Ansi 10 Color", ColoredOutputTypeRegistry.getAnsiColorKey(10));
        colorsMap.put("Ansi 11 Color", ColoredOutputTypeRegistry.getAnsiColorKey(11));
        colorsMap.put("Ansi 12 Color", ColoredOutputTypeRegistry.getAnsiColorKey(12));
        colorsMap.put("Ansi 13 Color", ColoredOutputTypeRegistry.getAnsiColorKey(13));
        colorsMap.put("Ansi 14 Color", ColoredOutputTypeRegistry.getAnsiColorKey(14));
        colorsMap.put("Ansi 15 Color", ColoredOutputTypeRegistry.getAnsiColorKey(15));
        colorsMap.put("Background Color", ConsoleViewContentType.CONSOLE_BACKGROUND_KEY);
        colorsMap.put("Foreground Color", ConsoleViewContentType.NORMAL_OUTPUT_KEY);
        float red = -1, blue = -1, green = -1;
        Color color;
        AttributesFlyweight f;
        TextAttributes attrs;
        Element root = SchemeImportUtil.loadSchemeDom(selectedFile);
        Element dict = root.getChild("dict");
        List<Element> ansiKeys = dict.getChildren("key");
        List<Element> colors = dict.getChildren("dict");
        if (colors.isEmpty() || ansiKeys.isEmpty() || ansiKeys.size() != colors.size()) {
            throw new InvalidDataException("Scheme is not valid");
        }
        Iterator ansiKeysIterator = ansiKeys.iterator();
        Iterator colorsIterator = colors.iterator();
        while (ansiKeysIterator.hasNext()) {
            Element ansiKey = (Element) ansiKeysIterator.next();
            Element colorDict = (Element) colorsIterator.next();
            String ansiKeyName = ansiKey.getValue();
            if (!colorsMap.containsKey(ansiKeyName))
                continue;
            List<Element> colorDictKeys = colorDict.getChildren("key");
            List<Element> colorDictRGB = colorDict.getChildren("real");
            Iterator colorDictKeysIterator = colorDictKeys.iterator();
            Iterator colorDictRGBIterator = colorDictRGB.iterator();
            while (colorDictKeysIterator.hasNext()) {
                Element colorComponentName = (Element) colorDictKeysIterator.next();
                Element floatNumElem = (Element) colorDictRGBIterator.next();
                String floatNumStr = floatNumElem.getValue();
                if (colorComponentName.getValue().equals("Blue Component")) {
                    blue = Float.parseFloat(floatNumStr);
                } else if (colorComponentName.getValue().equals("Green Component")) {
                    green = Float.parseFloat(floatNumStr);
                } else if (colorComponentName.getValue().equals("Red Component")) {
                    red = Float.parseFloat(floatNumStr);
                }
            }
            if (blue == -1 || red == -1 || green == -1) {
                throw new SchemeImportException("Scheme is not valid");
            }
            color = new Color(red, green, blue);
            if (ansiKeyName.equals("Background Color")) {
                newScheme.setColor((ColorKey)colorsMap.get(ansiKeyName), color);
            } else {
                if (ansiKeyName.equals("Foreground Color")){
                    f = AttributesFlyweight.create(color, null, 0, color, EffectType.LINE_UNDERSCORE, null);
                    attrs = TextAttributes.fromFlyweight(f);
                    newScheme.setAttributes(ConsoleViewContentType.ERROR_OUTPUT_KEY, attrs);
                }
                f = AttributesFlyweight.create(color, null, 0, null, null, null);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes((TextAttributesKey)colorsMap.get(ansiKeyName), attrs);
            }
        }
        return newScheme;
    }

// добавить обработку исключений
    private EditorColorsScheme parseTerminalFile(VirtualFile selectedFile, EditorColorsScheme newScheme) throws SchemeImportException {
        Map<String, Comparable> colorsMap = new HashMap<>();
        colorsMap.put("ANSIBlackColor", ColoredOutputTypeRegistry.getAnsiColorKey(0));
        colorsMap.put("ANSIRedColor", ColoredOutputTypeRegistry.getAnsiColorKey(1));
        colorsMap.put("ANSIGreenColor", ColoredOutputTypeRegistry.getAnsiColorKey(2));
        colorsMap.put("ANSIYellowColor", ColoredOutputTypeRegistry.getAnsiColorKey(3));
        colorsMap.put("ANSIBlueColor", ColoredOutputTypeRegistry.getAnsiColorKey(4));
        colorsMap.put("ANSIMagentaColor", ColoredOutputTypeRegistry.getAnsiColorKey(5));
        colorsMap.put("ANSICyanColor", ColoredOutputTypeRegistry.getAnsiColorKey(6));
        colorsMap.put("ANSIWhiteColor", ColoredOutputTypeRegistry.getAnsiColorKey(7));
        colorsMap.put("ANSIBrightBlackColor", ColoredOutputTypeRegistry.getAnsiColorKey(8));
        colorsMap.put("ANSIBrightRedColor", ColoredOutputTypeRegistry.getAnsiColorKey(9));
        colorsMap.put("ANSIBrightGreenColor", ColoredOutputTypeRegistry.getAnsiColorKey(10));
        colorsMap.put("ANSIBrightYellowColor", ColoredOutputTypeRegistry.getAnsiColorKey(11));
        colorsMap.put("ANSIBrightBlueColor", ColoredOutputTypeRegistry.getAnsiColorKey(12));
        colorsMap.put("ANSIBrightMagentaColor", ColoredOutputTypeRegistry.getAnsiColorKey(13));
        colorsMap.put("ANSIBrightCyanColor", ColoredOutputTypeRegistry.getAnsiColorKey(14));
        colorsMap.put("ANSIBrightWhiteColor", ColoredOutputTypeRegistry.getAnsiColorKey(15));
        colorsMap.put("BackgroundColor", ConsoleViewContentType.CONSOLE_BACKGROUND_KEY);
        colorsMap.put("TextColor", ConsoleViewContentType.NORMAL_OUTPUT_KEY);
        Color color;
        AttributesFlyweight f;
        TextAttributes attrs;
        Comparable key;
        String[] splitting;
        Element root = SchemeImportUtil.loadSchemeDom(selectedFile);
        Element dict;
        dict = root.getChild("dict");
        List<Element> dataList = dict.getChildren();
        if (dataList.isEmpty()) {
            throw new InvalidDataException("Scheme is not valid; no data");
        }
        Iterator dataIterator = dataList.iterator();
        while (dataIterator.hasNext()) {
            Element colorElement = (Element)dataIterator.next(); //problems with keys and data lists
            String colorName = colorElement.getValue();
            if (!colorsMap.containsKey(colorName)) {
                dataIterator.next();
                continue;
            }
            Element dataElement = (Element)dataIterator.next();
            String data = dataElement.getValue();
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] decoded = Base64.getMimeDecoder().decode(dataBytes);
            String decodedString = new String(decoded, StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("\\d(\\.\\d+)? \\d(\\.\\d+)? \\d(\\.\\d+)?");
            Matcher matcher = pattern.matcher(decodedString);
            boolean found = false;
            String colorFloats = "";
            while (matcher.find()) {
                colorFloats = matcher.group();
                found = true;
            }
            if (!found) return null;
            splitting = colorFloats.split(" ");
            float red = Float.parseFloat(splitting[0]);
            float green = Float.parseFloat(splitting[1]);
            float blue = Float.parseFloat(splitting[2]);
            color = new Color(red, green, blue);
            key = colorsMap.get(colorName);
            if (colorName.equals("BackgroundColor")) {
                newScheme.setColor((ColorKey)key, color);
            }
            else {
                if (colorName.equals("TextColor")) {
                    f = AttributesFlyweight.create(color, null, 0, color, EffectType.LINE_UNDERSCORE, null);
                    attrs = TextAttributes.fromFlyweight(f);
                    newScheme.setAttributes(ConsoleViewContentType.ERROR_OUTPUT_KEY, attrs);
                }
                f = AttributesFlyweight.create(color, null, 0, null, null, null);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes((TextAttributesKey)key, attrs);
            }
        }
        return newScheme;
    }
}

import com.intellij.execution.process.ColoredOutputTypeRegistry;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.AttributesFlyweight;
import com.intellij.openapi.editor.colors.impl.EditorColorsManagerImpl;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom.Element;

public class ImportConsoleColorScheme implements SchemeImporter<EditorColorsScheme> {

    public ImportConsoleColorScheme() {
    }

    @NotNull
    @Override
    public String[] getSourceExtensions() {
        return new String[]{"reg", "colorscheme", "config", "itermcolors", "terminal"};
    }


    private Map<String, Comparable> initColorsMap() {
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
        if (extension.equals("reg")) {
            try {
                newScheme = parseRegFile(selectedFile, newScheme);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (extension.equals("colorscheme")) {
            newScheme = parseColorschemeFile(selectedFile, newScheme);
        }
        if (extension.equals("config")) {
            try {
                newScheme = parseConfigFile(selectedFile, newScheme);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (extension.equals("itermcolors")) {
            newScheme = parseItermcolorsFile(selectedFile, newScheme);
        }
        if (extension.equals("terminal")) {
            newScheme = parseTerminalFile(selectedFile, newScheme);
        }
        return newScheme;
    }

    private EditorColorsScheme parseRegFile(VirtualFile selectedFile, EditorColorsScheme newScheme) throws IOException, SchemeImportException {
//        Path path = Paths.get(selectedFile.getPath());
//        List<String> lines;
//        try {
//            lines = Files.readAllLines(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//        Color color;
//        String[] splitting;
//        String[] colorNums;
//        Object[] linesArray;
//        AttributesFlyweight f;
//        TextAttributes attrs;
//        TextAttributesKey key;
//        int linesAmount = lines.size();
//        linesArray = lines.toArray();
//        for (int i = 0; i < linesAmount; i++) {
//            for (int j = 0; j < 22; j++) {
//                if (linesArray[i].toString().startsWith("\"Colour" + j)) {
//                    splitting = linesArray[i].toString().split("\"");
//                    colorNums = splitting[3].split(",");
//                    color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
//                    f = AttributesFlyweight.create(color, null, 0, null, null, null);
//                    attrs = TextAttributes.fromFlyweight(f);
//                    if (j != 1 && j != 3 && j != 4 && j != 5 && linesArray[i].toString().substring(8, 9).equals("\"")) {
//                        switch (j) {
//                            case 0: { //Default Foreground
//                                key = ConsoleViewContentType.NORMAL_OUTPUT_KEY;
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 2: { //Default Background
//                                newScheme.setColor(ConsoleViewContentType.CONSOLE_BACKGROUND_KEY, color);
//                                break;
//                            }
//                            case 6: { //Black ANSI output
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(0);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 7: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(8);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 8: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(1);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 9: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(9);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                        }
//                    } else {
//                        switch (j) {
//                            case 10: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(2);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 11: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(10);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 12: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(3);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 13: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(11);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 14: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(4);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 15: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(12);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 16: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(5);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 17: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(13);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 18: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(6);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 19: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(14);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 20: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(7);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                            case 21: {
//                                key = ColoredOutputTypeRegistry.getAnsiColorKey(15);
//                                newScheme.setAttributes(key, attrs);
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return newScheme;
        Map<String, Comparable> colorsMap = initColorsMap();
        Comparable key;
        AttributesFlyweight f;
        TextAttributes attrs;
        InputStream input = selectedFile.getInputStream();
        InputStreamReader isr = new InputStreamReader(input);
        RegColorsLexer ccl = new RegColorsLexer(isr);
        ColorType colorType = ccl.yylex();
        while(colorType != null) {
            key = colorsMap.get(colorType.getColorIdentifier());
            if (colorType.getColorIdentifier().equals(ColorType.BACKGROUND))
                newScheme.setColor((ColorKey)key, colorType.getColor());
            else {
                f = AttributesFlyweight.create(colorType.getColor(), null, 0, null, null, null);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes((TextAttributesKey)key, attrs);
            }
            colorType = ccl.yylex();
        }
        return newScheme;
    }


    private EditorColorsScheme parseColorschemeFile(VirtualFile selectedFile, EditorColorsScheme newScheme) {
        Path path = Paths.get(selectedFile.getPath());
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Color color;
        String[] splitting;
        String[] colorNums;
        Object[] linesArray;
        AttributesFlyweight f;
        TextAttributes attrs;
        TextAttributesKey key;
        int linesAmount = lines.size();
        linesArray = lines.toArray();
        for (int i = 0; i < linesAmount; i++) {
            if (linesArray[i].toString().startsWith("[Background]")) {
                i++;
                splitting = linesArray[i].toString().split("=");
                colorNums = splitting[1].split(",");
                color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                newScheme.setColor(ConsoleViewContentType.CONSOLE_BACKGROUND_KEY, color);
            } else if (linesArray[i].toString().startsWith("[Foreground]")) {
                i++;
                splitting = linesArray[i].toString().split("=");
                colorNums = splitting[1].split(",");
                color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                f = AttributesFlyweight.create(color, null, 0, null, null, null);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes(ConsoleViewContentType.NORMAL_OUTPUT_KEY, attrs);
            } else if (linesArray[i].toString().startsWith("[Color")) {
                for (int j = 0; j < 8; j++) {
                    if (linesArray[i].toString().startsWith("[Color" + j)) {
                        i++;
                        splitting = linesArray[i].toString().split("=");
                        colorNums = splitting[1].split(",");
                        color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                        f = AttributesFlyweight.create(color, null, 0, null, null, null);
                        attrs = TextAttributes.fromFlyweight(f);
                        if (linesArray[i - 1].toString().startsWith("[Color" + j + "]")) {
                            key = ColoredOutputTypeRegistry.getAnsiColorKey(j);
                            newScheme.setAttributes(key, attrs);
                        } else if (linesArray[i - 1].toString().startsWith("[Color" + j + "Intense]")) {
                            key = ColoredOutputTypeRegistry.getAnsiColorKey(j + 8);
                            newScheme.setAttributes(key, attrs);
                        }
                    }
                }
            }
        }
        return newScheme;
    }
//known bug: .colorscheme parsing replaces default scheme

    private EditorColorsScheme parseConfigFile(VirtualFile selectedFile, EditorColorsScheme newScheme) throws IOException, SchemeImportException {
        Map<String, Comparable> colorsMap = initColorsMap();
        Comparable key;
        AttributesFlyweight f;
        TextAttributes attrs;
        InputStream input = selectedFile.getInputStream();
        InputStreamReader isr = new InputStreamReader(input);
        ConfigColorsLexer ccl = new ConfigColorsLexer(isr);
        ColorType colorType = ccl.yylex();
        while(colorType != null) {
            key = colorsMap.get(colorType.getColorIdentifier());
            if (colorType.getColorIdentifier().equals(ColorType.BACKGROUND))
                newScheme.setColor((ColorKey)key, colorType.getColor());
            else {
                f = AttributesFlyweight.create(colorType.getColor(), null, 0, null, null, null);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes((TextAttributesKey)key, attrs);
            }
            colorType = ccl.yylex();
        }
        return newScheme;
    }


    private EditorColorsScheme parseItermcolorsFile(VirtualFile selectedFile, EditorColorsScheme newScheme) throws SchemeImportException {
        float red = -1, blue = -1, green = -1;
        int ansiColorNumber;
        Color color;
        AttributesFlyweight f;
        TextAttributes attrs;
        TextAttributesKey key;
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
            String ansiKeyName = ansiKey.getValue();
            if (!ansiKeyName.startsWith("Ansi") && !ansiKeyName.startsWith("Background") && !ansiKeyName.startsWith("Foreground"))
                continue;
            Element colorDict = (Element) colorsIterator.next();
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
                blue = red = green = -1;
                continue;
            }
            color = new Color(red, green, blue);
            if (ansiKeyName.startsWith("Ansi")) {
                if (ansiKeyName.substring(6, 7).equals(" ")) {
                    ansiColorNumber = Integer.parseInt(ansiKeyName.substring(5, 6));
                } else {
                    ansiColorNumber = Integer.parseInt(ansiKeyName.substring(5, 7));
                }
                f = AttributesFlyweight.create(color, null, 0, null, null, null);
                key = ColoredOutputTypeRegistry.getAnsiColorKey(ansiColorNumber);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes(key, attrs);
            } else if (ansiKeyName.startsWith("Background")) {
                newScheme.setColor(ConsoleViewContentType.CONSOLE_BACKGROUND_KEY, color);
            } else if (ansiKeyName.startsWith("Foreground")) {
                f = AttributesFlyweight.create(color, null, 0, null, null, null);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes(ConsoleViewContentType.NORMAL_OUTPUT_KEY, attrs);
            }
        }
        return newScheme;
    }

// добавить обработку исключений
    private EditorColorsScheme parseTerminalFile(VirtualFile selectedFile, EditorColorsScheme newScheme) throws SchemeImportException {
        Color color;
        AttributesFlyweight f;
        TextAttributes attrs;
        TextAttributesKey key = null;
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
            if (!colorName.startsWith("ANSI") && !colorName.startsWith("BackgroundColor") && !colorName.startsWith("TextColor")) {
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
            if (colorName.startsWith("ANSI")) {
                f = AttributesFlyweight.create(color, null, 0, null, null, null);
                if (colorName.startsWith("ANSIBlack")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(0);
                }
                else if (colorName.startsWith("ANSIBlue")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(4);
                }
                else if (colorName.startsWith("ANSIBrightBlack")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(8);
                }
                else if (colorName.startsWith("ANSIBrightBlue")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(12);
                }
                else if (colorName.startsWith("ANSIBrightCyan")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(14);
                }
                else if (colorName.startsWith("ANSIBrightGreen")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(10);
                }
                else if (colorName.startsWith("ANSIBrightMagenta")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(13);
                }
                else if (colorName.startsWith("ANSIBrightRed")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(9);
                }
                else if (colorName.startsWith("ANSIBrightWhite")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(15);
                }
                else if (colorName.startsWith("ANSIBrightYellow")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(11);
                }
                else if (colorName.startsWith("ANSICyan")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(6);
                }
                else if (colorName.startsWith("ANSIGreen")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(2);
                }
                else if (colorName.startsWith("ANSIMagenta")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(5);
                }
                else if (colorName.startsWith("ANSIRed")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(1);
                }
                else if (colorName.startsWith("ANSIWhite")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(7);
                }
                else if (colorName.startsWith("ANSIYellow")) {
                    key = ColoredOutputTypeRegistry.getAnsiColorKey(3);
                }
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes(key, attrs);
            }
            else if (colorName.startsWith("BackgroundColor")) {
                newScheme.setColor(ConsoleViewContentType.CONSOLE_BACKGROUND_KEY, color);
            }
            else if (colorName.startsWith("TextColor")) {
                f = AttributesFlyweight.create(color, null, 0, null, null, null);
                attrs = TextAttributes.fromFlyweight(f);
                newScheme.setAttributes(ConsoleViewContentType.NORMAL_OUTPUT_KEY, attrs);
            }
        }
        return newScheme;
    }
}

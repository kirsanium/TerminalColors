import Lexers.*;
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
import com.intellij.openapi.util.NlsContexts;
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

public class TerminalColorSchemeImporter implements SchemeImporter<EditorColorsScheme> {

    public TerminalColorSchemeImporter() {
    }

    @NotNull
    @Override
    public String[] getSourceExtensions() {
        return new String[]{"reg", "colorscheme", "config", "itermcolors", "terminal"};
    }

    @Override
    public @Nullable EditorColorsScheme importScheme(
            @NotNull Project project,
            @NotNull VirtualFile selectedFile,
            @NotNull EditorColorsScheme currentScheme,
            @NotNull SchemeFactory<? extends EditorColorsScheme> schemeFactory
    ) throws SchemeImportException {
        String currentSchemeName = currentScheme.getName();
        String terminalSchemeName = selectedFile.getNameWithoutExtension();
        String schemeExtension = selectedFile.getExtension();
        EditorColorsManagerImpl editorColorsManager = (EditorColorsManagerImpl) EditorColorsManagerImpl.getInstance();
        EditorColorsScheme currentGlobalScheme = (EditorColorsScheme) editorColorsManager.getGlobalScheme().clone();
        EditorColorsScheme newScheme = null;
        switch (Objects.requireNonNull(schemeExtension)) {
            case "reg":
            case "colorscheme":
            case "config":
                try {
                    newScheme = parseRegConfigColorschemeFile(selectedFile, currentGlobalScheme, schemeExtension);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new SchemeImportException(e);
                }
                break;
            case "itermcolors":
                newScheme = parseItermcolorsFile(selectedFile, currentGlobalScheme);
                break;
            case "terminal":
                newScheme = parseTerminalFile(selectedFile, currentGlobalScheme);
                break;
            default:
                throw new SchemeImportException("Wrong extension");
        }
        if (newScheme == null) {
            throw new SchemeImportException("Failed to import scheme");
        }

        if (currentSchemeName.contains("+Terminal")) {
            String[] splitCurrentSchemeName = currentSchemeName.split("\\+Terminal");
            currentSchemeName = splitCurrentSchemeName[0];
        }

        newScheme.setName(currentSchemeName + "+Terminal" + terminalSchemeName);
        return newScheme;
    }

    private EditorColorsScheme parseRegConfigColorschemeFile(
            VirtualFile selectedFile,
            EditorColorsScheme currentScheme,
            String extension
    ) throws IOException, SchemeImportException {
        Map<String, Comparable> colorsMap = createColorTypeMap();
        InputStream inputStream = selectedFile.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        ColorsLexer colorsLexer;
        switch (extension) {
            case "reg":
                colorsLexer = new RegColorsLexer(inputStreamReader);
                break;
            case "colorscheme":
                colorsLexer = new ColorschemeColorsLexer(inputStreamReader);
                break;
            case "config":
                colorsLexer = new ConfigColorsLexer(inputStreamReader);
                break;
            default:
                throw new SchemeImportException("Wrong extension");
        }

        EditorColorsScheme newScheme = (EditorColorsScheme) currentScheme.clone();

        ColorType colorType = colorsLexer.yylex();

        while (colorType != null) {
            Color color = colorType.getColor();
            String colorIdentifier = colorType.getColorIdentifier();
            Comparable key = colorsMap.get(colorIdentifier);
            if (colorIdentifier.equals(ColorType.BACKGROUND)) {
                newScheme.setColor((ColorKey) key, color);
            } else {
                if (colorIdentifier.equals(ColorType.FOREGROUND)) {
                    setErrorOutputTextAttributes(newScheme, color);
                }
                boolean isAnsi = false;
                for (String colorName: ColorType.ANSI) {
                    if (colorIdentifier.equals(colorName)) {
                        isAnsi = true;
                        break;
                    }
                }
                if (isAnsi) setTextAttributesByKey(newScheme, (TextAttributesKey) key, color);
            }
            colorType = colorsLexer.yylex();
        }
        return newScheme;
    }

    private Map<String, Comparable> createColorTypeMap() {
        Map<String, Comparable> colorsMap = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            colorsMap.put(ColorType.ANSI[i], ColoredOutputTypeRegistry.getAnsiColorKey(i));
        }
        colorsMap.put(ColorType.BACKGROUND, ConsoleViewContentType.CONSOLE_BACKGROUND_KEY);
        colorsMap.put(ColorType.FOREGROUND, ConsoleViewContentType.NORMAL_OUTPUT_KEY);
        return colorsMap;
    }

    private EditorColorsScheme parseItermcolorsFile(
            VirtualFile selectedFile,
            EditorColorsScheme newScheme
    ) throws SchemeImportException {
        Map<String, Comparable> colorsMap = createItermcolorsMap();

        float red = -1, blue = -1, green = -1;

        Element root = SchemeImportUtil.loadSchemeDom(selectedFile);
        Element dict = root.getChild("dict");
        List<Element> ansiKeys = dict.getChildren("key");
        List<Element> colors = dict.getChildren("dict");
        if (colors.isEmpty() || ansiKeys.isEmpty() || ansiKeys.size() != colors.size()) {
            throw new InvalidDataException("Scheme is not valid");
        }
        Iterator<Element> ansiKeysIterator = ansiKeys.iterator();
        Iterator<Element> colorsIterator = colors.iterator();

        while (ansiKeysIterator.hasNext()) {
            Element ansiKey = ansiKeysIterator.next();
            Element colorDict = colorsIterator.next();
            String ansiKeyName = ansiKey.getValue();

            if (!colorsMap.containsKey(ansiKeyName))
                continue;

            List<Element> colorDictEntries = colorDict.getChildren();
            Iterator<Element> colorDictEntriesIterator = colorDictEntries.iterator();

            while (colorDictEntriesIterator.hasNext()) {
                Element colorComponentName = colorDictEntriesIterator.next();
                Element floatNumElem = colorDictEntriesIterator.next();
                String floatNumStr = floatNumElem.getValue();
                switch (colorComponentName.getValue()) {
                    case "Blue Component":
                        blue = Float.parseFloat(floatNumStr);
                        break;
                    case "Green Component":
                        green = Float.parseFloat(floatNumStr);
                        break;
                    case "Red Component":
                        red = Float.parseFloat(floatNumStr);
                        break;
                }
            }

            if (blue == -1 || red == -1 || green == -1) {
                throw new SchemeImportException("Scheme is not valid");
            }

            Color color = new Color(red, green, blue);
            Comparable key = colorsMap.get(ansiKeyName);
            if (ansiKeyName.equals("Background Color")) {
                newScheme.setColor((ColorKey) key, color);
            } else {
                if (ansiKeyName.equals("Foreground Color")) {
                    setErrorOutputTextAttributes(newScheme, color);
                }
                setTextAttributesByKey(newScheme, (TextAttributesKey) key, color);
            }
        }
        return newScheme;
    }

    private Map<String, Comparable> createItermcolorsMap() {
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

        return colorsMap;
    }

    // добавить обработку исключений
    private EditorColorsScheme parseTerminalFile(
            VirtualFile selectedFile,
            EditorColorsScheme newScheme
    ) throws SchemeImportException {
        Map<String, Comparable> colorsMap = createTerminalColorsMap();
        Element root = SchemeImportUtil.loadSchemeDom(selectedFile);
        Element dict = root.getChild("dict");
        List<Element> dataList = dict.getChildren();
        if (dataList.isEmpty()) {
            throw new InvalidDataException("Scheme is not valid; no data");
        }

        Iterator<Element> dataIterator = dataList.iterator();
        while (dataIterator.hasNext()) {
            Element colorElement = dataIterator.next(); //problems with keys and data lists
            String colorName = colorElement.getValue();
            if (!colorsMap.containsKey(colorName)) {
                dataIterator.next();
                continue;
            }
            Element dataElement = dataIterator.next();
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
            String[] splitting = colorFloats.split(" ");
            float red = Float.parseFloat(splitting[0]);
            float green = Float.parseFloat(splitting[1]);
            float blue = Float.parseFloat(splitting[2]);
            Color color = new Color(red, green, blue);
            Comparable key = colorsMap.get(colorName);
            if (colorName.equals("BackgroundColor")) {
                newScheme.setColor((ColorKey) key, color);
            } else {
                if (colorName.equals("TextColor")) {
                    setErrorOutputTextAttributes(newScheme, color);
                }
                setTextAttributesByKey(newScheme, (TextAttributesKey) key, color);
            }
        }
        return newScheme;
    }

    private Map<String, Comparable> createTerminalColorsMap() {
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

        return colorsMap;
    }

    private void setErrorOutputTextAttributes(EditorColorsScheme scheme, Color color) {
        AttributesFlyweight attributesFlyweight = AttributesFlyweight.create(
                color, null, 0,
                color, EffectType.LINE_UNDERSCORE, null);
        TextAttributes textAttributes = TextAttributes.fromFlyweight(attributesFlyweight);
        scheme.setAttributes(ConsoleViewContentType.ERROR_OUTPUT_KEY, textAttributes);
    }

    private void setTextAttributesByKey(EditorColorsScheme scheme, TextAttributesKey key, Color color) {
        AttributesFlyweight attributesFlyweight = AttributesFlyweight.create(
                color, null, 0,
                null, null, null);
        TextAttributes textAttributes = TextAttributes.fromFlyweight(attributesFlyweight);
        scheme.setAttributes(key, textAttributes);
    }
}

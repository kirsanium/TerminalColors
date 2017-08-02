import com.intellij.execution.process.ColoredOutputTypeRegistry;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.AttributesFlyweight;
import com.intellij.openapi.editor.colors.impl.EditorColorsManagerImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.SchemeFactory;
import com.intellij.openapi.options.SchemeImportException;
import com.intellij.openapi.options.SchemeImporter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ImportConsoleColorScheme implements SchemeImporter<EditorColorsScheme> {

    public ImportConsoleColorScheme() {
    }

    @NotNull
    @Override
    public String[] getSourceExtensions() {
        return new String[]{"reg", "colorscheme"};
    }


    @Nullable
    @Override
    public EditorColorsScheme importScheme(@NotNull Project project, @NotNull VirtualFile selectedFile, @NotNull EditorColorsScheme currentScheme, @NotNull SchemeFactory<EditorColorsScheme> schemeFactory) throws SchemeImportException {
        Path path = Paths.get(selectedFile.getPath());
        String name = selectedFile.getNameWithoutExtension();
        String extension = selectedFile.getExtension();
        EditorColorsManagerImpl manager = (EditorColorsManagerImpl) EditorColorsManagerImpl.getInstance();
        EditorColorsScheme newScheme = manager.getGlobalScheme();
        newScheme.setName(name);
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (extension.equals("reg")) {
            newScheme = parseRegFile(lines, newScheme);
        }
        if (extension.equals("colorscheme")) {
            newScheme = parseColorschemeFile(lines, newScheme);
        }
        return newScheme;
    }

    private EditorColorsScheme parseRegFile(List<String> lines, EditorColorsScheme newScheme) {
        Color color;
        String[] splitting;
        String[] colorNums;
        Object[] linesArray;
        AttributesFlyweight f;
        TextAttributes attrs = null;
        TextAttributesKey key = null;
        int linesAmount = lines.size();
        linesArray = lines.toArray();
        for (int i = 0; i < linesAmount; i++) {
            for (int j = 0; j < 22; j++) {
                if (linesArray[i].toString().startsWith("\"Colour" + j)) {
                    splitting = linesArray[i].toString().split("\"");
                    colorNums = splitting[3].split(",");
                    color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                    f = AttributesFlyweight.create(color, null, 0, null, null, null);
                    if (j != 1 && j != 3 && j != 4 && j != 5 && linesArray[i].toString().substring(8, 9).equals("\"")) {
                        switch (j) {
                            case 0: { //Default Foreground
                                key = ConsoleViewContentType.NORMAL_OUTPUT_KEY;
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 2: { //Default Background
                                newScheme.setColor(ConsoleViewContentType.CONSOLE_BACKGROUND_KEY, color);
                                break;
                            }
                            case 6: { //Black ANSI output
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(0);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 7: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(8);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 8: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(1);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 9: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(9);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                        }
                    } else {
                        switch (j) {
                            case 10: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(2);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 11: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(10);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 12: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(3);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 13: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(11);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 14: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(4);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 15: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(12);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 16: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(5);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 17: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(13);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 18: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(6);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 19: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(14);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 20: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(7);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                            case 21: {
                                key = ColoredOutputTypeRegistry.getAnsiColorKey(15);
                                attrs = TextAttributes.fromFlyweight(f);
                                newScheme.setAttributes(key, attrs);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return newScheme;
    }

    private EditorColorsScheme parseColorschemeFile(List<String> lines, EditorColorsScheme newScheme) {
        Color color;
        String[] splitting;
        String[] colorNums;
        Object[] linesArray;
        AttributesFlyweight f;
        TextAttributes attrs = null;
        TextAttributesKey key = null;
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
            } else {
                for (int j = 0; j < 8; j++) {
                    if (linesArray[i].toString().startsWith("[Color" + j)) {
                        i++;
                        splitting = linesArray[i].toString().split("=");
                        colorNums = splitting[1].split(",");
                        color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                        f = AttributesFlyweight.create(color, null, 0, null, null, null);
                        if (linesArray[i].toString().substring(7, 8).equals("]")) {
                            switch (j) {
                                case 0: { //Default Foreground
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(0);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 1: { //Default Background
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(1);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 2: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(2);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 3: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(3);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 4: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(4);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 5: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(5);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 6: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(6);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 7: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(7);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                            }
                        } else {
                            switch (j) {
                                case 0: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(8);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 1: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(9);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 2: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(10);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 3: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(11);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 4: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(12);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 5: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(13);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 6: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(14);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                                case 7: {
                                    key = ColoredOutputTypeRegistry.getAnsiColorKey(15);
                                    attrs = TextAttributes.fromFlyweight(f);
                                    newScheme.setAttributes(key, attrs);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return newScheme;
    }

    private EditorColorsScheme parseConfigFile(List<String> lines, EditorColorsScheme newScheme) {
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
            if (linesArray[i].toString().contains("palette")) {
                splitting = linesArray[i].toString().split("#");
                for (int j = 0; j < 16; j++) {
                    color = new Color (Integer.parseInt(splitting[i+1].substring(0,6), 16));
                }
            }
        }
        return newScheme;
    }
}


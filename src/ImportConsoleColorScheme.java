import com.intellij.execution.process.ColoredOutputTypeRegistry;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.impl.EditorColorsManagerImpl;
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

//    @Override
//    public void actionPerformed(AnActionEvent anActionEvent) {
//
//    }

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
        Object[] colors;
        int linesAmount = lines.size();
        colors = lines.toArray();
        for (int i = 0; i < linesAmount; i++) {
            for (int j = 0; j < 22; j++) {
                if (colors[i].toString().startsWith("\"Colour" + j)) {
                    splitting = colors[i].toString().split("\"");
                    colorNums = splitting[3].split(",");
                    color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                    if (j != 1 && j != 3 && j != 4 && j != 5 && colors[i].toString().substring(8, 9).equals("\"")) {
                        switch (j) {
                            case 0: { //Default Foreground
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(16)).setForegroundColor(color);
                                break;
                            }
                            case 2: { //Default Background
                                newScheme.setColor(ConsoleViewContentType.CONSOLE_BACKGROUND_KEY, color);
                                break;
                            }
                            case 6: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(0)).setForegroundColor(color);
                                break;
                            }
                            case 7: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(8)).setForegroundColor(color);
                                break;
                            }
                            case 8: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(1)).setForegroundColor(color);
                                break;
                            }
                            case 9: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(9)).setForegroundColor(color);
                                break;
                            }
                        }
                    } else {
                        switch (j) {
                            case 10: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(2)).setForegroundColor(color);
                                break;
                            }
                            case 11: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(10)).setForegroundColor(color);
                                break;
                            }
                            case 12: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(3)).setForegroundColor(color);
                                break;
                            }
                            case 13: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(11)).setForegroundColor(color);
                                break;
                            }
                            case 14: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(4)).setForegroundColor(color);
                                break;
                            }
                            case 15: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(12)).setForegroundColor(color);
                                break;
                            }
                            case 16: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(5)).setForegroundColor(color);
                                break;
                            }
                            case 17: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(13)).setForegroundColor(color);
                                break;
                            }
                            case 18: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(6)).setForegroundColor(color);
                                break;
                            }
                            case 19: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(14)).setForegroundColor(color);
                                break;
                            }
                            case 20: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(7)).setForegroundColor(color);
                                break;
                            }
                            case 21: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(15)).setForegroundColor(color);
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
        Object[] colors;
        int linesAmount = lines.size();
        colors = lines.toArray();
        for (int i = 0; i < linesAmount; i++) {
            if (colors[i].toString().startsWith("[Background]")) {
                i++;
                splitting = colors[i].toString().split("=");
                colorNums = splitting[1].split(",");
                color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                newScheme.setColor(ConsoleViewContentType.CONSOLE_BACKGROUND_KEY, color);
            } else if (colors[i].toString().startsWith("[Foreground]")) {
                i++;
                splitting = colors[i].toString().split("=");
                colorNums = splitting[1].split(",");
                color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                newScheme.getAttributes(ConsoleViewContentType.NORMAL_OUTPUT_KEY).setForegroundColor(color);
            } else {
                for (int j = 0; j < 8; j++) {
                    if (colors[i].toString().startsWith("[Color" + j)) {
                        i++;
                        splitting = colors[i].toString().split("=");
                        colorNums = splitting[1].split(",");
                        color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                        if (colors[i].toString().substring(7, 8).equals("]")) {
                            switch (j) {
                                case 0: { //Default Foreground
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(0)).setForegroundColor(color);
                                    break;
                                }
                                case 1: { //Default Background
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(1)).setForegroundColor(color);
                                    break;
                                }
                                case 2: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(10)).setForegroundColor(color);
                                    break;
                                }
                                case 3: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(11)).setForegroundColor(color);
                                    break;
                                }
                                case 4: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(12)).setForegroundColor(color);
                                    break;
                                }
                                case 5: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(13)).setForegroundColor(color);
                                    break;
                                }
                                case 6: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(14)).setForegroundColor(color);
                                    break;
                                }
                                case 7: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(15)).setForegroundColor(color);
                                    break;
                                }
                            }
                        } else {
                            switch (j) {
                                case 0: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(2)).setForegroundColor(color);
                                    break;
                                }
                                case 1: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(3)).setForegroundColor(color);
                                    break;
                                }
                                case 2: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(4)).setForegroundColor(color);
                                    break;
                                }
                                case 3: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(5)).setForegroundColor(color);
                                    break;
                                }
                                case 4: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(6)).setForegroundColor(color);
                                    break;
                                }
                                case 5: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(7)).setForegroundColor(color);
                                    break;
                                }
                                case 6: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(8)).setForegroundColor(color);
                                    break;
                                }
                                case 7: {
                                    newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(9)).setForegroundColor(color);
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
        Object[] colors;
        int linesAmount = lines.size();
        colors = lines.toArray();
        for (int i = 0; i < linesAmount; i++) {
            for (int j = 0; j < 22; j++) {
                if (colors[i].toString().startsWith("\"Colour" + j)) {
                    splitting = colors[i].toString().split("\"");
                    colorNums = splitting[3].split(",");
                    color = new Color(Integer.parseInt(colorNums[0]), Integer.parseInt(colorNums[1]), Integer.parseInt(colorNums[2]));
                    if (j != 1 && j != 3 && j != 4 && j != 5 && colors[i].toString().substring(8, 9).equals("\"")) {
                        switch (j) {
                            case 0: { //Default Foreground
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(16)).setForegroundColor(color);
                                break;
                            }
                            case 2: { //Default Background
                                newScheme.setColor(ConsoleViewContentType.CONSOLE_BACKGROUND_KEY, color);
                                break;
                            }
                            case 6: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(0)).setForegroundColor(color);
                                break;
                            }
                            case 7: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(8)).setForegroundColor(color);
                                break;
                            }
                            case 8: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(1)).setForegroundColor(color);
                                break;
                            }
                            case 9: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(9)).setForegroundColor(color);
                                break;
                            }
                        }
                    } else {
                        switch (j) {
                            case 10: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(2)).setForegroundColor(color);
                                break;
                            }
                            case 11: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(10)).setForegroundColor(color);
                                break;
                            }
                            case 12: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(3)).setForegroundColor(color);
                                break;
                            }
                            case 13: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(11)).setForegroundColor(color);
                                break;
                            }
                            case 14: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(4)).setForegroundColor(color);
                                break;
                            }
                            case 15: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(12)).setForegroundColor(color);
                                break;
                            }
                            case 16: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(5)).setForegroundColor(color);
                                break;
                            }
                            case 17: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(13)).setForegroundColor(color);
                                break;
                            }
                            case 18: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(6)).setForegroundColor(color);
                                break;
                            }
                            case 19: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(14)).setForegroundColor(color);
                                break;
                            }
                            case 20: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(7)).setForegroundColor(color);
                                break;
                            }
                            case 21: {
                                newScheme.getAttributes(ColoredOutputTypeRegistry.getAnsiColorKey(15)).setForegroundColor(color);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return newScheme;
    }
}


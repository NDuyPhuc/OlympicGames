package com.duyphuc.olympics.util;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThemeManager {

    private static ThemeManager instance;

    public enum Theme {
        LIGHT("Light Mode"),
        DARK("Dark Mode"),
        HIGH_CONTRAST("High Contrast Mode");

        private final String displayName;
        Theme(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private Theme currentTheme = Theme.LIGHT;
    private final List<Scene> managedScenes = new ArrayList<>();

    private final String baseCssPath = "/com/duyphuc/olympics/css/";
    private final String lightThemeFile = "light-theme.css";
    private final String darkThemeFile = "dark-theme.css";
    private final String highContrastThemeFile = "high-contrast-theme.css";

    private ThemeManager() {}

    public static synchronized ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    public void addManagedScene(Scene scene) {
        if (scene != null && !managedScenes.contains(scene)) {
            managedScenes.add(scene);
            applyThemeToScene(scene);
        }
    }

    public void removeManagedScene(Scene scene) {
        managedScenes.remove(scene);
    }

    public void setTheme(Theme theme) {
        if (theme != null) {
            this.currentTheme = theme;
            System.out.println("Theme changed to: " + theme.getDisplayName());
            for (Scene scene : managedScenes) {
                applyThemeToScene(scene);
            }
        }
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    private void applyThemeToScene(Scene scene) {
        if (scene == null) return;

        scene.getStylesheets().removeIf(s ->
                s.endsWith(lightThemeFile) ||
                s.endsWith(darkThemeFile) ||
                s.endsWith(highContrastThemeFile)
        );

        String themeCssPath = null;
        switch (currentTheme) {
            case DARK:
                themeCssPath = Objects.requireNonNull(getClass().getResource(baseCssPath + darkThemeFile)).toExternalForm();
                break;
            case HIGH_CONTRAST:
                // themeCssPath = Objects.requireNonNull(getClass().getResource(baseCssPath + highContrastThemeFile)).toExternalForm();
                break;
            case LIGHT:
            default:
                themeCssPath = Objects.requireNonNull(getClass().getResource(baseCssPath + lightThemeFile)).toExternalForm();
                break;
        }

        if (themeCssPath != null) {
            scene.getStylesheets().add(themeCssPath); // Chỉ load 1 file theme
            System.out.println("Applied theme styles: " + themeCssPath + " to scene: " + scene.hashCode());
        }

        if (scene.getWindow() instanceof Stage) {
            Stage stage = (Stage) scene.getWindow();
            if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                String rootStyleClass = "theme-" + currentTheme.name().toLowerCase();
                stage.getScene().getRoot().getStyleClass().removeIf(s -> s.startsWith("theme-"));
                stage.getScene().getRoot().getStyleClass().add(rootStyleClass);
                System.out.println("Set root style class: " + rootStyleClass);
            }
        }
    }

    public void toggleTheme() {
        if (currentTheme == Theme.LIGHT) setTheme(Theme.DARK);
        else setTheme(Theme.LIGHT);
    }
}
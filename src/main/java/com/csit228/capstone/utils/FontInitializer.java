package com.csit228.capstone.utils;

import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class FontInitializer {
    private static final String FONT_RESOURCE_PATH = "/com/csit228/capstone/fonts/";
    private static final List<String> FONT_FILES = List.of(
            "Inter_18pt-Black.ttf",
            "Inter_18pt-BlackItalic.ttf",
            "Inter_18pt-Bold.ttf",
            "Inter_18pt-BoldItalic.ttf",
            "Inter_18pt-ExtraBold.ttf",
            "Inter_18pt-ExtraBoldItalic.ttf",
            "Inter_18pt-ExtraLight.ttf",
            "Inter_18pt-ExtraLightItalic.ttf",
            "Inter_18pt-Italic.ttf",
            "Inter_18pt-Light.ttf",
            "Inter_18pt-LightItalic.ttf",
            "Inter_18pt-Medium.ttf",
            "Inter_18pt-MediumItalic.ttf",
            "Inter_18pt-Regular.ttf",
            "Inter_18pt-SemiBold.ttf",
            "Inter_18pt-SemiBoldItalic.ttf",
            "Inter_18pt-Thin.ttf",
            "Inter_18pt-ThinItalic.ttf",
            "Inter_24pt-Black.ttf",
            "Inter_24pt-BlackItalic.ttf",
            "Inter_24pt-Bold.ttf",
            "Inter_24pt-BoldItalic.ttf",
            "Inter_24pt-ExtraBold.ttf",
            "Inter_24pt-ExtraBoldItalic.ttf",
            "Inter_24pt-ExtraLight.ttf",
            "Inter_24pt-ExtraLightItalic.ttf",
            "Inter_24pt-Italic.ttf",
            "Inter_24pt-Light.ttf",
            "Inter_24pt-LightItalic.ttf",
            "Inter_24pt-Medium.ttf",
            "Inter_24pt-MediumItalic.ttf",
            "Inter_24pt-Regular.ttf",
            "Inter_24pt-SemiBold.ttf",
            "Inter_24pt-SemiBoldItalic.ttf",
            "Inter_24pt-Thin.ttf",
            "Inter_24pt-ThinItalic.ttf",
            "Inter_28pt-Black.ttf",
            "Inter_28pt-BlackItalic.ttf",
            "Inter_28pt-Bold.ttf",
            "Inter_28pt-BoldItalic.ttf",
            "Inter_28pt-ExtraBold.ttf",
            "Inter_28pt-ExtraBoldItalic.ttf",
            "Inter_28pt-ExtraLight.ttf",
            "Inter_28pt-ExtraLightItalic.ttf",
            "Inter_28pt-Italic.ttf",
            "Inter_28pt-Light.ttf",
            "Inter_28pt-LightItalic.ttf",
            "Inter_28pt-Medium.ttf",
            "Inter_28pt-MediumItalic.ttf",
            "Inter_28pt-Regular.ttf",
            "Inter_28pt-SemiBold.ttf",
            "Inter_28pt-SemiBoldItalic.ttf",
            "Inter_28pt-Thin.ttf",
            "Inter_28pt-ThinItalic.ttf"
    );

    private static boolean initialized;

    private FontInitializer() {
    }

    public static synchronized void initializeFonts() {
        if (initialized) {
            return;
        }

        for (String fontFile : FONT_FILES) {
            loadFont(fontFile);
        }

        initialized = true;
    }

    private static void loadFont(String fontFile) {
        try (InputStream inputStream = FontInitializer.class.getResourceAsStream(FONT_RESOURCE_PATH + fontFile)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing font resource: " + FONT_RESOURCE_PATH + fontFile);
            }
            Font.loadFont(inputStream, 12);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load font resource: " + FONT_RESOURCE_PATH + fontFile, e);
        }
    }
}


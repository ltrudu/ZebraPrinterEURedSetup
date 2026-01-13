package com.zebra.zebraprintereuredsetup;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    public static final String LANGUAGE_SYSTEM = "system";
    public static final String LANGUAGE_ARABIC = "ar";
    public static final String LANGUAGE_GERMAN = "de";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_SPANISH = "es";
    public static final String LANGUAGE_FRENCH = "fr";
    public static final String LANGUAGE_HINDI = "hi";
    public static final String LANGUAGE_ITALIAN = "it";
    public static final String LANGUAGE_PORTUGUESE = "pt";
    public static final String LANGUAGE_URDU = "ur";
    public static final String LANGUAGE_VIETNAMESE = "vi";
    public static final String LANGUAGE_CHINESE = "zh";

    // Sorted alphabetically by locale code (System first, then AR, DE, EN, ES, FR, HI, IT, PT, UR, VI, ZH)
    public static final String[] LANGUAGE_CODES = {
            LANGUAGE_SYSTEM,
            LANGUAGE_ARABIC,
            LANGUAGE_GERMAN,
            LANGUAGE_ENGLISH,
            LANGUAGE_SPANISH,
            LANGUAGE_FRENCH,
            LANGUAGE_HINDI,
            LANGUAGE_ITALIAN,
            LANGUAGE_PORTUGUESE,
            LANGUAGE_URDU,
            LANGUAGE_VIETNAMESE,
            LANGUAGE_CHINESE
    };

    public static Context applyLocale(Context context) {
        String languageCode = SettingsHelper.getLanguage(context);
        return applyLocale(context, languageCode);
    }

    public static Context applyLocale(Context context, String languageCode) {
        Locale locale;
        if (languageCode == null || languageCode.equals(LANGUAGE_SYSTEM)) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            locale = new Locale(languageCode);
        }

        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    public static String getLanguageDisplayName(Context context, String languageCode) {
        if (languageCode == null || languageCode.equals(LANGUAGE_SYSTEM)) {
            return context.getString(R.string.language_system);
        }

        String nativeName = getNativeLanguageName(languageCode);
        return languageCode.toUpperCase() + " - " + nativeName;
    }

    private static String getNativeLanguageName(String languageCode) {
        switch (languageCode) {
            case LANGUAGE_ARABIC:
                return "العربية";
            case LANGUAGE_GERMAN:
                return "Deutsch";
            case LANGUAGE_ENGLISH:
                return "English";
            case LANGUAGE_SPANISH:
                return "Español";
            case LANGUAGE_FRENCH:
                return "Français";
            case LANGUAGE_HINDI:
                return "हिन्दी";
            case LANGUAGE_ITALIAN:
                return "Italiano";
            case LANGUAGE_PORTUGUESE:
                return "Português";
            case LANGUAGE_URDU:
                return "اردو";
            case LANGUAGE_VIETNAMESE:
                return "Tiếng Việt";
            case LANGUAGE_CHINESE:
                return "中文";
            default:
                return languageCode;
        }
    }

    public static String[] getLanguageDisplayNames(Context context) {
        String[] names = new String[LANGUAGE_CODES.length];
        for (int i = 0; i < LANGUAGE_CODES.length; i++) {
            names[i] = getLanguageDisplayName(context, LANGUAGE_CODES[i]);
        }
        return names;
    }

    public static int getLanguageIndex(String languageCode) {
        if (languageCode == null) {
            return 0;
        }
        for (int i = 0; i < LANGUAGE_CODES.length; i++) {
            if (LANGUAGE_CODES[i].equals(languageCode)) {
                return i;
            }
        }
        return 0;
    }
}

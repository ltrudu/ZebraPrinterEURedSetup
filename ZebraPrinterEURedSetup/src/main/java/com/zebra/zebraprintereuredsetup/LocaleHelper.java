package com.zebra.zebraprintereuredsetup;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    public static final String LANGUAGE_SYSTEM = "system";
    public static final String LANGUAGE_AFRIKAANS = "af";
    public static final String LANGUAGE_ALBANIAN = "sq";
    public static final String LANGUAGE_AMHARIC = "am";
    public static final String LANGUAGE_ARABIC = "ar";
    public static final String LANGUAGE_ARMENIAN = "hy";
    public static final String LANGUAGE_AZERBAIJANI = "az";
    public static final String LANGUAGE_BELARUSIAN = "be";
    public static final String LANGUAGE_BENGALI = "bn";
    public static final String LANGUAGE_BOSNIAN = "bs";
    public static final String LANGUAGE_BULGARIAN = "bg";
    public static final String LANGUAGE_BURMESE = "my";
    public static final String LANGUAGE_CATALAN = "ca";
    public static final String LANGUAGE_CHINESE = "zh";
    public static final String LANGUAGE_CHINESE_TW = "zh-rTW";
    public static final String LANGUAGE_CROATIAN = "hr";
    public static final String LANGUAGE_CZECH = "cs";
    public static final String LANGUAGE_DANISH = "da";
    public static final String LANGUAGE_DUTCH = "nl";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_ESTONIAN = "et";
    public static final String LANGUAGE_FILIPINO = "fil";
    public static final String LANGUAGE_FINNISH = "fi";
    public static final String LANGUAGE_FRENCH = "fr";
    public static final String LANGUAGE_GALICIAN = "gl";
    public static final String LANGUAGE_GEORGIAN = "ka";
    public static final String LANGUAGE_GERMAN = "de";
    public static final String LANGUAGE_GREEK = "el";
    public static final String LANGUAGE_GUJARATI = "gu";
    public static final String LANGUAGE_HEBREW = "he";
    public static final String LANGUAGE_HINDI = "hi";
    public static final String LANGUAGE_HUNGARIAN = "hu";
    public static final String LANGUAGE_ICELANDIC = "is";
    public static final String LANGUAGE_INDONESIAN = "id";
    public static final String LANGUAGE_ITALIAN = "it";
    public static final String LANGUAGE_JAPANESE = "ja";
    public static final String LANGUAGE_JAVANESE = "jv";
    public static final String LANGUAGE_KANNADA = "kn";
    public static final String LANGUAGE_KHMER = "km";
    public static final String LANGUAGE_KOREAN = "ko";
    public static final String LANGUAGE_KYRGYZ = "ky";
    public static final String LANGUAGE_LAO = "lo";
    public static final String LANGUAGE_LATIN = "la";
    public static final String LANGUAGE_LATVIAN = "lv";
    public static final String LANGUAGE_LITHUANIAN = "lt";
    public static final String LANGUAGE_MACEDONIAN = "mk";
    public static final String LANGUAGE_MALAY = "ms";
    public static final String LANGUAGE_MALAYALAM = "ml";
    public static final String LANGUAGE_MARATHI = "mr";
    public static final String LANGUAGE_MONGOLIAN = "mn";
    public static final String LANGUAGE_NEPALI = "ne";
    public static final String LANGUAGE_NORWEGIAN = "nb";
    public static final String LANGUAGE_PERSIAN = "fa";
    public static final String LANGUAGE_POLISH = "pl";
    public static final String LANGUAGE_PORTUGUESE = "pt";
    public static final String LANGUAGE_PUNJABI = "pa";
    public static final String LANGUAGE_ROMANIAN = "ro";
    public static final String LANGUAGE_RUSSIAN = "ru";
    public static final String LANGUAGE_SERBIAN = "sr";
    public static final String LANGUAGE_SINHALA = "si";
    public static final String LANGUAGE_SLOVAK = "sk";
    public static final String LANGUAGE_SLOVENIAN = "sl";
    public static final String LANGUAGE_SPANISH = "es";
    public static final String LANGUAGE_SWAHILI = "sw";
    public static final String LANGUAGE_SWEDISH = "sv";
    public static final String LANGUAGE_TAMIL = "ta";
    public static final String LANGUAGE_TELUGU = "te";
    public static final String LANGUAGE_THAI = "th";
    public static final String LANGUAGE_TURKISH = "tr";
    public static final String LANGUAGE_UKRAINIAN = "uk";
    public static final String LANGUAGE_URDU = "ur";
    public static final String LANGUAGE_VIETNAMESE = "vi";
    public static final String LANGUAGE_ZULU = "zu";

    // Sorted alphabetically by language code (System first)
    public static final String[] LANGUAGE_CODES = {
            LANGUAGE_SYSTEM,
            LANGUAGE_AFRIKAANS,
            LANGUAGE_AMHARIC,
            LANGUAGE_ARABIC,
            LANGUAGE_ARMENIAN,
            LANGUAGE_AZERBAIJANI,
            LANGUAGE_BELARUSIAN,
            LANGUAGE_BENGALI,
            LANGUAGE_BOSNIAN,
            LANGUAGE_BULGARIAN,
            LANGUAGE_BURMESE,
            LANGUAGE_CATALAN,
            LANGUAGE_CHINESE,
            LANGUAGE_CHINESE_TW,
            LANGUAGE_CROATIAN,
            LANGUAGE_CZECH,
            LANGUAGE_DANISH,
            LANGUAGE_DUTCH,
            LANGUAGE_ENGLISH,
            LANGUAGE_ESTONIAN,
            LANGUAGE_FILIPINO,
            LANGUAGE_FINNISH,
            LANGUAGE_FRENCH,
            LANGUAGE_GALICIAN,
            LANGUAGE_GEORGIAN,
            LANGUAGE_GERMAN,
            LANGUAGE_GREEK,
            LANGUAGE_GUJARATI,
            LANGUAGE_HEBREW,
            LANGUAGE_HINDI,
            LANGUAGE_HUNGARIAN,
            LANGUAGE_ICELANDIC,
            LANGUAGE_INDONESIAN,
            LANGUAGE_ITALIAN,
            LANGUAGE_JAPANESE,
            LANGUAGE_JAVANESE,
            LANGUAGE_KANNADA,
            LANGUAGE_KHMER,
            LANGUAGE_KOREAN,
            LANGUAGE_KYRGYZ,
            LANGUAGE_LAO,
            LANGUAGE_LATIN,
            LANGUAGE_LATVIAN,
            LANGUAGE_LITHUANIAN,
            LANGUAGE_MACEDONIAN,
            LANGUAGE_MALAY,
            LANGUAGE_MALAYALAM,
            LANGUAGE_MARATHI,
            LANGUAGE_MONGOLIAN,
            LANGUAGE_NEPALI,
            LANGUAGE_NORWEGIAN,
            LANGUAGE_PERSIAN,
            LANGUAGE_POLISH,
            LANGUAGE_PORTUGUESE,
            LANGUAGE_PUNJABI,
            LANGUAGE_ROMANIAN,
            LANGUAGE_RUSSIAN,
            LANGUAGE_SERBIAN,
            LANGUAGE_SINHALA,
            LANGUAGE_SLOVAK,
            LANGUAGE_SLOVENIAN,
            LANGUAGE_ALBANIAN,
            LANGUAGE_SPANISH,
            LANGUAGE_SWAHILI,
            LANGUAGE_SWEDISH,
            LANGUAGE_TAMIL,
            LANGUAGE_TELUGU,
            LANGUAGE_THAI,
            LANGUAGE_TURKISH,
            LANGUAGE_UKRAINIAN,
            LANGUAGE_URDU,
            LANGUAGE_VIETNAMESE,
            LANGUAGE_ZULU
    };

    public static Context applyLocale(Context context) {
        String languageCode = SettingsHelper.getLanguage(context);
        return applyLocale(context, languageCode);
    }

    public static Context applyLocale(Context context, String languageCode) {
        Locale locale;
        if (languageCode == null || languageCode.equals(LANGUAGE_SYSTEM)) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else if (languageCode.equals(LANGUAGE_CHINESE_TW)) {
            locale = Locale.TRADITIONAL_CHINESE;
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
            case LANGUAGE_AFRIKAANS:
                return "Afrikaans";
            case LANGUAGE_ALBANIAN:
                return "Shqip";
            case LANGUAGE_AMHARIC:
                return "አማርኛ";
            case LANGUAGE_ARABIC:
                return "العربية";
            case LANGUAGE_ARMENIAN:
                return "Հայdelays";
            case LANGUAGE_AZERBAIJANI:
                return "Azərbaycan";
            case LANGUAGE_BELARUSIAN:
                return "Беларуская";
            case LANGUAGE_BENGALI:
                return "বাংলা";
            case LANGUAGE_BOSNIAN:
                return "Bosanski";
            case LANGUAGE_BULGARIAN:
                return "Български";
            case LANGUAGE_BURMESE:
                return "မြန်မာ";
            case LANGUAGE_CATALAN:
                return "Català";
            case LANGUAGE_CHINESE:
                return "中文 (简体)";
            case LANGUAGE_CHINESE_TW:
                return "中文 (繁體)";
            case LANGUAGE_CROATIAN:
                return "Hrvatski";
            case LANGUAGE_CZECH:
                return "Čeština";
            case LANGUAGE_DANISH:
                return "Dansk";
            case LANGUAGE_DUTCH:
                return "Nederlands";
            case LANGUAGE_ENGLISH:
                return "English";
            case LANGUAGE_ESTONIAN:
                return "Eesti";
            case LANGUAGE_FILIPINO:
                return "Filipino";
            case LANGUAGE_FINNISH:
                return "Suomi";
            case LANGUAGE_FRENCH:
                return "Français";
            case LANGUAGE_GALICIAN:
                return "Galego";
            case LANGUAGE_GEORGIAN:
                return "ქართული";
            case LANGUAGE_GERMAN:
                return "Deutsch";
            case LANGUAGE_GREEK:
                return "Ελληνικά";
            case LANGUAGE_GUJARATI:
                return "ગુજરાતી";
            case LANGUAGE_HEBREW:
                return "עברית";
            case LANGUAGE_HINDI:
                return "हिन्दी";
            case LANGUAGE_HUNGARIAN:
                return "Magyar";
            case LANGUAGE_ICELANDIC:
                return "Íslenska";
            case LANGUAGE_INDONESIAN:
                return "Bahasa Indonesia";
            case LANGUAGE_ITALIAN:
                return "Italiano";
            case LANGUAGE_JAPANESE:
                return "日本語";
            case LANGUAGE_JAVANESE:
                return "Basa Jawa";
            case LANGUAGE_KANNADA:
                return "ಕನ್ನಡ";
            case LANGUAGE_KHMER:
                return "ខ្មែរ";
            case LANGUAGE_KOREAN:
                return "한국어";
            case LANGUAGE_KYRGYZ:
                return "Кыргызча";
            case LANGUAGE_LAO:
                return "ລາວ";
            case LANGUAGE_LATIN:
                return "Latina";
            case LANGUAGE_LATVIAN:
                return "Latviešu";
            case LANGUAGE_LITHUANIAN:
                return "Lietuvių";
            case LANGUAGE_MACEDONIAN:
                return "Македонски";
            case LANGUAGE_MALAY:
                return "Bahasa Melayu";
            case LANGUAGE_MALAYALAM:
                return "മലയാളം";
            case LANGUAGE_MARATHI:
                return "मराठी";
            case LANGUAGE_MONGOLIAN:
                return "Монгол";
            case LANGUAGE_NEPALI:
                return "नेपाली";
            case LANGUAGE_NORWEGIAN:
                return "Norsk";
            case LANGUAGE_PERSIAN:
                return "فارسی";
            case LANGUAGE_POLISH:
                return "Polski";
            case LANGUAGE_PORTUGUESE:
                return "Português";
            case LANGUAGE_PUNJABI:
                return "ਪੰਜਾਬੀ";
            case LANGUAGE_ROMANIAN:
                return "Română";
            case LANGUAGE_RUSSIAN:
                return "Русский";
            case LANGUAGE_SERBIAN:
                return "Српски";
            case LANGUAGE_SINHALA:
                return "සිංහල";
            case LANGUAGE_SLOVAK:
                return "Slovenčina";
            case LANGUAGE_SLOVENIAN:
                return "Slovenščina";
            case LANGUAGE_SPANISH:
                return "Español";
            case LANGUAGE_SWAHILI:
                return "Kiswahili";
            case LANGUAGE_SWEDISH:
                return "Svenska";
            case LANGUAGE_TAMIL:
                return "தமிழ்";
            case LANGUAGE_TELUGU:
                return "తెలుగు";
            case LANGUAGE_THAI:
                return "ไทย";
            case LANGUAGE_TURKISH:
                return "Türkçe";
            case LANGUAGE_UKRAINIAN:
                return "Українська";
            case LANGUAGE_URDU:
                return "اردو";
            case LANGUAGE_VIETNAMESE:
                return "Tiếng Việt";
            case LANGUAGE_ZULU:
                return "isiZulu";
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

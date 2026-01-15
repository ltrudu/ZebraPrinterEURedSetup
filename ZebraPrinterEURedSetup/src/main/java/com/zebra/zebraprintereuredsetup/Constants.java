package com.zebra.zebraprintereuredsetup;

public class Constants {
    public static final String TAG = "ZEUSetup";

    public static final String SETTINGS_BLUETOOTH_ADDRESS = "ZEURED_BLUETOOTH_ADDRESS";

    public static final String SETTINGS_OLD_ADMIN_PASSWORD = "ZEURED_OLDADMINPASSWORD";
    public static final String SETTINGS_DEFAULT_OLD_PASSWORD = "";

    public static final String SETTINGS_NEW_ADMIN_PASSWORD = "ZEURED_NEW_ADMINPASSWORD";
    public static final String SETTINGS_DEFAULT_NEW_PASSWORD = "JeSuisUneZebra";

   public static final String SETTINGS_ADMIN_PASSWORD = "ZEURED_ADMINPASSWORD";
    public static final String SETTINGS_DEFAULT_PASSWORD = "JeSuisUneZebra";


    public static final String httpadminpasswordKey = "ZEURED_HTTPADMINPASSWORD";
    public static final String languageKey = "ZEURED_LANGUAGE";


    public static final String DEFAULT_PASSWORD_HTTP = "MaSuperImprimanteZebra";

    // Checkbox states for settings cards
    public static final String SETTINGS_CHANGE_PASSWORD_ENABLED = "ZEURED_CHANGE_PASSWORD_ENABLED";
    public static final String SETTINGS_HTTP_ADMIN_ENABLED = "ZEURED_HTTP_ADMIN_ENABLED";

    // Connectivity type
    public static final String SETTINGS_CONNECTIVITY_TYPE = "ZEURED_CONNECTIVITY_TYPE";
    public static final int CONNECTIVITY_TYPE_BLE = 0;
    public static final int CONNECTIVITY_TYPE_USB = 1;

    public static final String ACTION_USB_PERMISSION = "com.android.USB_PERMISSION";

    // Protected Mode setting
    public static final String SETTINGS_PROTECTED_MODE_ALLOWED = "ZEURED_PROTECTED_MODE_ALLOWED";

    // Bluetooth Discoverable setting
    public static final String SETTINGS_BLUETOOTH_DISCOVERABLE_ENABLED = "ZEURED_BLUETOOTH_DISCOVERABLE_ENABLED";
    public static final String SETTINGS_BLUETOOTH_DISCOVERABLE = "ZEURED_BLUETOOTH_DISCOVERABLE";
    public static final boolean DEFAULT_BLUETOOTH_DISCOVERABLE = true;

    // Setvar Wlan Enable setting
    public static final String SETTINGS_SETVAR_WLAN_ENABLE_ENABLED = "ZEURED_SETVAR_WLAN_ENABLE_ENABLED";
    public static final String SETTINGS_SETVAR_WLAN_ENABLE = "ZEURED_SETVAR_WLAN_ENABLE";
    public static final boolean DEFAULT_SETVAR_WLAN_ENABLE = false;

    // Setvar IP HTTP Enable setting
    public static final String SETTINGS_SETVAR_IP_HTTP_ENABLE_ENABLED = "ZEURED_SETVAR_IP_HTTP_ENABLE_ENABLED";
    public static final String SETTINGS_SETVAR_IP_HTTP_ENABLE = "ZEURED_SETVAR_IP_HTTP_ENABLE";
    public static final boolean DEFAULT_SETVAR_IP_HTTP_ENABLE = false;

    // Display Password Level setting
    public static final String SETTINGS_DISPLAY_PASSWORD_LEVEL_ENABLED = "ZEURED_DISPLAY_PASSWORD_LEVEL_ENABLED";
    public static final String SETTINGS_DISPLAY_PASSWORD_LEVEL = "ZEURED_DISPLAY_PASSWORD_LEVEL";
    public static final String DEFAULT_DISPLAY_PASSWORD_LEVEL = "None";

    // Display Password Current setting
    public static final String SETTINGS_DISPLAY_PASSWORD_CURRENT = "ZEURED_DISPLAY_PASSWORD_CURRENT";
    public static final String SETTINGS_DISPLAY_PASSWORD_CURRENT_ENABLED = "ZEURED_DISPLAY_PASSWORD_CURRENT_ENABLED";
    public static final String DEFAULT_DISPLAY_PASSWORD_CURRENT = "JeSuisUneZebra";

    // Device Prompted Network Reset setting
    public static final String SETTINGS_DEVICE_PROMPTED_NETWORK_RESET = "ZEURED_DEVICE_PROMPTED_NETWORK_RESET";
    public static final String SETTINGS_DEVICE_PROMPTED_NETWORK_RESET_ENABLED = "ZEURED_DEVICE_PROMPTED_NETWORK_RESET_ENABLED";
    public static final boolean DEFAULT_DEVICE_PROMPTED_NETWORK_RESET = true;

    // Advanced Mode (hidden feature)
    public static final String SETTINGS_ADVANCED_MODE_ENABLED = "ZEURED_ADVANCED_MODE_ENABLED";

    // EURed Configuration settings
    public static final String SETTINGS_EURED_FIRMWARE_DOWNLOAD = "ZEURED_FIRMWARE_DOWNLOAD";
    public static final String SETTINGS_EURED_TCP_ENABLE = "ZEURED_TCP_ENABLE";
    public static final String SETTINGS_EURED_LPD_ENABLE = "ZEURED_LPD_ENABLE";
    public static final String SETTINGS_EURED_HTTPS_ENABLE = "ZEURED_HTTPS_ENABLE";
    public static final String SETTINGS_EURED_FTP_ENABLE = "ZEURED_FTP_ENABLE";
    public static final String SETTINGS_EURED_SNMP_ENABLE = "ZEURED_SNMP_ENABLE";
    public static final String SETTINGS_EURED_WLAN_ENABLE = "ZEURED_WLAN_ENABLE";
    public static final String SETTINGS_EURED_USB_MIRROR_ENABLE = "ZEURED_USB_MIRROR_ENABLE";
    public static final String SETTINGS_EURED_ZBI_ENABLE = "ZEURED_ZBI_ENABLE";
}

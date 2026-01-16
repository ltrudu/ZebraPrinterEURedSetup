/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2012
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.zebraprintereuredsetup;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsHelper {

    private static final String PREFS_NAME = "ZEURED";

    public static String getOldAdminpassword(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(Constants.SETTINGS_OLD_ADMIN_PASSWORD, Constants.SETTINGS_DEFAULT_OLD_PASSWORD);
    }

    public static void saveOldAdminPassword(Context context, String password) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.SETTINGS_OLD_ADMIN_PASSWORD, password);
        editor.commit();
    }
    public static String getNewAdminpassword(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(Constants.SETTINGS_NEW_ADMIN_PASSWORD, Constants.SETTINGS_DEFAULT_NEW_PASSWORD);
    }

    public static void saveNewAdminPassword(Context context, String password) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.SETTINGS_NEW_ADMIN_PASSWORD, password);
        editor.commit();
    }

    public static String getBluetoothAddress(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(Constants.SETTINGS_BLUETOOTH_ADDRESS, "");
    }

    public static void saveBluetoothAddress(Context context, String address) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.SETTINGS_BLUETOOTH_ADDRESS, address);
        editor.commit();
    }

    public static String getHttpadminpasswordKey(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(Constants.httpadminpasswordKey, Constants.DEFAULT_PASSWORD_HTTP);
    }

    public static void saveHttpadminpasswordKey(Context context, String httpadminpassword) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.httpadminpasswordKey, httpadminpassword);
        editor.commit();
    }

    public static String getLanguage(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(Constants.languageKey, LocaleHelper.LANGUAGE_SYSTEM);
    }

    public static void saveLanguage(Context context, String language) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.languageKey, language);
        editor.commit();
    }

    // Change Password card checkbox state
    public static boolean getChangePasswordEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_CHANGE_PASSWORD_ENABLED, true); // enabled by default
    }

    public static void saveChangePasswordEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_CHANGE_PASSWORD_ENABLED, enabled);
        editor.commit();
    }

    // HTTP Admin card checkbox state
    public static boolean getHttpAdminEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_HTTP_ADMIN_ENABLED, false); // disabled by default
    }

    public static void saveHttpAdminEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_HTTP_ADMIN_ENABLED, enabled);
        editor.commit();
    }

    // Authentication password
    public static String getAuthPassword(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(Constants.SETTINGS_ADMIN_PASSWORD, Constants.SETTINGS_DEFAULT_PASSWORD);
    }

    public static void saveAuthPassword(Context context, String password) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.SETTINGS_ADMIN_PASSWORD, password);
        editor.commit();
    }

    // Connectivity type (BLE or USB)
    public static int getConnectivityType(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(Constants.SETTINGS_CONNECTIVITY_TYPE, Constants.CONNECTIVITY_TYPE_USB);
    }

    public static void saveConnectivityType(Context context, int connectivityType) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.SETTINGS_CONNECTIVITY_TYPE, connectivityType);
        editor.commit();
    }

    // Protected Mode setting (default to false)
    public static boolean getProtectedModeAllowed(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_PROTECTED_MODE_ALLOWED, false);
    }

    public static void saveProtectedModeAllowed(Context context, boolean allowed) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_PROTECTED_MODE_ALLOWED, allowed);
        editor.commit();
    }

    // Bluetooth Discoverable enabled state
    public static boolean getBluetoothDiscoverableEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_BLUETOOTH_DISCOVERABLE_ENABLED, true);
    }

    public static void saveBluetoothDiscoverableEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_BLUETOOTH_DISCOVERABLE_ENABLED, enabled);
        editor.commit();
    }

    // Bluetooth Discoverable value (Yes=true, No=false)
    public static boolean getBluetoothDiscoverable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_BLUETOOTH_DISCOVERABLE, Constants.DEFAULT_BLUETOOTH_DISCOVERABLE);
    }

    public static void saveBluetoothDiscoverable(Context context, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_BLUETOOTH_DISCOVERABLE, value);
        editor.commit();
    }

    // Setvar Wlan Enable enabled state
    public static boolean getSetvarWlanEnableEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_SETVAR_WLAN_ENABLE_ENABLED, false);
    }

    public static void saveSetvarWlanEnableEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_SETVAR_WLAN_ENABLE_ENABLED, enabled);
        editor.commit();
    }

    // Setvar Wlan Enable value (On=true, Off=false)
    public static boolean getSetvarWlanEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_SETVAR_WLAN_ENABLE, Constants.DEFAULT_SETVAR_WLAN_ENABLE);
    }

    public static void saveSetvarWlanEnable(Context context, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_SETVAR_WLAN_ENABLE, value);
        editor.commit();
    }

    // Setvar IP HTTP Enable enabled state
    public static boolean getSetvarIpHttpEnableEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_SETVAR_IP_HTTP_ENABLE_ENABLED, false);
    }

    public static void saveSetvarIpHttpEnableEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_SETVAR_IP_HTTP_ENABLE_ENABLED, enabled);
        editor.commit();
    }

    // Setvar IP HTTP Enable value (On=true, Off=false)
    public static boolean getSetvarIpHttpEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_SETVAR_IP_HTTP_ENABLE, Constants.DEFAULT_SETVAR_IP_HTTP_ENABLE);
    }

    public static void saveSetvarIpHttpEnable(Context context, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_SETVAR_IP_HTTP_ENABLE, value);
        editor.commit();
    }

    // Display Password Level enabled state
    public static boolean getDisplayPasswordLevelEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_DISPLAY_PASSWORD_LEVEL_ENABLED, false);
    }

    public static void saveDisplayPasswordLevelEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_DISPLAY_PASSWORD_LEVEL_ENABLED, enabled);
        editor.commit();
    }

    // Display Password Level value (All, None, Selected - string)
    public static String getDisplayPasswordLevel(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(Constants.SETTINGS_DISPLAY_PASSWORD_LEVEL, Constants.DEFAULT_DISPLAY_PASSWORD_LEVEL);
    }

    public static void saveDisplayPasswordLevel(Context context, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.SETTINGS_DISPLAY_PASSWORD_LEVEL, value);
        editor.commit();
    }

    // EURed Configuration settings (all default to true - authorize everything by default)
    public static boolean getEuredFirmwareDownload(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_FIRMWARE_DOWNLOAD, true);
    }

    public static void saveEuredFirmwareDownload(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_FIRMWARE_DOWNLOAD, enabled);
        editor.commit();
    }

    public static boolean getEuredTcpEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_TCP_ENABLE, true);
    }

    public static void saveEuredTcpEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_TCP_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredLpdEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_LPD_ENABLE, true);
    }

    public static void saveEuredLpdEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_LPD_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredHttpsEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_HTTPS_ENABLE, true);
    }

    public static void saveEuredHttpsEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_HTTPS_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredFtpEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_FTP_ENABLE, true);
    }

    public static void saveEuredFtpEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_FTP_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredSnmpEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_SNMP_ENABLE, true);
    }

    public static void saveEuredSnmpEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_SNMP_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredWlanEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_WLAN_ENABLE, true);
    }

    public static void saveEuredWlanEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_WLAN_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredUsbMirrorEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_USB_MIRROR_ENABLE, true);
    }

    public static void saveEuredUsbMirrorEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_USB_MIRROR_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredZbiEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_ZBI_ENABLE, true);
    }

    public static void saveEuredZbiEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_ZBI_ENABLE, enabled);
        editor.commit();
    }

    // Display Password Current setting
    public static String getDisplayPasswordCurrent(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(Constants.SETTINGS_DISPLAY_PASSWORD_CURRENT, Constants.DEFAULT_DISPLAY_PASSWORD_CURRENT);
    }

    public static void saveDisplayPasswordCurrent(Context context, String password) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.SETTINGS_DISPLAY_PASSWORD_CURRENT, password);
        editor.commit();
    }

    // Display Password Current enabled state
    public static boolean getDisplayPasswordCurrentEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_DISPLAY_PASSWORD_CURRENT_ENABLED, true);
    }

    public static void saveDisplayPasswordCurrentEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_DISPLAY_PASSWORD_CURRENT_ENABLED, enabled);
        editor.commit();
    }

    // Device Prompted Network Reset value (Yes=true, No=false)
    public static boolean getDevicePromptedNetworkReset(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_DEVICE_PROMPTED_NETWORK_RESET, Constants.DEFAULT_DEVICE_PROMPTED_NETWORK_RESET);
    }

    public static void saveDevicePromptedNetworkReset(Context context, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_DEVICE_PROMPTED_NETWORK_RESET, value);
        editor.commit();
    }

    // Device Prompted Network Reset enabled state
    public static boolean getDevicePromptedNetworkResetEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_DEVICE_PROMPTED_NETWORK_RESET_ENABLED, true);
    }

    public static void saveDevicePromptedNetworkResetEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_DEVICE_PROMPTED_NETWORK_RESET_ENABLED, enabled);
        editor.commit();
    }

    // Advanced Mode (hidden feature - default false)
    public static boolean getAdvancedModeEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_ADVANCED_MODE_ENABLED, false);
    }

    public static void saveAdvancedModeEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_ADVANCED_MODE_ENABLED, enabled);
        editor.commit();
    }

    // Last used folder URIs for import/export
    private static final String KEY_LAST_IMPORT_FOLDER_URI = "last_import_folder_uri";
    private static final String KEY_LAST_EXPORT_FOLDER_URI = "last_export_folder_uri";

    public static String getLastImportFolderUri(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(KEY_LAST_IMPORT_FOLDER_URI, null);
    }

    public static void saveLastImportFolderUri(Context context, String uri) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_LAST_IMPORT_FOLDER_URI, uri);
        editor.commit();
    }

    public static String getLastExportFolderUri(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(KEY_LAST_EXPORT_FOLDER_URI, null);
    }

    public static void saveLastExportFolderUri(Context context, String uri) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_LAST_EXPORT_FOLDER_URI, uri);
        editor.commit();
    }

    // Custom Script Card Height (in pixels, -1 means use default)
    private static final String KEY_CUSTOM_SCRIPT_CARD_HEIGHT = "custom_script_card_height";

    public static int getCustomScriptCardHeight(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(KEY_CUSTOM_SCRIPT_CARD_HEIGHT, -1);
    }

    public static void saveCustomScriptCardHeight(Context context, int height) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(KEY_CUSTOM_SCRIPT_CARD_HEIGHT, height);
        editor.commit();
    }

    // Suggestions settings
    private static final String KEY_SUGGESTIONS_UNLIMITED = "suggestions_unlimited";
    private static final String KEY_MAX_SUGGESTIONS = "max_suggestions";

    public static boolean getSuggestionsUnlimited(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(KEY_SUGGESTIONS_UNLIMITED, true); // unlimited by default
    }

    public static void saveSuggestionsUnlimited(Context context, boolean unlimited) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_SUGGESTIONS_UNLIMITED, unlimited);
        editor.commit();
    }

    public static int getMaxSuggestions(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(KEY_MAX_SUGGESTIONS, 10); // default 10 when not unlimited
    }

    public static void saveMaxSuggestions(Context context, int max) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(KEY_MAX_SUGGESTIONS, max);
        editor.commit();
    }

    // Allow Edit EURed Script setting
    private static final String KEY_ALLOW_EDIT_EURED_SCRIPT = "allow_edit_eured_script";

    public static boolean getAllowEditEuredScript(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(KEY_ALLOW_EDIT_EURED_SCRIPT, false); // disabled by default
    }

    public static void saveAllowEditEuredScript(Context context, boolean allow) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_ALLOW_EDIT_EURED_SCRIPT, allow);
        editor.commit();
    }

    // Show Restore Pre-EURed setting
    private static final String KEY_SHOW_RESTORE_PRE_EURED = "show_restore_pre_eured";

    public static boolean getShowRestorePreEured(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(KEY_SHOW_RESTORE_PRE_EURED, false); // hidden by default
    }

    public static void saveShowRestorePreEured(Context context, boolean show) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_SHOW_RESTORE_PRE_EURED, show);
        editor.commit();
    }

    // Show Send Script Card setting
    private static final String KEY_SHOW_SEND_SCRIPT_CARD = "show_send_script_card";

    public static boolean getShowSendScriptCard(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(KEY_SHOW_SEND_SCRIPT_CARD, true); // shown by default
    }

    public static void saveShowSendScriptCard(Context context, boolean show) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_SHOW_SEND_SCRIPT_CARD, show);
        editor.commit();
    }

    // Embed EURED Script setting
    private static final String KEY_EMBED_EURED_SCRIPT = "embed_eured_script";

    public static boolean getEmbedEuredScript(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(KEY_EMBED_EURED_SCRIPT, false); // not embedded by default
    }

    public static void saveEmbedEuredScript(Context context, boolean embed) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_EMBED_EURED_SCRIPT, embed);
        editor.commit();
    }

    // ==================== JSON Export/Import Methods ====================

    /**
     * Get EURED script configuration as JSON.
     * Contains all settings related to EURED script configuration.
     */
    public static JSONObject getEUREDJson(Context context) throws JSONException {
        JSONObject json = new JSONObject();

        // Password settings
        json.put("changePasswordEnabled", getChangePasswordEnabled(context));
        json.put("httpAdminEnabled", getHttpAdminEnabled(context));
        json.put("oldPassword", getOldAdminpassword(context));
        json.put("newPassword", getNewAdminpassword(context));
        json.put("httpAdminPassword", getHttpadminpasswordKey(context));
        json.put("authPassword", getAuthPassword(context));
        json.put("protectedModeAllowed", getProtectedModeAllowed(context));

        // EURed Configuration
        json.put("firmwareDownload", getEuredFirmwareDownload(context));
        json.put("tcpEnable", getEuredTcpEnable(context));
        json.put("lpdEnable", getEuredLpdEnable(context));
        json.put("httpsEnable", getEuredHttpsEnable(context));
        json.put("ftpEnable", getEuredFtpEnable(context));
        json.put("snmpEnable", getEuredSnmpEnable(context));
        json.put("wlanEnable", getEuredWlanEnable(context));
        json.put("usbMirrorEnable", getEuredUsbMirrorEnable(context));
        json.put("zbiEnable", getEuredZbiEnable(context));

        // Bluetooth Discoverable
        json.put("bluetoothDiscoverableEnabled", getBluetoothDiscoverableEnabled(context));
        json.put("bluetoothDiscoverable", getBluetoothDiscoverable(context));

        // Setvar Wlan Enable
        json.put("setvarWlanEnableEnabled", getSetvarWlanEnableEnabled(context));
        json.put("setvarWlanEnable", getSetvarWlanEnable(context));

        // Setvar IP HTTP Enable
        json.put("setvarIpHttpEnableEnabled", getSetvarIpHttpEnableEnabled(context));
        json.put("setvarIpHttpEnable", getSetvarIpHttpEnable(context));

        // Display Password Level
        json.put("displayPasswordLevelEnabled", getDisplayPasswordLevelEnabled(context));
        json.put("displayPasswordLevel", getDisplayPasswordLevel(context));

        // Display Password Current
        json.put("displayPasswordCurrentEnabled", getDisplayPasswordCurrentEnabled(context));
        json.put("displayPasswordCurrent", getDisplayPasswordCurrent(context));

        // Device Prompted Network Reset
        json.put("devicePromptedNetworkResetEnabled", getDevicePromptedNetworkResetEnabled(context));
        json.put("devicePromptedNetworkReset", getDevicePromptedNetworkReset(context));

        return json;
    }

    /**
     * Apply EURED settings from JSON.
     * Saves all EURED script configuration settings from the provided JSON.
     */
    public static void applyEUREDJson(Context context, JSONObject json) throws JSONException {
        // Password settings
        if (json.has("changePasswordEnabled")) {
            saveChangePasswordEnabled(context, json.getBoolean("changePasswordEnabled"));
        }
        if (json.has("httpAdminEnabled")) {
            saveHttpAdminEnabled(context, json.getBoolean("httpAdminEnabled"));
        }
        if (json.has("oldPassword")) {
            saveOldAdminPassword(context, json.getString("oldPassword"));
        }
        if (json.has("newPassword")) {
            saveNewAdminPassword(context, json.getString("newPassword"));
        }
        if (json.has("httpAdminPassword")) {
            saveHttpadminpasswordKey(context, json.getString("httpAdminPassword"));
        }
        if (json.has("authPassword")) {
            saveAuthPassword(context, json.getString("authPassword"));
        }
        if (json.has("protectedModeAllowed")) {
            saveProtectedModeAllowed(context, json.getBoolean("protectedModeAllowed"));
        }

        // EURed Configuration
        if (json.has("firmwareDownload")) {
            saveEuredFirmwareDownload(context, json.getBoolean("firmwareDownload"));
        }
        if (json.has("tcpEnable")) {
            saveEuredTcpEnable(context, json.getBoolean("tcpEnable"));
        }
        if (json.has("lpdEnable")) {
            saveEuredLpdEnable(context, json.getBoolean("lpdEnable"));
        }
        if (json.has("httpsEnable")) {
            saveEuredHttpsEnable(context, json.getBoolean("httpsEnable"));
        }
        if (json.has("ftpEnable")) {
            saveEuredFtpEnable(context, json.getBoolean("ftpEnable"));
        }
        if (json.has("snmpEnable")) {
            saveEuredSnmpEnable(context, json.getBoolean("snmpEnable"));
        }
        if (json.has("wlanEnable")) {
            saveEuredWlanEnable(context, json.getBoolean("wlanEnable"));
        }
        if (json.has("usbMirrorEnable")) {
            saveEuredUsbMirrorEnable(context, json.getBoolean("usbMirrorEnable"));
        }
        if (json.has("zbiEnable")) {
            saveEuredZbiEnable(context, json.getBoolean("zbiEnable"));
        }

        // Bluetooth Discoverable
        if (json.has("bluetoothDiscoverableEnabled")) {
            saveBluetoothDiscoverableEnabled(context, json.getBoolean("bluetoothDiscoverableEnabled"));
        }
        if (json.has("bluetoothDiscoverable")) {
            saveBluetoothDiscoverable(context, json.getBoolean("bluetoothDiscoverable"));
        }

        // Setvar Wlan Enable
        if (json.has("setvarWlanEnableEnabled")) {
            saveSetvarWlanEnableEnabled(context, json.getBoolean("setvarWlanEnableEnabled"));
        }
        if (json.has("setvarWlanEnable")) {
            saveSetvarWlanEnable(context, json.getBoolean("setvarWlanEnable"));
        }

        // Setvar IP HTTP Enable
        if (json.has("setvarIpHttpEnableEnabled")) {
            saveSetvarIpHttpEnableEnabled(context, json.getBoolean("setvarIpHttpEnableEnabled"));
        }
        if (json.has("setvarIpHttpEnable")) {
            saveSetvarIpHttpEnable(context, json.getBoolean("setvarIpHttpEnable"));
        }

        // Display Password Level
        if (json.has("displayPasswordLevelEnabled")) {
            saveDisplayPasswordLevelEnabled(context, json.getBoolean("displayPasswordLevelEnabled"));
        }
        if (json.has("displayPasswordLevel")) {
            saveDisplayPasswordLevel(context, json.getString("displayPasswordLevel"));
        }

        // Display Password Current
        if (json.has("displayPasswordCurrentEnabled")) {
            saveDisplayPasswordCurrentEnabled(context, json.getBoolean("displayPasswordCurrentEnabled"));
        }
        if (json.has("displayPasswordCurrent")) {
            saveDisplayPasswordCurrent(context, json.getString("displayPasswordCurrent"));
        }

        // Device Prompted Network Reset
        if (json.has("devicePromptedNetworkResetEnabled")) {
            saveDevicePromptedNetworkResetEnabled(context, json.getBoolean("devicePromptedNetworkResetEnabled"));
        }
        if (json.has("devicePromptedNetworkReset")) {
            saveDevicePromptedNetworkReset(context, json.getBoolean("devicePromptedNetworkReset"));
        }
    }

    /**
     * Get app settings as JSON.
     * @param context Android context
     * @param embedEURED If true, includes EURED configuration in the JSON
     */
    public static JSONObject getSettingsJSON(Context context, boolean embedEURED) throws JSONException {
        JSONObject json = new JSONObject();

        // Language
        json.put("language", getLanguage(context));

        // Suggestions settings
        json.put("suggestionsUnlimited", getSuggestionsUnlimited(context));
        json.put("maxSuggestions", getMaxSuggestions(context));

        // Card visibility settings
        json.put("showEditEuredScriptCard", getAllowEditEuredScript(context));
        json.put("showRestorePreEuredCard", getShowRestorePreEured(context));
        json.put("showSendScriptCard", getShowSendScriptCard(context));

        // Embed setting
        json.put("embedEuredScript", getEmbedEuredScript(context));

        // Optionally embed EURED config
        if (embedEURED) {
            json.put("euredConfig", getEUREDJson(context));
        }

        return json;
    }

    /**
     * Apply app settings from JSON.
     * If the JSON contains embedded EURED config, it will also be applied.
     */
    public static void applySettingsJSON(Context context, JSONObject json) throws JSONException {
        // Language
        if (json.has("language")) {
            saveLanguage(context, json.getString("language"));
        }

        // Suggestions settings
        if (json.has("suggestionsUnlimited")) {
            saveSuggestionsUnlimited(context, json.getBoolean("suggestionsUnlimited"));
        }
        if (json.has("maxSuggestions")) {
            saveMaxSuggestions(context, json.getInt("maxSuggestions"));
        }

        // Card visibility settings
        if (json.has("showEditEuredScriptCard")) {
            saveAllowEditEuredScript(context, json.getBoolean("showEditEuredScriptCard"));
        }
        if (json.has("showRestorePreEuredCard")) {
            saveShowRestorePreEured(context, json.getBoolean("showRestorePreEuredCard"));
        }
        if (json.has("showSendScriptCard")) {
            saveShowSendScriptCard(context, json.getBoolean("showSendScriptCard"));
        }

        // Embed setting
        if (json.has("embedEuredScript")) {
            saveEmbedEuredScript(context, json.getBoolean("embedEuredScript"));
        }

        // Apply embedded EURED config if present
        if (json.has("euredConfig")) {
            applyEUREDJson(context, json.getJSONObject("euredConfig"));
        }
    }

    // Edit Mode setting
    private static final String EDIT_MODE_ENABLED_KEY = "EDIT_MODE_ENABLED";

    public static boolean getEditModeEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(EDIT_MODE_ENABLED_KEY, false);
    }

    public static void saveEditModeEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(EDIT_MODE_ENABLED_KEY, enabled);
        editor.commit();
    }
}

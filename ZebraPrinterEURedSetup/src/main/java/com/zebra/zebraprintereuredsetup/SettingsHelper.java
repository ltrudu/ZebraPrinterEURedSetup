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
}

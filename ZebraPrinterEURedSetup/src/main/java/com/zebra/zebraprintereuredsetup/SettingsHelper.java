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

    // Bluetooth Discoverable setting (default to true)
    public static boolean getBluetoothDiscoverable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_BLUETOOTH_DISCOVERABLE, true);
    }

    public static void saveBluetoothDiscoverable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_BLUETOOTH_DISCOVERABLE, enabled);
        editor.commit();
    }

    // EURed Configuration settings (all default to false)
    public static boolean getEuredFirmwareDownload(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_FIRMWARE_DOWNLOAD, false);
    }

    public static void saveEuredFirmwareDownload(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_FIRMWARE_DOWNLOAD, enabled);
        editor.commit();
    }

    public static boolean getEuredTcpEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_TCP_ENABLE, false);
    }

    public static void saveEuredTcpEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_TCP_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredLpdEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_LPD_ENABLE, false);
    }

    public static void saveEuredLpdEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_LPD_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredHttpsEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_HTTPS_ENABLE, false);
    }

    public static void saveEuredHttpsEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_HTTPS_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredFtpEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_FTP_ENABLE, false);
    }

    public static void saveEuredFtpEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_FTP_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredSnmpEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_SNMP_ENABLE, false);
    }

    public static void saveEuredSnmpEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_SNMP_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredWlanEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_WLAN_ENABLE, false);
    }

    public static void saveEuredWlanEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_WLAN_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredUsbMirrorEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_USB_MIRROR_ENABLE, false);
    }

    public static void saveEuredUsbMirrorEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_USB_MIRROR_ENABLE, enabled);
        editor.commit();
    }

    public static boolean getEuredZbiEnable(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(Constants.SETTINGS_EURED_ZBI_ENABLE, false);
    }

    public static void saveEuredZbiEnable(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_EURED_ZBI_ENABLE, enabled);
        editor.commit();
    }
}

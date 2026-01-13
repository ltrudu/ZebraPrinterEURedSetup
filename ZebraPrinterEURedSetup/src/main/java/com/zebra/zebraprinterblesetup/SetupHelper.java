package com.zebra.zebraprinterblesetup;

public class SetupHelper {

    private String configLabelString ="";

    public SetupHelper setDeviceLanguageZPL()
    {
        configLabelString += "! U1 setvar \"device.languages\" \"zpl\"\r\n";
        return this;
    }

    public SetupHelper changePassword(String oldPassowrd, String newPassword)
    {
        configLabelString += "{}{\n" +
                "  \"protect\": {\n" +
                "    \"authentication\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \""+ oldPassowrd +"\",\n" +
                "      \"type\": \"basic\"\n" +
                "    },\n" +
                "    \"operation\": \"setup\",\n" +
                "    \"setup\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \""+ newPassword +"\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        return this;
    }

    public SetupHelper setProtectedModeAllowed(String password, boolean allowed)
    {
        configLabelString += "{}{\n" +
                "  \"protect\": {\n" +
                "    \"authentication\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \""+ password + "\",\n" +
                "      \"type\": \"basic\"\n" +
                "    },\n" +
                "    \"operation\": \"configure-one\",\n" +
                "    \"configure-one\": {\n" +
                "      \"protected-mode-allowed\": \"" + (allowed ? "yes":"no") + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        return this;
    }

    public SetupHelper setEURedParams(String authenticationPasword,
                                      Boolean allowFirmwareDownload,
                                      Boolean tcpEnable,
                                      Boolean lpdEnable,
                                      Boolean httpsenable,
                                      Boolean ftpEnable,
                                      Boolean snmpEnable,
                                      Boolean wlanEnable,
                                      Boolean usbMirrorEnable,
                                      Boolean zbiEnable,
                                      String httpadminpassword,
                                      String displayPasswordCurrent)
    {
        configLabelString += "{}{\n" +
                "  \"protect\": {\n" +
                "    \"authentication\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \""+authenticationPasword+"\",\n" +
                "      \"type\": \"basic\"\n" +
                "    },\n" +
                "    \"operation\": \"set\",\n" +
                "    \"set\": {\n" +
                "      \"device.allow_firmware_downloads\": \""+ (allowFirmwareDownload ? "yes" : "no") +"\",\n" +
                "      \"ip.tcp.enable\": \""+ (tcpEnable ? "on" : "off") + "\",\n" +
                "      \"ip.lpd.enable\": \"" + (lpdEnable ? "on" : "off") + "\",\n" +
                "      \"ip.https.enable\": \""+ (httpsenable ? "on" : "off") + "\",\n" +
                "      \"ip.ftp.enable\": \"" + (ftpEnable ? "on" : "off") + "\",\n" +
                "      \"ip.snmp.enable\": \"" + (snmpEnable ? "on" : "off") + "\",\n" +
                "      \"wlan.enable\": \" " + (wlanEnable ? "on" : "off") + "\",\n" +
                "      \"usb.mirror.enable\": \" " + (usbMirrorEnable ? "on" : "off") + "\",\n" +
                "      \"zbi.enable\": \"" + (zbiEnable ? "on" : "off") + "\",\n" +
                "      \"display.password.current\": \"" + ((displayPasswordCurrent != null && displayPasswordCurrent.isEmpty() == false) ? displayPasswordCurrent : "") + "\",\n";

        if(httpadminpassword != null && httpadminpassword.isEmpty() == false)
            configLabelString += "      \"ip.http.admin_password\": \""+ httpadminpassword + "\"\n";

        configLabelString += "    }\n" +
                "  }\n" +
                "}\n";

        return this;
    }

    public SetupHelper setDevicePromptedNetworkReset(String password)
    {
        configLabelString += "{}{\n" +
                "  \"protect\": {\n" +
                "    \"authentication\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \""+ password +"\",\n" +
                "      \"type\": \"basic\"\n" +
                "    },\n" +
                "    \"operation\": \"set\",\n" +
                "    \"set\": {\n" +
                "      \"device.prompted_network_reset\": \"yes\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        return this;
    }

    public SetupHelper setupEURed(String password, String httpadminpassword)
    {
            configLabelString += "{}{\n" +
                    "  \"protect\": {\n" +
                    "    \"authentication\": {\n" +
                    "      \"username\": \"admin\",\n" +
                    "      \"password\": \""+ password + "\",\n" +
                    "      \"type\": \"basic\"\n" +
                    "    },\n" +
                    "    \"operation\": \"configure-one\",\n" +
                    "    \"configure-one\": {\n" +
                    "      \"protected-mode-allowed\": \"no\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n" +
                    "{}{\n" +
                    "  \"protect\": {\n" +
                    "    \"authentication\": {\n" +
                    "      \"username\": \"admin\",\n" +
                    "      \"password\": \""+password+"\",\n" +
                    "      \"type\": \"basic\"\n" +
                    "    },\n" +
                    "    \"operation\": \"set\",\n" +
                    "    \"set\": {\n" +
                    "      \"device.allow_firmware_downloads\": \"yes\",\n" +
                    "      \"ip.tcp.enable\": \"on\",\n" +
                    "      \"ip.lpd.enable\": \"off\",\n" +
                    "      \"ip.https.enable\": \"on\",\n" +
                    "      \"ip.ftp.enable\": \"off\",\n" +
                    "      \"ip.snmp.enable\": \"off\",\n" +
                    "      \"wlan.enable\": \"on\",\n" +
                    "      \"usb.mirror.enable\": \"on\",\n" +
                    "      \"zbi.enable\": \"off\",\n" +
                    "      \"display.password.current\": \"\",\n";

                    if(httpadminpassword != null && httpadminpassword.isEmpty() == false)
                        configLabelString += "      \"ip.http.admin_password\": \""+ httpadminpassword + "\"\n";

                    configLabelString += "    }\n" +
                    "  }\n" +
                    "}\n" +
                    "{}{\n" +
                    "  \"protect\": {\n" +
                    "    \"authentication\": {\n" +
                    "      \"username\": \"admin\",\n" +
                    "      \"password\": \""+ password +"\",\n" +
                    "      \"type\": \"basic\"\n" +
                    "    },\n" +
                    "    \"operation\": \"set\",\n" +
                    "    \"set\": {\n" +
                    "      \"device.prompted_network_reset\": \"yes\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n";
            return this;
    }

    public SetupHelper restoreDeviceLanguage()
    {
        configLabelString += "! U1 setvar \"device.restore_defaults\" \"device.languages\"\r\n";
        return this;
    }

    public SetupHelper changeBluetoothDiscoverable(boolean discoverable)
    {
        if(discoverable)
        {
            configLabelString +=  "! U1 setvar \"bluetooth.discoverable\" \"on\"\r\n";
        }
        else
        {
            configLabelString +=  "! U1 setvar \"bluetooth.discoverable\" \"off\"\r\n";
        }
        return this;
    }

    public SetupHelper resetDevice()
    {
        configLabelString += "! U1 do \"device.reset\" \"\"\r\n";
        return this;
    }

    public byte[] getBytesArray()
    {
        return configLabelString.getBytes();
    }

    public String getString() { return configLabelString;}

    private byte[] updatePasswordAndMakeBluetoothDiscoverable() {
        byte[] configLabel = null;
        String configlabelString = "! U1 setvar \"device.languages\" \"zpl\"\r\n" +
                "{}{\n" +
                "  \"protect\": {\n" +
                "    \"authentication\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \"\",\n" +
                "      \"type\": \"basic\"\n" +
                "    },\n" +
                "    \"operation\": \"setup\",\n" +
                "    \"setup\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \"JeSuisUneZebra\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "{}{\n" +
                "  \"protect\": {\n" +
                "    \"authentication\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \"JeSuisUneZebra\",\n" +
                "      \"type\": \"basic\"\n" +
                "    },\n" +
                "    \"operation\": \"configure-one\",\n" +
                "    \"configure-one\": {\n" +
                "      \"protected-mode-allowed\": \"no\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "{}{\n" +
                "  \"protect\": {\n" +
                "    \"authentication\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \"JeSuisUneZebra\",\n" +
                "      \"type\": \"basic\"\n" +
                "    },\n" +
                "    \"operation\": \"set\",\n" +
                "    \"set\": {\n" +
                "      \"device.allow_firmware_downloads\": \"yes\",\n" +
                "      \"ip.tcp.enable\": \"on\",\n" +
                "      \"ip.lpd.enable\": \"off\",\n" +
                "      \"ip.https.enable\": \"on\",\n" +
                "      \"ip.ftp.enable\": \"off\",\n" +
                "      \"ip.snmp.enable\": \"off\",\n" +
                "      \"wlan.enable\": \"on\",\n" +
                "      \"usb.mirror.enable\": \"on\",\n" +
                "      \"zbi.enable\": \"off\",\n" +
                "      \"display.password.current\": \"\",\n" +
                "      \"ip.http.admin_password\": \"MaSuperImprimanteZebra\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "{}{\n" +
                "  \"protect\": {\n" +
                "    \"authentication\": {\n" +
                "      \"username\": \"admin\",\n" +
                "      \"password\": \"JeSuisUneZebra\",\n" +
                "      \"type\": \"basic\"\n" +
                "    },\n" +
                "    \"operation\": \"set\",\n" +
                "    \"set\": {\n" +
                "      \"device.prompted_network_reset\": \"yes\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "! U1 setvar \"device.restore_defaults\" \"device.languages\"\r\n"+
                "! U1 setvar \"bluetooth.discoverable\" \"on\"\r\n"+
                "! U1 do \"device.reset\" \"\"\r\n";

        configLabel = configlabelString.getBytes();
        return configLabel;
    }

}

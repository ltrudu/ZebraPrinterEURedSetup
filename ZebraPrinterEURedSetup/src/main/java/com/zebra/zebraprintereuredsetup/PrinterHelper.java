package com.zebra.zebraprintereuredsetup;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.zebra.sdk.btleComm.BluetoothLeConnection;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;

import java.util.LinkedList;
import java.util.List;

import static com.zebra.zebraprintereuredsetup.Constants.ACTION_USB_PERMISSION;

public class PrinterHelper {
    private Connection printerConnection;
    private ZebraPrinter printer;
    private Boolean sendingDataToPrinter = false;
    private Context context;

    public interface PrinterHelperCallback {
        void OnStatus(String message, int color);
        void onSuccess();
    }

    private boolean hasPermissionToCommunicate = false;
    private IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    private PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;

    private DiscoveredPrinterUsb discoveredPrinterUsb;
    private byte[] mDataToPrint;

    private PrinterHelperCallback printerHelperCallback = null;

    // Catches intent indicating if the user grants permission to use the USB device
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            setStatus(context.getString(R.string.status_permission_granted), Color.GREEN);
                            hasPermissionToCommunicate = true;
                            context.unregisterReceiver(mUsbReceiver);
                            PrintUSB(context, mDataToPrint);
                        }
                    }
                }
            }
        }
    };

    // Handles USB device discovery
    private class UsbDiscoveryHandler implements DiscoveryHandler {
        public List<DiscoveredPrinterUsb> printers;
        public boolean discoveryComplete = false;

        public UsbDiscoveryHandler() {
            printers = new LinkedList<DiscoveredPrinterUsb>();
        }

        public void foundPrinter(final DiscoveredPrinter printer) {
            printers.add((DiscoveredPrinterUsb) printer);
        }

        public void discoveryFinished() {
            discoveryComplete = true;
        }

        public void discoveryError(String message) {
            discoveryComplete = true;
        }
    }
    public void setupPasswordAndBluetooth(Context context, String macAddress, PrinterHelperCallback callback) {
        this.context = context;
        this.printerHelperCallback = callback;
        final byte[] message = getEURedConfiguration().getBytes();
        new Thread(() -> {
            Looper.prepare();
            doSendDataToPrinterBLE(context, macAddress, message);
            Looper.loop();
            Looper.myLooper().quit();
            if (printerHelperCallback != null)
                printerHelperCallback.onSuccess();
        }).start();
    }

    protected void doSendDataToPrinterBLE(Context context, String macAddress, byte[] message) {
        sendingDataToPrinter = true;
        setStatus(context.getString(R.string.status_connecting), Color.YELLOW);
        printer = connectBLE(context, macAddress);
        if (printer != null) {
            sendDataToPrinter(message);
        } else {
            disconnect();
        }
    }

    private void sendDataToPrinter(byte[] message) {
        try {
            printerConnection.write(message);
            setStatus(context.getString(R.string.status_sending_data), Color.BLUE);
            sleep(1500);
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
                setStatus(friendlyName, Color.MAGENTA);
                sleep(500);
            }
        } catch (ConnectionException e) {
            setStatus(e.getMessage(), Color.RED);
        } finally {
            disconnect();
        }
    }

    protected ZebraPrinter connectBLE(Context context, String macAddress) {
        setStatus(context.getString(R.string.status_connecting_dots), Color.YELLOW);
        printerConnection = null;
        printerConnection = new BluetoothLeConnection(macAddress, context);
        SettingsHelper.saveBluetoothAddress(context, macAddress);

        try {
            printerConnection.open();
            setStatus(context.getString(R.string.status_connected), Color.GREEN);
        } catch (ConnectionException e) {
            setStatus(context.getString(R.string.status_comm_error_disconnecting), Color.RED);
            sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                setStatus(context.getString(R.string.status_determining_printer_language), Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus(context.getString(R.string.status_printer_language, pl.toString()), Color.BLUE);
            } catch (ConnectionException e) {
                setStatus(context.getString(R.string.status_unknown_printer_language), Color.RED);
                printer = null;
                sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus(context.getString(R.string.status_unknown_printer_language), Color.RED);
                printer = null;
                sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

    public void sendDataToBT(Context context, String message, String macAddress, PrinterHelperCallback callback) {
        this.context = context;
        this.printerHelperCallback = callback;
        final byte[] messageByte = message.getBytes();
        new Thread(() -> {
            Looper.prepare();
            doSendDataToPrinterBT(context, macAddress, messageByte);
            Looper.loop();
            Looper.myLooper().quit();
            if (printerHelperCallback != null)
                printerHelperCallback.onSuccess();
        }).start();
    }

    protected void doSendDataToPrinterBT(Context context, String macAddress, byte[] message) {
        sendingDataToPrinter = true;
        setStatus(context.getString(R.string.status_connecting), Color.YELLOW);
        printer = connectBT(context, macAddress);
        if (printer != null) {
            sendDataToPrinter(message);
        } else {
            disconnect();
        }
    }

    public ZebraPrinter connectBT(Context context, String macAddress) {
        setStatus(context.getString(R.string.status_connecting_dots), Color.YELLOW);
        printerConnection = null;

        printerConnection = new BluetoothConnection(macAddress);
        SettingsHelper.saveBluetoothAddress(context, macAddress);
        try {
            printerConnection.open();
            setStatus(context.getString(R.string.status_connected), Color.GREEN);
        } catch (ConnectionException e) {
            setStatus(context.getString(R.string.status_comm_error_disconnecting), Color.RED);
            sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                setStatus(context.getString(R.string.status_determining_printer_language), Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus(context.getString(R.string.status_printer_language, pl.toString()), Color.BLUE);
            } catch (ConnectionException e) {
                setStatus(context.getString(R.string.status_unknown_printer_language), Color.RED);
                printer = null;
                sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus(context.getString(R.string.status_unknown_printer_language), Color.RED);
                printer = null;
                sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

    protected void disconnect() {
        try {
            setStatus(context.getString(R.string.status_disconnecting), Color.RED);
            if (printerConnection != null) {
                printerConnection.close();
            }
            setStatus(context.getString(R.string.status_ready), Color.GREEN);
        } catch (ConnectionException e) {
            setStatus(context.getString(R.string.status_comm_error_disconnected), Color.RED);
        } finally {
            sendingDataToPrinter = false;
        }
    }

    protected void setStatus(final String statusMessage, final int color) {
        if (printerHelperCallback != null) {
            printerHelperCallback.OnStatus(statusMessage, color);
        }
        sleep(1000);
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getEURedConfiguration()
    {
        // Retrieve parameters from settings
        boolean changePasswordEnabled = SettingsHelper.getChangePasswordEnabled(context);
        boolean httpAdminEnabled = SettingsHelper.getHttpAdminEnabled(context);
        String oldSecurePassword = SettingsHelper.getOldAdminpassword(context);
        String newSecurePassword = SettingsHelper.getNewAdminpassword(context);
        String httpAdminPassword = SettingsHelper.getHttpadminpasswordKey(context);
        String authenticationPassword = newSecurePassword;

        // Retrieve protected mode allowed
        boolean protectedModeAllowed = SettingsHelper.getProtectedModeAllowed(context);

        // Retrieve EURed configuration settings
        boolean allowFirmwareDownload = SettingsHelper.getEuredFirmwareDownload(context);
        boolean tcpEnable = SettingsHelper.getEuredTcpEnable(context);
        boolean lpdEnable = SettingsHelper.getEuredLpdEnable(context);
        boolean httpsEnable = SettingsHelper.getEuredHttpsEnable(context);
        boolean ftpEnable = SettingsHelper.getEuredFtpEnable(context);
        boolean snmpEnable = SettingsHelper.getEuredSnmpEnable(context);
        boolean wlanEnable = SettingsHelper.getEuredWlanEnable(context);
        boolean usbMirrorEnable = SettingsHelper.getEuredUsbMirrorEnable(context);
        boolean zbiEnable = SettingsHelper.getEuredZbiEnable(context);

        // Retrieve bluetooth discoverable
        boolean setBluetoothDiscoverable = SettingsHelper.getBluetoothDiscoverable(context);

        // Build the Setup Helper
        SetupHelper setupHelper = new SetupHelper();
        setupHelper = setupHelper.setDeviceLanguageZPL();

        if(changePasswordEnabled)
            setupHelper = setupHelper.changePassword(oldSecurePassword, newSecurePassword);

        setupHelper = setupHelper.setProtectedModeAllowed(authenticationPassword, protectedModeAllowed)
                .setEURedParams(authenticationPassword,
                allowFirmwareDownload, tcpEnable, lpdEnable, httpsEnable, ftpEnable,
                snmpEnable, wlanEnable, usbMirrorEnable, zbiEnable,
                httpAdminEnabled ? httpAdminPassword : null, null)
                .setDevicePromptedNetworkReset(authenticationPassword)
                .restoreDeviceLanguage()
                .changeBluetoothDiscoverable(setBluetoothDiscoverable)
                .resetDevice();

        return setupHelper.getString();
    }

    // USB Methods
    public void sendDataToUSB(Context context, String message, PrinterHelperCallback callback) {
        this.context = context;
        this.printerHelperCallback = callback;
        new Thread(() -> {
            sendDataToUSBPrinter(context, message, callback);
            if (printerHelperCallback != null)
                printerHelperCallback.onSuccess();
        }).start();
    }

    public void setupPasswordAndBluetoothUSB(Context context, PrinterHelperCallback callback) {
        this.context = context;
        this.printerHelperCallback = callback;
        final String message = getEURedConfiguration();
        Log.d(Constants.TAG, message);
        new Thread(() -> {
            sendDataToUSBPrinter(context, message, callback);
            if (printerHelperCallback != null)
                printerHelperCallback.onSuccess();
        }).start();
    }

    public void sendDataToUSBPrinter(Context context, String dataString, PrinterHelperCallback printerHelperCallback)
    {
        byte[] data = dataString.getBytes();
        this.printerHelperCallback = printerHelperCallback;
        // Register broadcast receiver that catches USB permission intent
        if(mUsbManager == null) {
            if(printerHelperCallback != null)
            {
                printerHelperCallback.OnStatus("Getting USB manager", Color.YELLOW);
            }
            mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        }
        if(mPermissionIntent == null) {
            if(printerHelperCallback != null) {
                printerHelperCallback.OnStatus("Creating Permission Intent", Color.YELLOW);
            }
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            // Android 12+ (API 31) requires FLAG_MUTABLE for USB permission intents
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }
            // Make Intent explicit by setting package (required for Android 14+ with FLAG_MUTABLE)
            Intent intent = new Intent(ACTION_USB_PERMISSION);
            intent.setPackage(context.getPackageName());
            mPermissionIntent = PendingIntent.getBroadcast(context, 0, intent, flags);
        }
        // Find connected printers
        if(printerHelperCallback != null)
        {
            printerHelperCallback.OnStatus("Summon USB Discovery Handler", Color.WHITE);
        }
        UsbDiscoveryHandler handler = new UsbDiscoveryHandler();
        UsbDiscoverer.findPrinters(context, handler);

        try {
            while (!handler.discoveryComplete) {
                Thread.sleep(100);
            }

            if (handler.printers != null && handler.printers.size() > 0) {
                if(printerHelperCallback != null)
                {
                    printerHelperCallback.OnStatus("Found " + String.valueOf(handler.printers.size()) + " printers", Color.GREEN);
                }
                discoveredPrinterUsb = handler.printers.get(0);
                if(printerHelperCallback != null)
                {
                    printerHelperCallback.OnStatus("Selected printer: " + discoveredPrinterUsb.address, Color.GREEN);
                }
                if (!mUsbManager.hasPermission(discoveredPrinterUsb.device)) {
                    if(printerHelperCallback != null)
                    {
                        printerHelperCallback.OnStatus("Need USB Permissions", Color.RED);
                    }
                    mDataToPrint = data;
                    // Android 14+ (API 34) requires specifying receiver export flags
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        context.registerReceiver(mUsbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
                    } else {
                        context.registerReceiver(mUsbReceiver, filter);
                    }
                    mUsbManager.requestPermission(discoveredPrinterUsb.device, mPermissionIntent);
                } else {
                    hasPermissionToCommunicate = true;
                    PrintUSB(context, data);
                }
            }
        } catch (Exception e) {
            Toast.makeText(context,"Error discovering printers: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected void PrintUSB(Context context, byte[] data)
    {
        ZebraPrinter printer = connectUSB(context, discoveredPrinterUsb);
        if(printer != null)
            sendDataToPrinter(data);
    }

    protected ZebraPrinter connectUSB(Context context, DiscoveredPrinterUsb usbprinter) {
        setStatus(context.getString(R.string.status_connecting_dots), Color.YELLOW);
        printerConnection = null;
        printerConnection = usbprinter.getConnection();

        try {
            printerConnection.open();
            setStatus(context.getString(R.string.status_connected), Color.GREEN);
        } catch (ConnectionException e) {
            setStatus(context.getString(R.string.status_comm_error_disconnecting), Color.RED);
            sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                setStatus(context.getString(R.string.status_determining_printer_language), Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus(context.getString(R.string.status_printer_language, pl.toString()), Color.BLUE);
            } catch (ConnectionException e) {
                setStatus(context.getString(R.string.status_unknown_printer_language), Color.RED);
                printer = null;
                sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus(context.getString(R.string.status_unknown_printer_language), Color.RED);
                printer = null;
                sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

}

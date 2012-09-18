package com.senddroid.optout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Utils {
    private static String sID = null;

    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (deviceId == null) {
            deviceId = id(context);
        }
        return deviceId;
    }

    public static String getDeviceIdMD5(Context context) {
        String deviceId = getDeviceId(context);
        if(deviceId != null) {
            return md5(deviceId);
        }
        return null;
    }

    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), "INSTALLATION");
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                Log.e("SendDROID", "Could not get device id", e);
            }
        }
        return sID;
    }

    public static String md5(String data) {
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(data.getBytes());
            byte[] messageDigest = digester.digest();
            return Utils.byteArrayToHexString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            Log.e("SendDROID", "Couldn't generate md5", e);
        }
        return null;
    }

    public static String byteArrayToHexString(byte[] array) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}


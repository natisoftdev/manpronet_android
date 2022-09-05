package com.nfc;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by aless on 19/11/2015.
 */
public class RichiestaNFC {
    static Date NFCintentoWebData = null;
    static String NFCintentoWeb = "";
    static String NFCtagToWrite = "";

    public static void inizializza()
    {
        NFCintentoWeb = "";
    }
    public static String getNFCintentoWeb()
    {
       return NFCintentoWeb;
    }
    public static String getNFCtagToWrite()
    {
       return NFCtagToWrite;
    }

    public static void TagRead() {
        NFCtagToWrite = "";
        NFCintentoWebData = new Date();
        NFCintentoWeb = "read";
    }

    public static void TagWrite(String tagToWrite) {
        NFCtagToWrite = tagToWrite;
        NFCintentoWebData = new Date();
        NFCintentoWeb = "write";
    }

    public static boolean valido() {
        boolean valido = false;

        if(NFCintentoWeb.equals("read")||NFCintentoWeb.equals("write")) {
            Date end = new Date();
            Date start = NFCintentoWebData;

            long diffInMs = end.getTime() - start.getTime();
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);

            if(diffInSec<=30) { valido = true; }
        }
        return valido;
    }
}

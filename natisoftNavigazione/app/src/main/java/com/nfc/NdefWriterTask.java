package com.nfc;

import android.app.Activity;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.webkit.WebView;

import com.costanti.CostantiWeb;
import com.natisoftnavigazione.MainActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by aless on 29/11/2016.
 */

public class NdefWriterTask extends AsyncTask<Object, Void, String[]> {
    private boolean writable;
    private Activity ActivityMain;

    @Override
    protected void onPreExecute() {}

    private String[] write(String text, Tag tag) throws IOException, FormatException {
        //String idS = tag.getId().toString();
        String cardID = CostantiWeb.bytesToHex(tag.getId());
        String cardIdArr[] = cardID.split(" ");
        String reverseCardId = "";
        for (int i=0;i < cardIdArr.length;i++) { reverseCardId += cardIdArr[i]; }
        String idS = reverseCardId;

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();

        String[] obj = new String[2];
        obj[0] = idS;
        obj[1] = text;
        return obj;
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;
        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);
        return recordNFC;
    }

    @Override
    protected String[] doInBackground(Object... params) {
        Tag tag = (Tag)params[0];
        this.ActivityMain = (MainActivity)params[1];
        String[] obj = new String[2];

        Ndef ndef = Ndef.get(tag);
        if(ndef!=null) {
            writable = ndef.isWritable();
            if (writable) {
                try { obj = write(RichiestaNFC.getNFCtagToWrite(), tag); }
                catch (IOException e) { e.printStackTrace(); }
                catch (FormatException e) { e.printStackTrace(); }
            }
        }
        return obj;
    }

    @Override
    protected void onPostExecute(String[] result) {
        //Avvisi.avviso(ActivityMain, "SCRIVO : "+RichiestaNFC.getNFCtagToWrite(), "errore");
        MainActivity.NavigazioneFragment frm = (MainActivity.NavigazioneFragment) ActivityMain.getFragmentManager().findFragmentByTag("position"+0);
        WebView webView = frm.getWebView();

        if(writable) {
            if(webView!=null) {
                webView.loadUrl("javascript:callBackNFCscrittura('" + result[1] + "')");
                webView.loadUrl("javascript:callBackNFCscrittura2('" + result[0] + "' , '" + result[1] + "')");
            }
        } else {
            String msgErrore = "tag nfc non scrivibile!";
            //Avvisi.avviso(ActivityMain, msgErrore, "errore");
            if(webView!=null) webView.loadUrl("javascript:callBackNFCerrore('"+msgErrore+"')");
        }
    }
}
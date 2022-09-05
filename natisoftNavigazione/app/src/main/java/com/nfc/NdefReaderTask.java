package com.nfc;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

import com.costanti.CostantiWeb;
import com.natisoftnavigazione.MainActivity;
import com.utility.Avvisi;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by aless on 29/11/2016.
 */

public class NdefReaderTask extends AsyncTask<Object, Void, String[]> {

    private Activity ActivityMain;

    @Override
    protected void onPreExecute()
    {
    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException {

        byte[] payload = record.getPayload();
        String utf8 = "UTF-8";
        String utf16 = "UTF-16";

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? utf8 : utf16;

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    @Override
    protected String[] doInBackground(Object... params) {
        Tag tag = (Tag)params[0];
        this.ActivityMain = (MainActivity)params[1];
        //String idS = tag.getId().toString();

        String cardID = CostantiWeb.bytesToHex(tag.getId());
        String cardIdArr[] = cardID.split(" ");
        String reverseCardId = "";
        for (int i=0;i < cardIdArr.length;i++) {
            reverseCardId += cardIdArr[i];
        }
        String idS = reverseCardId;


        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            // NDEF is not supported by this Tag.
            return null;
        }

        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        if(ndefMessage!=null) {
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        String[] obj = new String[2];
                        obj[0] = idS;
                        obj[1] = readText(ndefRecord);
                        return obj;
                    } catch (UnsupportedEncodingException e) {
                        Log.e("", "Unsupported Encoding", e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result[1] != null) {
            MainActivity.NavigazioneFragment frm = (MainActivity.NavigazioneFragment) ActivityMain.getFragmentManager().findFragmentByTag("position"+0);
            WebView webView = frm.getWebView();
            Log.d("NdefReaderTask", "pagina corrente >"+result[1]+"<>"+result[0]);  webView.getUrl();
            if(webView!=null) {
                Log.d("NdefReaderTask", "invoco le call back di lettura con >"+result[1]+"<>"+result[0]);
                webView.loadUrl("javascript:callBackNFClettura('" + result[1] + "')");
                webView.loadUrl("javascript:callBackNFClettura2('" + result[0] + "' , '" + result[1] + "')");
            }
        }
        else Avvisi.avviso(ActivityMain, "tag NFC vuoto", "errore");
    }
}
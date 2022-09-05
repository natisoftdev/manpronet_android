package com.costanti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  CostantiWeb {
	public final static String SERVER_HOST = "manpronet.com";
	public static String INDIRIZZO_PORTALE = "";
	public static String INDIRIZZO_PORTALE_GENERICO = "";
	public static String INDIRIZZO_PORTALE_ENCODING = "";
	public static String NAME_ODBC = "";
	public static String PAGINA_INS_RFID = "/mobile/dati_inviati_da_app/salva_rfid.php";

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	//public static List<String> arrayPortali = new ArrayList<>();
	public static List<CharSequence> chooseDB = new ArrayList<>();
	public static KeyStore keyStore = null;

	public final static Map<String, Integer> idNotificaArray = implementIdNotifiche();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		try { while ((line = reader.readLine()) != null) { sb.append(line).append("\n"); } }
		catch (IOException e) { e.printStackTrace();}
		finally { try { is.close();	} catch (IOException e) { e.printStackTrace(); } }
		return sb.toString();
	}

	public static String getPAGINA_INS_RFID(){
		String str = "";
		String indirizzo = INDIRIZZO_PORTALE_GENERICO.substring(0, INDIRIZZO_PORTALE_GENERICO.lastIndexOf("/"));
		//Log.d("AAAAAA", "indirizzo_rfid: " + indirizzo);
		str = indirizzo + PAGINA_INS_RFID;
		//Log.d("AAAAAA", "indirizzo_rfid: " + indirizzo_rfid);
		return str;
	}

	public static Map<String, Integer> implementIdNotifiche(){
		Map<String, Integer> params = new HashMap<>();
		params.put("trasporto_pazienti", 232);
		params.put("manutenzione", 333);
		return params;
	}
}
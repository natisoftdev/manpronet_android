package com.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Avvisi {
	public static void avviso(final Context context, final String msg, String tipo) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		int codice = Integer.parseInt( sharedPrefs.getString("avvisiErroriConnessione", "2") );

		/*
		if(tipo=="errore") {
			if( codice==1 || codice==2 ) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
		else if(tipo=="avviso") {
			if( codice==2 ) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}*/

		((Activity)context).runOnUiThread(new Runnable() {
			public void run() { Toast.makeText(context, msg, Toast.LENGTH_LONG).show(); }
		});
	}
}
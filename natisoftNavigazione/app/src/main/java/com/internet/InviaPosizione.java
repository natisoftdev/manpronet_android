package com.internet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.costanti.CostantiWeb;
import com.natisoftnavigazione.MainActivity;
import com.natisoftnavigazione.R;
import com.utility.Avvisi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static com.costanti.CostantiWeb.convertStreamToString;

public class InviaPosizione extends AsyncTask<Object, Object, Object> {

	//private Activity activityRif;
	private String TAG = "InviaPosizione";

	//HttpURLConnection urlConnection;

	protected void onPreExecute() { }

	protected void onPostExecute(Object result) { }

	@Override
	protected Void doInBackground(Object... params) {
        try
        {
			//activityRif = (Activity) params[0];
            Context ctx = (Context) params[0];
			double lat = (double) params[1];
			double lng = (double) params[2];

			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(ctx);

			String serverPage = CostantiWeb.INDIRIZZO_PORTALE_GENERICO+"/build/manpronet/servizi-report/invio_posizione_risorsa/json-events-invio-posizione.php";

			Log.d(TAG, ">>>>>>"+serverPage);

			String username = sharedPrefs.getString("prefDBUsername", "");
			String password = sharedPrefs.getString("prefDBPassword", "");

			String name_odbc = CostantiWeb.NAME_ODBC;

			Log.d(TAG, "name_odbc:"+name_odbc);
			Log.d(TAG, "username:"+username);
			Log.d(TAG, "password:"+password);

			if(name_odbc.length()>0 && username.length()>0 && password.length()>0) {
				// Create a TrustManager that trusts the CAs in our KeyStore
				String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
				tmf.init(CostantiWeb.keyStore);

				// Create an SSLContext that uses our TrustManager
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null, tmf.getTrustManagers(), null);

				//URLEncoder.encode(StringaAccesso, "UTF-8"
				String urlParameters = "name_odbc=" + name_odbc + "&username=" + username + "&password=" + password+"&lat="+lat+"&lng="+lng;

				Log.d(TAG, ">>>>>>"+urlParameters);

				byte[] postData = urlParameters.getBytes("UTF-8");
				int postDataLength = postData.length;
				URL url = new URL(serverPage);
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setInstanceFollowRedirects(false);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("charset", "utf-8");
				conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
				conn.setUseCaches(false);

				conn.setSSLSocketFactory(context.getSocketFactory());

				//Send request
				DataOutputStream wr = new DataOutputStream(
						conn.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();

				InputStream in = conn.getInputStream();

				String json = convertStreamToString(in);

				Log.d(TAG, "json1: " + json);

				/*try {
					JSONArray jArray = new JSONArray(json);

					JSONObject obj = (JSONObject) jArray.get(0);

					String strOk = obj.getString("risposta");

					Log.e(TAG, "risposta: " + strOk);
				} catch (JSONException e) {
					//Avvisi.avviso(activityRif, activityRif.getApplicationContext().getString(R.string.erroreJson1), "errore");
					//SenderMail.inviaMailMalfunzionamento(activityRif, "Errore in RisoluzioneIndirizzoPortale", "JSONException: "+e);
					Log.e(TAG, "JSONException: " + e);
				}
				*/
			}
        }
        catch (Exception ex)
        {
            Log.e("Exception 2",  ""+ex );
            ex.printStackTrace();

            //String msg = activityRif.getApplicationContext().getString(R.string.erroreJson1)+" "+ex;

            Log.e(TAG,  "ClientProtocolException"  );
        }

	    return null;
	}

}  

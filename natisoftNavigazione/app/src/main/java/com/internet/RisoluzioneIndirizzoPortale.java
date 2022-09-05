package com.internet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.costanti.CostantiWeb;
import com.natisoftnavigazione.MainActivity;
import com.natisoftnavigazione.R;
import com.natisoftnavigazione.UserSettingActivity;
import com.utility.Avvisi;

 

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static com.costanti.CostantiWeb.convertStreamToString;

public class RisoluzioneIndirizzoPortale extends AsyncTask<Object, Object, Object> {

	//private Handler mHandler = new Handler(Looper.getMainLooper());

	private String indirizzoPortale;
	private String indirizzoPortaleEncoding;

    private String NameOdbc;
	private String StringaAccesso;
	private Activity activityRif;

	//HttpURLConnection urlConnection;

	protected void onPreExecute() {
		indirizzoPortale = ""; 
	}

	protected void onPostExecute(Object result) {

		if( indirizzoPortale.length() > 0 )
		{
			CostantiWeb.INDIRIZZO_PORTALE = indirizzoPortale;

			if( indirizzoPortaleEncoding.length() > 0 )
				CostantiWeb.INDIRIZZO_PORTALE_ENCODING = indirizzoPortaleEncoding;
			else
				CostantiWeb.INDIRIZZO_PORTALE_ENCODING = "ansi";

			if( NameOdbc.length() > 0 )
				CostantiWeb.NAME_ODBC = NameOdbc;
			else
				CostantiWeb.NAME_ODBC = "";

            String str = CostantiWeb.INDIRIZZO_PORTALE;
            int index = str.lastIndexOf('/');
            CostantiWeb.INDIRIZZO_PORTALE_GENERICO = str.substring(0,index);

            CostantiWeb.INDIRIZZO_PORTALE_GENERICO =  (CostantiWeb.INDIRIZZO_PORTALE_GENERICO).replace("mobile","");

            /*
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(( (MainActivity) activityRif ));
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("indirizzo_portale", CostantiWeb.INDIRIZZO_PORTALE_GENERICO);
            editor.putString("nome_odbc", CostantiWeb.NAME_ODBC);
            editor.commit();
            */



			Avvisi.avviso(activityRif, activityRif.getApplicationContext().getString(R.string.reload), "avviso");
		}
		else
		{
			CostantiWeb.INDIRIZZO_PORTALE = "";
			Avvisi.avviso(activityRif, activityRif.getApplicationContext().getString(R.string.stringa_non_riconosciuta), "errore");
		}

		( (MainActivity) activityRif ).reloadFragmentAtIndex(0); //ricarichiamo il fragment di navigazione

	}

	@Override
	protected Void doInBackground(Object... params) {

		StringaAccesso = "" +params[0];
		activityRif = (Activity) params[1];
		String serverPage = "https://"+CostantiWeb.SERVER_HOST+"/attracco_android/risolutore_indirizzo/json-events-login.php";

		Log.d("Indirizzo di collegamento",serverPage);

        try
        {
            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(CostantiWeb.keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            //URLEncoder.encode(StringaAccesso, "UTF-8"
            String urlParameters  = "StringaAccesso="+StringaAccesso;
            byte[] postData       = urlParameters.getBytes(StandardCharsets.UTF_8);
            int    postDataLength = postData.length;
            URL    url            = new URL( serverPage );
            HttpsURLConnection conn= (HttpsURLConnection) url.openConnection();
            conn.setDoOutput( true );
            conn.setInstanceFollowRedirects( false );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            conn.setUseCaches( false );

            conn.setSSLSocketFactory(context.getSocketFactory());

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    conn.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();

            InputStream in = conn.getInputStream();

            String json = convertStreamToString(in);

            try
            {
                JSONArray jArray = new JSONArray(json);

                JSONObject obj = (JSONObject) jArray.get(0);

                indirizzoPortale = obj.getString("indirizzoPortale");
                indirizzoPortaleEncoding = obj.getString("indirizzoPortaleEncoding");
				NameOdbc = obj.getString("NameOdbc");
            }
            catch (JSONException e)
            {
                Avvisi.avviso(activityRif, activityRif.getApplicationContext().getString(R.string.erroreJson1), "errore");
                //SenderMail.inviaMailMalfunzionamento(activityRif, "Errore in RisoluzioneIndirizzoPortale", "JSONException: "+e);
                Log.e("risolutore indirizzo",  "JSONException: "+e );
            }
        }
        catch (Exception ex)
        {
            Log.e("Exception 2",  ""+ex );
            ex.printStackTrace();

            String msg = activityRif.getApplicationContext().getString(R.string.erroreJson1)+" "+ex;

            Avvisi.avviso(activityRif, msg, "errore");

            Log.e("risolutore indirizzo",  "ClientProtocolException"  );
        }

	    return null;
	}


    public String getNameOdbc() {
        return NameOdbc;
    }


	/*
	private static String convertStreamToString(InputStream is) {

	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append((line + "\n"));
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}*/

}  

package com.natisoftnavigazione;

import static com.costanti.CostantiWeb.convertStreamToString;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.costanti.CostantiWeb;
import com.database.myDbAdapter;
import com.nfc.NdefReaderTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.internet.RisoluzioneIndirizzoPortale;
import com.nfc.NdefWriterTask;
import com.nfc.RichiestaNFC;
import com.servizi.BackgroundTicketService;
import com.utility.Avvisi;
import com.utility.MobileData;
import com.utility.Geolocalizzazione;
import android.webkit.JavascriptInterface;

import android.preference.PreferenceManager;
import android.provider.MediaStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks   {

	private NavigationDrawerFragment mNavigationDrawerFragment; // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	private CharSequence mTitle; // Used to store the last screen title. For use in {@link #restoreActionBar()}.
	private  ArrayList<Fragment> listFragment = new ArrayList<Fragment>();
	private static final int RESULT_SETTINGS = 1;
	private ValueCallback<Uri> mUploadMessage;
	private ValueCallback<Uri[]> mUploadMessage2;
	private static final int FILECHOOSER_RESULTCODE = 222;
	private static final int INPUT_FILE_REQUEST_CODE = 222;
    static String currentPhotoPath;
	private String mCameraPhotoPath;
	static Uri capturedImageUri=null;
	private NfcAdapter mNfcAdapter;
	public static final String MIME_TEXT_PLAIN = "text/plain";
	private Activity ActivityMain;

	static String stringaConnessione="";
	static String TAG = "MainActivity";
	//public BackgroundService gpsService;
	public BackgroundTicketService ticketService;
	public SharedPreferences sharedPrefs = null;

	public static PermissionRequest chromeWebViewRequest;
	private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 111;

	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

	private int PERMISSION_ALL = 1;
	private String[] PERMISSIONS = {
			android.Manifest.permission.READ_PHONE_STATE,
			android.Manifest.permission.CAMERA,
			android.Manifest.permission.INTERNET,
			android.Manifest.permission.ACCESS_NETWORK_STATE,
			android.Manifest.permission.ACCESS_COARSE_LOCATION,
			android.Manifest.permission.ACCESS_FINE_LOCATION,
			android.Manifest.permission.READ_EXTERNAL_STORAGE,
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
			android.Manifest.permission.NFC,
			android.Manifest.permission.NFC_TRANSACTION_EVENT,
			android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
			android.Manifest.permission.WAKE_LOCK,
			android.Manifest.permission.FOREGROUND_SERVICE,
			android.Manifest.permission.RECORD_AUDIO,
			android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
			android.Manifest.permission.CAPTURE_AUDIO_OUTPUT,
			//android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		ActivityMain = this;

		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream caInput2 = new BufferedInputStream(getClass().getResourceAsStream("/assets/cert/manpronet.pem") );
			Certificate ca;
			ca = cf.generateCertificate(caInput2);
			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			CostantiWeb.keyStore = KeyStore.getInstance(keyStoreType);
			CostantiWeb.keyStore.load(null, null);
			CostantiWeb.keyStore.setCertificateEntry("SSL", ca);
		}
		catch (Exception ex) {
			String msg = getApplicationContext().getString(R.string.erroreSsl);
			Avvisi.avviso(this, msg, "errore");
			Log.e("Exception in keystore", ""+ ex );
			ex.printStackTrace();
		}

		if(savedInstanceState==null) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean prefLocaliz = sharedPrefs.getBoolean("prefControlliAvvioLocalizzazione", true);
			boolean prefRete = sharedPrefs.getBoolean("prefControlliAvvioRete", true);

			if(prefLocaliz) Geolocalizzazione.enableLoaction(this);
			if(prefRete) MobileData.setMobileDataEnabled( this, true);

			caricaUserSettings();
		}

		setContentView(R.layout.activity_main);
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		//File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		//path.mkdirs();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			path.mkdirs();
		}
		else{
			// below Android Q
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			path.mkdirs();
		}

		if (mNfcAdapter != null) RichiestaNFC.inizializza();
		ArrayList<String> permissions = new ArrayList<String>();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Android M Permission check
			if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.richiestaPosizione)); //Questa app richiede l'accesso alla posizione GPS per mostrare gli interventi più vicini
				builder.setMessage(getString(R.string.mexRichiestaPosizione));
				builder.setPositiveButton(getString(android.R.string.ok), null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@TargetApi(23)
					@Override
					public void onDismiss(DialogInterface dialog) {
						requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
								PERMISSION_REQUEST_COARSE_LOCATION);

						if(!hasPermissions(ActivityMain, PERMISSIONS)){
							ActivityCompat.requestPermissions(ActivityMain, PERMISSIONS, PERMISSION_ALL);
						}
					}

				});
				builder.show();
			}

         if(!hasPermissions(this, PERMISSIONS)){ ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL); }
     }

		String permission = "android.permission.ACCESS_FINE_LOCATION";
		int res = this.checkCallingOrSelfPermission(permission);

		gestioneFullScreen();

	}

	public static boolean hasPermissions(Context context, String... permissions) {
		if (context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) { return false;
				}
			}
		}
		return true;
	}

	public static void webViewChromeAskForPermission(MainActivity mainActivity, String origin, String permission, int requestCode) {
		Log.d("WebView", "inside askForPermission for" + origin + "with" + permission);


		if (ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(),
				permission)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
					permission)) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(mainActivity,
						new String[]{permission},
						requestCode);
			}
		} else {
			chromeWebViewRequest.grant(chromeWebViewRequest.getResources());
		}
	}


	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			String name = className.getClassName();
			if (name.endsWith("BackgroundService")) {
				//gpsService = ((BackgroundService.LocationServiceBinder) service).getService();
				ticketService = ((BackgroundTicketService.BackgroundTicketServiceBinder) service).getService();
				//btnStartTracking.setEnabled(true);
				//txtStatus.setText("GPS Ready");
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			if (className.getClassName().equals("BackgroundService")) {
				//gpsService = null;
				ticketService = null;
			}
		}
	};


	@Override
	protected void onResume() {
		super.onResume();
		if(mNfcAdapter!=null) setupForegroundDispatch(this, mNfcAdapter);
		//modifiche per bosh - menu opzioni dopo login
		try {
			NavigazioneFragment frm = (NavigazioneFragment) getFragmentManager().findFragmentByTag("position"+0);
			WebView webView = frm.getWebView();
			if(webView!=null) webView.loadUrl("javascript:resumeThreads()");
		}
		catch(Exception ex) { Log.d(this.getClass().getSimpleName(), "javascript:resumeThreads()", ex); }
		gestioneFullScreen();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(mNfcAdapter!=null) stopForegroundDispatch(this, mNfcAdapter);
		//modifiche per bosh - menu opzioni dopo login
		try {
			NavigazioneFragment frm = (NavigazioneFragment) getFragmentManager().findFragmentByTag("position"+0);
			WebView webView = frm.getWebView();
			if(webView!=null) webView.loadUrl("javascript:stopThreads()");
		}
		catch(Exception ex) { Log.d(this.getClass().getSimpleName(), "javascript:stopThreads()", ex); }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		handleIntent(intent);
	}

	private void gestioneFullScreen(){
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean prefFullScreen = sharedPrefs.getBoolean("prefControlliFullScreen", false);

		if(prefFullScreen) {
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
							| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
							| View.SYSTEM_UI_FLAG_IMMERSIVE);
			getActionBar().hide();
		}
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();

		//Avvisi.avviso(ActivityMain, "handleIntent " + action, "avviso");
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equalsIgnoreCase(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

			//String type = intent.getType();

			if(RichiestaNFC.getNFCintentoWeb().equalsIgnoreCase("write")) //scrittura
			{
				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				if(RichiestaNFC.valido()) {
					RichiestaNFC.inizializza();

					new NdefWriterTask().execute((Object)tag, (Object)ActivityMain);
				}
				else
					Avvisi.avviso(ActivityMain, getApplicationContext().getString(R.string.richiesta_nfc_scaduta), "avviso");
			}
			else //lettura
			{
				if (MIME_TEXT_PLAIN.equalsIgnoreCase(type)) {
					Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
					new NdefReaderTask().execute((Object)tag, (Object)ActivityMain);
				}
				else {
					Avvisi.avviso(ActivityMain, getApplicationContext().getString(R.string.nfc_vuoto), "avviso");
				}
			}
		}
		else if (NfcAdapter.ACTION_TECH_DISCOVERED.equalsIgnoreCase(action)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String[] techList = tag.getTechList();
			String searchedTech = Ndef.class.getName();
			for (String tech : techList) {
				if (searchedTech.equals(tech)) {
					if(RichiestaNFC.getNFCintentoWeb().equals("write")) {
						if(RichiestaNFC.valido()) {
							RichiestaNFC.inizializza();
							new NdefWriterTask().execute((Object)tag, (Object)ActivityMain);
						}
						else
							Avvisi.avviso(ActivityMain, getApplicationContext().getString(R.string.richiesta_nfc_scaduta), "avviso");
					}
					else
						new NdefReaderTask().execute((Object)tag, (Object)ActivityMain);

					break;
				}
			}
		}
		else if ( type != null && type.equalsIgnoreCase("text/plain") && action.equalsIgnoreCase("android.intent.action.SEND")){
			Log.d("AAAAAAAAAAAA","" + intent);
			//Log.d("AAAAAAAAAAAA","path -> " + intent.getStringExtra(Intent.EXTRA_TEXT) );
			StringTokenizer st = new StringTokenizer( intent.getStringExtra(Intent.EXTRA_TEXT) , "\r\n\r\n" );
			int count = 0;

			myDbAdapter helper = new myDbAdapter(this);

			while (st.hasMoreElements()) {
				String riga = st.nextToken();
				Log.d("AAAAAAAAAAAA",riga);

				if( count != 0 ){
					StringTokenizer st2 = new StringTokenizer( riga , "," );

					String Type,Identifier,Signal,SeenCount,FirstSeen,LastSeen,ASCII,RSSI = "";
					Type = st2.nextToken();
					//Log.d("AAAAAAAAAAAA","Type: " + Type);
					Identifier = st2.nextToken().replace("\"","");
					//Signal = new StringBuffer().append(st2.nextToken() ).append( st2.nextToken() ).toString();
					//Log.d("AAAAAAAAAAAA","Signal: " + Signal);
					//SeenCount = st2.nextToken();
					//Log.d("AAAAAAAAAAAA","SeenCount: " + SeenCount);
					//FirstSeen = st2.nextToken();
					//Log.d("AAAAAAAAAAAA","FirstSeen: " + FirstSeen);
					//LastSeen = st2.nextToken();
					//Log.d("AAAAAAAAAAAA","LastSeen: " + LastSeen);
					//ASCII = st2.nextToken();
					//Log.d("AAAAAAAAAAAA","ASCII: " + ASCII);
					//RSSI = st2.nextToken();

					Log.d("AAAAAAAAAAAA","Identifier: " + Identifier);

					helper.insertData(Identifier);
				}
				count++;
			}

			//INVIO I DATI A PHP
			String datiRfid = helper.getData();
			//Log.d("AAAAAAAAAAAA",data);
			String nameOdbc = CostantiWeb.NAME_ODBC;
			Log.d("AAAAAAAAAAAA", "nameOdbc: " + nameOdbc);

			datiRfid = datiRfid.substring(0,( datiRfid.length() - 1 ) );
			Log.d("AAAAAAAAAAAA", datiRfid);

			sendDatiRfid(nameOdbc,datiRfid);

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( MainActivity.this);
			sharedPrefs.edit();

			Calendar  calendar = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			String dateNow = dateFormat.format(calendar.getTime());

			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			editor.putString("lastDataInvioRfid", dateNow);
			editor.commit();

		}
	}

	private void sendDatiRfid( String nameOdbc, String datiRfid){
		String linkRfid = CostantiWeb.getPAGINA_INS_RFID();

		Log.d("AAAAAA", "Indirizzo di collegamento: " + linkRfid);

		RequestQueue reQueue = Volley.newRequestQueue(MainActivity.this);

		StringRequest request=new StringRequest(com.android.volley.Request.Method.POST,
				linkRfid,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Cosa ricevo: "+response);

						try {
							JSONObject jsonObject = new JSONObject(response);
							//JSONArray jsonArray = new JSONArray(response);
							//for (int i = 0; i < jsonArray.length(); i++){
							//JSONObject jsonObject = jsonArray.getJSONObject(0);
							//Devo aggiungere i dati nel DB
							//ContentValues record = new ContentValues();

							Log.d(TAG,jsonObject.toString());

							int result = jsonObject.getInt("esito");
							String msgErrore = jsonObject.getString("msg");
							if(result == 1){
								Log.d("AAA","Devo pulire il DB");
								myDbAdapter helper = new myDbAdapter(MainActivity.this);
								int count = helper.deleteAll();
								Log.d("AAA","Dati cancellati: " + count);
								Toast.makeText(MainActivity.this, msgErrore , Toast.LENGTH_SHORT).show();
								String test = helper.getData();
								Log.d("AAA","Cosa c'è dentro al DB? -> -" + test + "-");
							}else{
								Toast.makeText(MainActivity.this, msgErrore , Toast.LENGTH_SHORT).show();
							}
							/*String indirizzoPortaleEncoding = jsonObject.getString("indirizzoPortaleEncoding");
							String NameOdbc = jsonObject.getString("NameOdbc");

							Log.d(TAG,indirizzoPortale + " - " + indirizzoPortaleEncoding + " - " + NameOdbc);

							//Devo Salvare Dominio di indirizzoPortale in pref_indirizzoInvio
							String dominio = Costanti.extractDomain(indirizzoPortale);

							SharedPreferences.Editor editor = preferences.edit();
							editor.putString("pref_indirizzoInvio", dominio);
							editor.putString("pref_db_string", NameOdbc);
							editor.commit();

							String indirizzoInvio = preferences.getString("pref_indirizzoInvio", dominio);
							//Devo Salvare NameOdbc in
							String db_connect = preferences.getString("pref_db_string", NameOdbc);

							Log.d(TAG,indirizzoInvio  + " - " + indirizzoPortaleEncoding + " - " + db_connect);
							*/
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				},

				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(
							VolleyError error) {
						Log.e(TAG, ""+error);
						error.printStackTrace();
						Toast.makeText(MainActivity.this, "Errore invio dati", Toast.LENGTH_SHORT).show();
					}
				})
		{
			@Override
			protected Map<String, String> getParams()
			{
				Map<String, String> params = new HashMap<>();
				//params.put("Content-Type", "application/json; charset=ansi");
				params.put( "Content-Type", "application/x-www-form-urlencoded");
				params.put( "charset", "utf-8");

				params.put("nameOdbc", ""+nameOdbc);
				params.put("datiRfid", ""+datiRfid);

				return params;
			}
		};
		try{ reQueue.add(request); }
		catch(Exception e){ Log.e(TAG, ""+e); }
/*
		try
		{
			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(CostantiWeb.keyStore);

			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);

			//String urlParameters  = "StringaAccesso="+StringaAccesso;
			byte[] postData       = post.getBytes(StandardCharsets.UTF_8);
			int    postDataLength = postData.length;
			URL url            = new URL( linkRfid );
			HttpsURLConnection conn= (HttpsURLConnection) url.openConnection();
			conn.setDoOutput( true );
			conn.setInstanceFollowRedirects( false );
			conn.setRequestMethod( "POST" );
			conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty( "charset", "ansi");
			conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
			conn.setUseCaches( false );

			conn.setSSLSocketFactory(context.getSocketFactory());

			//Send request
			DataOutputStream wr = new DataOutputStream(
					conn.getOutputStream ());
			wr.writeBytes (post);
			wr.flush ();
			wr.close ();

			InputStream in = conn.getInputStream();

			String json = convertStreamToString(in);
		}
		catch (Exception ex)
		{
			Log.e("Exception 2",  ""+ex );
			ex.printStackTrace();
			Log.e("risolutore indirizzo",  "ClientProtocolException"  );
		}
*/
	}

	public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
		adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
	}

	public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

	public void onBackPressed() {
		Avvisi.avviso( this, getApplicationContext().getString(R.string.tasto_disabilitato), "avviso");
	}

	public void reloadFragmentAtIndex(int idx) {
		Fragment fragment = (Fragment) getFragmentManager().findFragmentByTag("position"+idx);
		if(fragment!=null) ( (NavigazioneFragment)fragment).init();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Fragment frm = null;
		frm = (Fragment) getFragmentManager().findFragmentByTag("position"+position);
		if(frm==null) {
			if(position==0) {
				frm = NavigazioneFragment.newInstance(position + 1);
				frm.setRetainInstance(true);
			}
			else  if(position==1) {
				frm = ImpostazioniFragment.newInstance(position + 1);
				frm.setRetainInstance(true);
			}
			else  if(position==3) {
				frm = RfidFragment.newInstance(position + 1);
				frm.setRetainInstance(true);
			}
		}

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, frm, "position"+position ).commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
			case 1:
				mTitle = getString(R.string.title_section1);
				break;
			case 2:
				mTitle = getString(R.string.title_section2);
				break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	public void avviaUserSettingActivity() {
		Intent i = new Intent(this, UserSettingActivity.class);
		startActivityForResult(i, RESULT_SETTINGS);
	}

	public String getImagePath(Uri uri){
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		String document_id = cursor.getString(0);
		document_id = document_id.substring(document_id.lastIndexOf(":")+1);
		cursor.close();

		cursor = getContentResolver().query(
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);

		String path = "";

		if (cursor != null && cursor.moveToFirst()) {
			final int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			path = cursor.getString(index);
		}


		return path;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("natisoft", "requestCode: " + requestCode);
		Log.d("natisoft", "resultCode: " + resultCode);
		Log.d("natisoft", "data: " + data);
		Log.d("natisoft","currentPhotoPath: " + currentPhotoPath);
		Log.d("natisoft", "mUploadMessage: " + mUploadMessage);
		Log.d("natisoft", "capturedImageUri: " + capturedImageUri);


		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		int livCompress = sharedPrefs.getInt("livCompress", 20);

		//livCompress = 100 - livCompress;
		if (requestCode==RESULT_SETTINGS) { caricaUserSettings(); }
		else if(requestCode==FILECHOOSER_RESULTCODE) {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

				if (requestCode != INPUT_FILE_REQUEST_CODE || mUploadMessage2 == null) {
					super.onActivityResult(requestCode, resultCode, data);
					return;
				}

				Uri[] results = null;
				String uri_canc = "";
				// Check that the response is a good one
				if (resultCode == Activity.RESULT_OK) {
					if (data == null) {
						// If there is not data, then we may have taken a photo
						if (mCameraPhotoPath != null) {
							results = new Uri[]{Uri.parse(mCameraPhotoPath)};
							uri_canc  = mCameraPhotoPath;
						}
					} else {
						String dataString = data.getDataString();
                        //String dataString = data.getData().toString();
						if (dataString != null) {
							results = new Uri[]{Uri.parse(dataString)};
							uri_canc  = dataString;
						}
					}
				}

				try {

					InputStream ims = getContentResolver().openInputStream( Uri.parse(uri_canc) );
					Bitmap bitmap = BitmapFactory.decodeStream(ims);
					if(bitmap!=null) {
						File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
						Calendar cal = Calendar.getInstance();
						String extTmpFile = ".jpg";
						String nameTmpFile = "img_compress_" + cal.getTimeInMillis();
						File outputFile = File.createTempFile(nameTmpFile, extTmpFile, storageDir);

						OutputStream os = new FileOutputStream(outputFile);
						bitmap.compress(Bitmap.CompressFormat.JPEG, livCompress, os);
						os.flush();
						os.close();
						Uri ut = Uri.fromFile(outputFile);
						results = new Uri[]{ut};
					}
				} catch (Exception e) {
					Log.d("onActivityResult - comp", ""+e);
					e.printStackTrace();
				}
				mUploadMessage2.onReceiveValue(results);
				mUploadMessage2 = null;
			}
			else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

                Uri[] results = null;
                String uri_canc = "";
				File fileToSEnd = null;

				/// FILE GALLERY

                if(data != null && data.getData() != null){

					Uri uri = data.getData();

					String path = getImagePath(uri);
					final String id = DocumentsContract.getDocumentId(uri);
					results = new Uri[]{uri};
					uri_canc  = "file://"+path;

					fileToSEnd = new File(path);
				} else {
					// fotocamera
					fileToSEnd = new File(currentPhotoPath);
					results = new Uri[]{Uri.parse(currentPhotoPath)};

					uri_canc  = "file://"+currentPhotoPath;


				}

                // Get length of file in bytes
                long fileSizeInBytes = fileToSEnd.length();
                // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                long fileSizeInKB = fileSizeInBytes / 1024;
                // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                long fileSizeInMB = fileSizeInKB / 1024;

                // *********************************************************************************
                // POPUP> DIMENSIONI DELL'IMMAGINE
				//Avvisi.avviso( this, "FOTO CAMERA DIM: -> " + fileSizeInKB, "avviso");
				// *********************************************************************************


                try {
                    //InputStream ims = getContentResolver().openInputStream( Uri.parse(uri_canc) );
                    InputStream ims = getContentResolver().openInputStream( Uri.parse(uri_canc) );

                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
                    if(bitmap!=null) {
						// OutputStream os = new FileOutputStream(fileToSEnd); // TODO: controllare, prima andava senza getAbs
						OutputStream os = new FileOutputStream(fileToSEnd.getAbsolutePath()); // TODO: controllare, prima andava senza getAbs
                        bitmap.compress(Bitmap.CompressFormat.JPEG, livCompress, os);
                        os.flush();
                        os.close();
                        Uri ut = Uri.fromFile(fileToSEnd);

                        results = new Uri[]{ut};
                    }
                    else{
						OutputStream os_2 = new FileOutputStream(fileToSEnd);
						copy(ims,os_2);
						bitmap.compress(Bitmap.CompressFormat.JPEG, livCompress, os_2);
					}
                    // Get length of file in bytes
                    fileSizeInBytes = fileToSEnd.length();
                    // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                    fileSizeInKB = fileSizeInBytes / 1024;
                    // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                    fileSizeInMB = fileSizeInKB / 1024;

                } catch (Exception e) {
                    Log.e("onActivityResult - comp", ""+e);
                    e.printStackTrace();
                }
                mUploadMessage2.onReceiveValue(results);
                mUploadMessage2 = null;
			}
		}
		else if(requestCode==IntentIntegrator.REQUEST_CODE) {
			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

			if(result.getContents() == null) { Avvisi.avviso(ActivityMain, getApplicationContext().getString(R.string.annullato_barcode), "errore"); }
			else {
				String ris = result.getContents();
				Avvisi.avviso(ActivityMain, getApplicationContext().getString(R.string.msg_letto)+ris, "avviso");
				NavigazioneFragment frm = (NavigazioneFragment) getFragmentManager().findFragmentByTag("position"+0);
				WebView webView = frm.getWebView();
				if(webView!=null){ webView.loadUrl("javascript:callBackLetturaQR('"+ris+"')");}
			}
		}
		else { super.onActivityResult(requestCode, resultCode, data); }
	}

	private void caricaUserSettings() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		/*
		boolean enableLoc = sharedPrefs.getBoolean("attivazioneServizioLocalizzazione", false);

		if(enableLoc) {
			if (gpsService != null) { gpsService.startTracking(); }
			else {

				final Intent intent = new Intent(this.getApplication(), BackgroundService.class);
				this.getApplication().startService(intent);
				//this.getApplication().startForegroundService(intent);
				this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					this.getApplication().startForegroundService(intent);
				}
			}
		}
		else { if (gpsService != null) gpsService.stopTracking(); }
		*/

		boolean abilitaNotificaTicket = sharedPrefs.getBoolean("attivazioneServizioNotificheTicket", false);

		if(abilitaNotificaTicket) {
			if (ticketService != null) { ticketService.startTracking(); }
			else {

				final Intent intent = new Intent(this.getApplication(), BackgroundTicketService.class);
				this.getApplication().startService(intent);
				this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					this.getApplication().startForegroundService(intent);
				}
			}
		}
		else { if (ticketService != null) ticketService.stopTracking(); }



		stringaConnessione = sharedPrefs.getString("stringaConnessione", "");
		String[] arrayPortali = sharedPrefs.getString("prefDBStringa", "").split(",");

		//Se arrayPieno ha + dati e stringaConnessione è vuota (3)
		if(sharedPrefs.getString("prefDBStringa", "").split(",").length > 1 && stringaConnessione.length() == 0){
			sharedPrefs.edit().putString("stringaConnessione", arrayPortali[0]).apply();
			stringaConnessione = arrayPortali[0];
		}
		//Se arrayPieno ha + dati e stringaConnessione è piena ma stringaConnessione non è più in array  (5)
		else if(
				sharedPrefs.getString("prefDBStringa", "").split(",").length > 1 &&
				stringaConnessione.length() > 0 &&
				!( sharedPrefs.getString("prefDBStringa", "").contains(stringaConnessione) )){

			sharedPrefs.edit().putString("stringaConnessione", arrayPortali[0]).apply();
			stringaConnessione = arrayPortali[0];
		}
		//Se arrayPortali contiene solo una stringa allora setto quella (1)
		else if(sharedPrefs.getString("prefDBStringa", "").split(",").length == 1) {
			sharedPrefs.edit().putString("stringaConnessione", sharedPrefs.getString("prefDBStringa", "")).apply();
			stringaConnessione = sharedPrefs.getString("prefDBStringa", "");
		}
		else{
			//SITUAZIONE PERFETTA
			//Non bisogna fare niente
		}
		reloadFragmentAtIndex(0);
		new RisoluzioneIndirizzoPortale().execute( (Object)stringaConnessione, (MainActivity) this );
	}

	public static class NavigazioneFragment extends Fragment {
		public static final String ARG_PLANET_NUMBER = "section_number";
		WebView webView;
		View rootView;

		public static NavigazioneFragment newInstance(int sectionNumber) {
			NavigazioneFragment fragment = new NavigazioneFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_PLANET_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			if(rootView==null) {
				rootView = inflater.inflate(R.layout.fragment_web_navigazione, container, false);
				webView = rootView.findViewById(R.id.webView1);
				webView.clearSslPreferences();
				WebSettings webSettings = webView.getSettings();
				webSettings.setJavaScriptEnabled(true);
				webSettings.setSupportZoom(false);
				webSettings.setDefaultTextEncodingName(CostantiWeb.INDIRIZZO_PORTALE_ENCODING);
				webSettings.setAppCacheEnabled(true);
				webSettings.setAppCachePath("/data/data" + (getActivity()).getPackageName() + "/cache");
				webSettings.setSaveFormData(true);
				webSettings.setDatabaseEnabled(true);
				webSettings.setSavePassword(true);
				webSettings.setDomStorageEnabled(true);
				webSettings.setAllowContentAccess(true);
				webSettings.setAllowFileAccess(true);
				webSettings.setMediaPlaybackRequiresUserGesture(false);
				webSettings.setSupportZoom(true);

				/*
				webSettings.setSupportMultipleWindows(true);
				webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
				webSettings.setBuiltInZoomControls(true);
				webSettings.setDisplayZoomControls(false);
				webSettings.setLoadWithOverviewMode(true);
				webSettings.setUseWideViewPort(true);
*/
				//webSettings.setLoadWithOverviewMode(true);
				CookieManager.setAcceptFileSchemeCookies(true);
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.setAcceptCookie(true);


				webView.setWebChromeClient(
						new WebChromeClient() {
							public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) { callback.invoke(origin, true, false); }

							public void onProgressChanged(WebView view, int progress) {
								try {
									MainActivity x = (MainActivity) getActivity();
									x.setTitle(x.getApplicationContext().getString(R.string.msg_attendere));
									x.setProgress(progress * 100);
									if (progress == 100)
										x.setTitle(R.string.app_name);
								} catch (Exception ex) {
									Log.e("WebChromeClieProChanged", "errore :"+ex);
								}
							}

							public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
								Avvisi.avviso( getActivity(), "errorCode: " + errorCode + "\ndescription: " + description + "\nfailingUrl: " + failingUrl, "avviso");
							}

							// For >= Android 5.0
							public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {

								MainActivity mainActivity = ((MainActivity) getActivity());

								// Double check that we don't have any existing callbacks
								if (mainActivity.mUploadMessage2 != null) {	mainActivity.mUploadMessage2.onReceiveValue(null); }
								mainActivity.mUploadMessage2 = filePath;

								Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

								if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

									if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null) {

										// Create the File where the photo should go
										File photoFile = null;
										try { photoFile = createImageFile(mainActivity.getApplicationContext()); }
										catch (IOException ex) {
											// Error occurred while creating the File
											Log.e("seq - onShowFileChooser", "Unable to create Image File", ex);
										}

										// Continue only if the File was successfully created
										if (photoFile != null) {
											mainActivity.mCameraPhotoPath = photoFile.getAbsolutePath();
											takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
													mainActivity.getApplicationContext(),
													"com.natisoftnavigazione.fileProvider",
													photoFile
											));
										} else {
											takePictureIntent = null;
										}
									}
								}
								else{
									if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null) {

										// Create the File where the photo should go
										File photoFile = null;
										try {
											photoFile = createImageFile(mainActivity.getApplicationContext());

											takePictureIntent.putExtra("PhotoPath", photoFile);
										} catch (IOException ex) {
											// Error occurred while creating the File
											Log.e("seq - onShowFileChooser", "Unable to create Image File", ex);
										}

										// Continue only if the File was successfully created
										if (photoFile != null) {
											mainActivity.mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
											takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,	Uri.fromFile(photoFile));
										} else {takePictureIntent = null; }
									}
								}

								Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
								contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
								contentSelectionIntent.setType("image/*");

								Intent[] intentArray;
								if (takePictureIntent != null) {
									intentArray = new Intent[]{takePictureIntent};
								} else { intentArray = new Intent[0]; }

								Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
								chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
								chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
								chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

								currentPhotoPath = mainActivity.mCameraPhotoPath;

								mainActivity.startActivityForResult(chooserIntent, ((MainActivity) getActivity()).INPUT_FILE_REQUEST_CODE);
								return true;
							}

							@Override
							public void onPermissionRequest(final PermissionRequest request) {
								chromeWebViewRequest = request;
								MainActivity mainActivity = ((MainActivity) getActivity());

								for (String permission : request.getResources()) {
									switch (permission) {
										case "android.webkit.resource.AUDIO_CAPTURE": {
											webViewChromeAskForPermission(mainActivity, request.getOrigin().toString(), Manifest.permission.RECORD_AUDIO, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
											break;
										}
									}
								}
							}


						});

				//webView.setWebViewClient(new myWebViewClient());
				webView.setWebViewClient(new WebViewClient() {
											 @Override
											 public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
												 String url = request.getUrl().toString();

												 if (url.startsWith("tel:")) {
													 Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
													 startActivity(intent);
												 } else if (url.startsWith("mailto:")) {
													 String body = "Enter your Question, Enquiry or Feedback below:\n\n";
													 Intent mail = new Intent(Intent.ACTION_SEND);
													 mail.setType("application/octet-stream");
													 mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"email address"});
													 mail.putExtra(Intent.EXTRA_SUBJECT, "Subject");
													 mail.putExtra(Intent.EXTRA_TEXT, body);
													 startActivity(mail);
												 } else if (url.startsWith("http:") || url.startsWith("https:")) {
													 //view.loadUrl(url);
													 return false;
												 }
												 return true;
											 }

											 @Override
											 public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
												 MainActivity mainActivity = ((MainActivity) getActivity());
												 final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
												 String message = "SSL Certificate error.";
												 switch (error.getPrimaryError()) {
													 case SslError.SSL_UNTRUSTED:
														 message = "The certificate authority is not trusted.";
														 break;
													 case SslError.SSL_EXPIRED:
														 message = "The certificate has expired.";
														 break;
													 case SslError.SSL_IDMISMATCH:
														 message = "The certificate Hostname mismatch.";
														 break;
													 case SslError.SSL_NOTYETVALID:
														 message = "The certificate is not yet valid.";
														 break;
												 }
												 message += " Do you want to continue anyway?";

												 builder.setTitle("SSL Certificate Error");
												 builder.setMessage(message);
												 builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
													 @Override
													 public void onClick(DialogInterface dialog, int which) {
														 handler.proceed();
													 }
												 });
												 builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
													 @Override
													 public void onClick(DialogInterface dialog, int which) {
														 handler.cancel();
													 }
												 });
												 final AlertDialog dialog = builder.create();
												 dialog.show();
											 }
										 });

				/*
				webView.setDownloadListener(new DownloadListener() {
					public void onDownloadStart(String url, String userAgent,
                                                String contentDisposition, String mimetype,
                                                long contentLength) {
						    Uri uri = Uri.parse(url);
						    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						    startActivity(intent);
					}
				});
				*/


				/*
				webView.setDownloadListener(new DownloadListener() {
					@Override
					public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

						MainActivity mainActivity = ((MainActivity) getActivity());

						DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

						Log.d("mimeType", ">"+mimeType+"<");

						request.setMimeType(mimeType);
						//------------------------COOKIE!!------------------------
						String cookies = CookieManager.getInstance().getCookie(url);
						request.addRequestHeader("cookie", cookies);
						//------------------------COOKIE!!------------------------
						request.addRequestHeader("User-Agent", userAgent);
						request.setDescription("Downloading file...");
						request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
						request.allowScanningByMediaScanner();
						request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
						request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));


						DownloadManager dm = (DownloadManager) mainActivity.getSystemService(DOWNLOAD_SERVICE);
						dm.enqueue(request);
						Toast.makeText(mainActivity.getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
					}
				});

				 */


				//handle downloading
				webView.setDownloadListener(new DownloadListener()
				{
					@Override
					public void onDownloadStart(String url, String userAgent,
												String contentDisposition, String mimeType,
												long contentLength) {

						MainActivity mainActivity = ((MainActivity) getActivity());
						DownloadManager.Request request = new DownloadManager.Request(
								Uri.parse(url));
						request.setMimeType(mimeType);
						String cookies = CookieManager.getInstance().getCookie(url);
						request.addRequestHeader("cookie", cookies);
						request.addRequestHeader("User-Agent", userAgent);
						request.setDescription("Downloading File...");
						request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
						request.allowScanningByMediaScanner();
						request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
						request.setDestinationInExternalPublicDir(
								Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
										url, contentDisposition, mimeType));
						DownloadManager dm = (DownloadManager) mainActivity.getSystemService(DOWNLOAD_SERVICE);
						dm.enqueue(request);
						Toast.makeText(mainActivity.getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
					}});


				//webView.getSettings().setJavaScriptEnabled(true);
				JavaScriptInterface JSInterface = new JavaScriptInterface( (MainActivity) getActivity() );
				webView.addJavascriptInterface(JSInterface, "JSInterface");
				init();
			}
			return rootView;
		}

		public void init() {
			//if (CostantiWeb.INDIRIZZO_PORTALE != null && !CostantiWeb.INDIRIZZO_PORTALE.isEmpty())
			if ( (CostantiWeb.INDIRIZZO_PORTALE).length()>0 ) { webView.loadUrl(CostantiWeb.INDIRIZZO_PORTALE); }
			else {
				//Avvisi.avviso(getActivity(), getResources().getString(R.string.no_stringa_connessione), "avviso");
				String lingua = Locale.getDefault().getLanguage();
				if(lingua.substring(0, 2).equals("en")){lingua = "en";}

				webView.loadUrl("file:///android_asset/html/primo_avvio-"+lingua+".html");
				//Mostro popUp per scegliere il portale
				//goPopUp();
				//new MainActivity().chooseDb();
			}
		}

		public WebView getWebView() { return webView; }

		public class JavaScriptInterface {
			Context mContext;

			/** Instantiate the interface and set the context */
			JavaScriptInterface(Context c){ mContext = c; }

			@JavascriptInterface
			public void leggiTagNFC(){ RichiestaNFC.TagRead(); }

			@JavascriptInterface
			public void scriviSuTagNFC(String tagToWrite){ RichiestaNFC.TagWrite(tagToWrite); }

			@JavascriptInterface
			public void interrompiOperazioneSuNFC(){ RichiestaNFC.inizializza(); }

			@JavascriptInterface
			public void disattivaRotazione(){ getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR); }

			@JavascriptInterface
			public void attivaRotazione(){ getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR); }

			@JavascriptInterface
			public void stampaStringa(String messaggio){ Avvisi.avviso(getActivity(), messaggio, "avviso"); }

			@JavascriptInterface
			public void detectorQR() {
				MainActivity mainActivity = ((MainActivity) getActivity());

				IntentIntegrator integrator =  new IntentIntegrator(getActivity());
				integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
				integrator.setPrompt(mainActivity.getApplicationContext().getString(R.string.msg_pronto_scansione));
				integrator.initiateScan();
			}

			@JavascriptInterface
			public void avviaGoogleMaps(String lat, String lng, String label) /*lat e lng devono essere float (usare il punto e non la virgola)*/{
				Uri gmmIntentUri = Uri.parse("geo:0,0?q="+lat+","+lng+"("+label+")");
				Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
				mapIntent.setPackage("com.google.android.apps.maps");
				startActivity(mapIntent);
			}

			@JavascriptInterface
			public String getAppVersionCode() {
				MainActivity mainActivity = ((MainActivity) getActivity());
				int versionCode = -1;

				try {
					PackageInfo pInfo = mainActivity.getPackageManager().getPackageInfo(mainActivity.getPackageName(), 0);
					versionCode = pInfo.versionCode;
				} catch (PackageManager.NameNotFoundException e) {
					Log.e(TAG, ""+e);
					e.printStackTrace();
				}
				return ""+versionCode;
			}

			@JavascriptInterface
			public String getLoginData() {
				MainActivity mainActivity = ((MainActivity) getActivity());
				JSONObject json = new JSONObject();

				try {
					SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);

					String username = sharedPrefs.getString("prefDBUsername", "");
					String password = sharedPrefs.getString("prefDBPassword", "");

					json.put("username", username);
					json.put("password", password);
				}
				catch (JSONException e) {
					Log.e(TAG, ""+e);
				}
				return json+"";
			}
		}
	}

	public static class RfidFragment extends Fragment {
		public static final String ARG_PLANET_NUMBER = "section_number";

		public static RfidFragment newInstance(int sectionNumber) {
			RfidFragment fragment = new RfidFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_PLANET_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_rfid, container, false);

			if(savedInstanceState==null) {
				TextView text = rootView.findViewById(R.id.textViewScansioneRfid);
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

				String lastDataInvioRfid = sharedPrefs.getString("lastDataInvioRfid", "");

				//SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
				//String date = SimpleDateFormat.format(lastDataInvioRfid);

				text.setText("Ultimo invio: " + lastDataInvioRfid);

				Button btn = rootView.findViewById(R.id.bntPulisciListaRfid);
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						text.setText("");
					}
				});

			}
			return rootView;
		}


		private void getRfid(TextView text, Context context) {

		}

		private void aggiornaListaRfid(TextView text, Context context) {

		}
	}

	public static class ImpostazioniFragment extends Fragment {
		public static final String ARG_PLANET_NUMBER = "section_number";

		public static ImpostazioniFragment newInstance(int sectionNumber) {
			ImpostazioniFragment fragment = new ImpostazioniFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_PLANET_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_impostazioni, container, false);

			if(savedInstanceState==null) {
				TextView text1 = rootView.findViewById(R.id.textViewConnessione);
				TextView text2 = rootView.findViewById(R.id.textViewGPS);
				TextView text3 = rootView.findViewById(R.id.textViewNFC);

				testConnessione(text1, getActivity());
				testGPS(text2, getActivity(), false);
				testNFC(text3, getActivity());

				Button bntTestConnessione = rootView.findViewById(R.id.bntTestConnessione);
				bntTestConnessione.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MainActivity x = (MainActivity) getActivity();
						TextView text = getView().findViewById(R.id.textViewConnessione);
						testConnessione(text, x);
					}
				});

				Button bntTestGPS = rootView.findViewById(R.id.bntTestGPS);
				bntTestGPS.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MainActivity x = (MainActivity) getActivity();
						TextView text = getView().findViewById(R.id.textViewGPS);
						testGPS(text, x, true);
					}
				});

				Button bntTestNFC = rootView.findViewById(R.id.bntTestNFC);
				bntTestNFC.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MainActivity x = (MainActivity) getActivity();
						TextView text = getView().findViewById(R.id.textViewNFC);
						testNFC(text, x);
					}
				});
			}
			return rootView;
		}

		private void testConnessione(TextView text, Context context) {
			MainActivity mainActivity = ((MainActivity) getActivity());
			if(MobileData.isOnline( context )) {
				text.setText(mainActivity.getApplicationContext().getString(R.string.sistema_connesso));
				text.setTextAppearance(getActivity(), R.style.labelSuccesso);
			}
			else {
				text.setText(mainActivity.getApplicationContext().getString(R.string.sistema_non_connesso));
				text.setTextAppearance(getActivity(), R.style.labelErrore);
			}
		}

		private void testGPS(TextView text, Context context, boolean permettiAttivazioneGPS) {
			MainActivity mainActivity = ((MainActivity) getActivity());
			if(Geolocalizzazione.isEnableLocation(context)) {
				text.setText(mainActivity.getApplicationContext().getString(R.string.localizzazione_attiva)+" "+Geolocalizzazione.findTypeLocationInUse(context)+"\" !");
				text.setTextAppearance(getActivity(), R.style.labelSuccesso);
			}
			else {
				text.setText(mainActivity.getApplicationContext().getString(R.string.localizzazione_non_attiva));
				text.setTextAppearance(getActivity(), R.style.labelErrore);

				if(permettiAttivazioneGPS) Geolocalizzazione.enableLoaction(context);
			}
		}

		private void testNFC(TextView text, Context context) {
			MainActivity mainActivity = ((MainActivity) getActivity());
			NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
			NfcAdapter adapter = manager.getDefaultAdapter();

			if (adapter == null ) {
				text.setText(mainActivity.getApplicationContext().getString(R.string.nfc_non_presente));
				text.setTextAppearance(getActivity(), R.style.labelErrore);
			}
			else {
				if (adapter.isEnabled()) {
					text.setText(mainActivity.getApplicationContext().getString(R.string.nfc_presente_e_abilitato));
					text.setTextAppearance(getActivity(), R.style.labelSuccesso);
				}

				if (!adapter.isEnabled()) {
					text.setText(mainActivity.getApplicationContext().getString(R.string.nfc_non_abilitato));
					text.setTextAppearance(getActivity(), R.style.labelErrore);
				}
			}
		}
	}

	public void chooseDb(){
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ActivityMain);
		String[] arrayPortali = sharedPrefs.getString("prefDBStringa", "").split(",");
		for (int i=0; i<arrayPortali.length; i++) {
			arrayPortali[i] = arrayPortali[i].replace(" ", "");
			CostantiWeb.chooseDB.add(arrayPortali[i]);
		}

		//CostantiWeb.chooseDB.add(portale);
		final CharSequence[] items = CostantiWeb.chooseDB.toArray(new CharSequence[CostantiWeb.chooseDB.size()]);
		AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain);
		builder.setTitle("Scegli la Stringa di Connessione")
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position
						// of the selected item
						Toast.makeText(ActivityMain, items[which].toString(), Toast.LENGTH_SHORT).show();
						//stringaConnessione = sharedPrefs.getString("prefDBStringa", "");
						//if (CostantiWeb.INDIRIZZO_PORTALE != null && !CostantiWeb.INDIRIZZO_PORTALE.isEmpty())
						reloadFragmentAtIndex(0);
						//if(CostantiWeb.chooseDB.size() > 1){
						//Se ha più stringhe deve scegliere la preferita
						//}
						//else{
						sharedPrefs.edit().putString("stringaConnessione", items[which].toString()).apply();
						new RisoluzioneIndirizzoPortale().execute( (Object)items[which].toString(),ActivityMain);
						//}
					}
				});
		builder.show();
		CostantiWeb.chooseDB.clear();
	}

	// Create ImageFile
	private static File createImageFile(Context context) throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + ".jpg";

		File Image = null;
		String imagesDir = null;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
			Image = new File(imagesDir, imageFileName);
		}
		else{
			// below Android Q
			imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
		}
		Image = new File(imagesDir, imageFileName);
		return Image;
	}

	public static int copy(InputStream input, OutputStream output) throws Exception, IOException {
		final int BUFFER_SIZE = 1024 * 2;
		byte[] buffer = new byte[BUFFER_SIZE];

		BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
		BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
		int count = 0, n = 0;
		try {
			while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
				out.write(buffer, 0, n);
				count += n;
			}
			out.flush();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				Log.e(TAG, ""+e);
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				Log.e(TAG, ""+e);
				e.printStackTrace();
			}
		}
		return count;
	}

}
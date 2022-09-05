package com.natisoftnavigazione;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.costanti.CostantiWeb;

public class UserSettingActivity extends PreferenceActivity  {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		String versionName;
		try {
			versionName = getBaseContext().getPackageManager().getPackageInfo(getBaseContext().getPackageName(), 0).versionName;
			Preference editTextPref = findPreference("versionNumberUser");
			editTextPref.setSummary(versionName);
		} catch (NameNotFoundException e) { e.printStackTrace(); }

/*
		Preference editTextPref = findPreference("nome_odbc");
		editTextPref.setT(CostantiWeb.NAME_ODBC);

		editTextPref = findPreference("indirizzo_portale");
		editTextPref.setSummary(CostantiWeb.INDIRIZZO_PORTALE_GENERICO);
		*/

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("indirizzo_portale", CostantiWeb.INDIRIZZO_PORTALE_GENERICO);
		editor.putString("nome_odbc", CostantiWeb.NAME_ODBC);
		editor.commit();



		int livCompress = prefs.getInt("livCompress", 0);
		final Preference barPref = findPreference("livCompress");
		barPref.setTitle(livCompress+" %");
		barPref.setOnPreferenceChangeListener(
				new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						//Log.i("onPreferenceChange", "NumberPicker Changed");
						//Toast.makeText(getBaseContext(),""+newValue, Toast.LENGTH_SHORT).show();
						barPref.setTitle(newValue+" %");
						return true;
					}
				});

		Preference updateApp = findPreference("updateApp");
		updateApp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
				@Override
				public boolean onPreferenceClick(Preference preference) {
			        Intent intent = new Intent(Intent.ACTION_VIEW ,Uri.parse("https://play.google.com/store/apps/details?id=com.natisoftnavigazione"));
			        startActivity(intent);
					return false;
				} 
	        });
	}

	@Override
	protected void onStart() { super.onStart(); }

	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("exit-PreferenceActivity", true);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		super.onBackPressed();
	}

	public PreferenceManager ottienireferenceManager()
	{
		return getPreferenceManager();
	}
}
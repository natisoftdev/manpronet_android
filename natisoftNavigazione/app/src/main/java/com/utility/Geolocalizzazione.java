package com.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.location.LocationManager; 

public class Geolocalizzazione {
	
	public static void enableLoaction(final Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		
		LocationManager lm = null;
		 boolean gps_enabled = false,network_enabled = false;
		    if(lm==null) lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		    try{ gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER); }
		    catch(Exception ex){}
		    try{ network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER); }
		    catch(Exception ex){}

			/*
		   if(!gps_enabled && !network_enabled){
		        dialog = new AlertDialog.Builder(context);
		        dialog.setMessage("La geolocalizzazione risulta disattivata.\n\nSe non si vuole visualizzare piu' questo messaggio andare nelle impostazioni dell'app e deselezionare la voce 'Localizzazione'.");
		        dialog.setPositiveButton("attiva geolocalizzazione",
	                    new DialogInterface.OnClickListener(){

		            @Override
		             public void onClick(DialogInterface dialog, int id){
	                    Intent callGPSSettingIntent = new Intent(
	                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                    context.startActivity(callGPSSettingIntent);
	                }
		        });
		        
		        dialog.setNegativeButton("Annulla",
	                    new DialogInterface.OnClickListener(){
	                public void onClick(DialogInterface dialog, int id){
	                    dialog.cancel();
	                }
	            });
		        dialog.show();

		    }
		*/
	}	
	
	
	public static boolean isEnableLocation(Context context) {
		LocationManager lm = null;
		boolean gps_enabled = false,network_enabled = false;
		if(lm==null){lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);}

		try{ gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER); }
		catch(Exception ex){}

		try{ network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER); }
		catch(Exception ex){}

		if(!gps_enabled && !network_enabled) { return false; }
		else {return true;}
	}	
	
	public static String findTypeLocationInUse(Context context) {
		LocationManager lm = null;
	 	boolean gps_enabled = false,network_enabled = false;
			if(lm==null){lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);}

		    try{ gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER); }
		    catch(Exception ex){}

		    try{ network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER); }
		    catch(Exception ex){}

		    if(gps_enabled && !network_enabled)
		    	return "gps";
		    else if(network_enabled && !gps_enabled)
		    	return "network";
		    else if(network_enabled && gps_enabled)
		    	return "gps e network"; 
		    else if(!gps_enabled && !network_enabled)
		    	return "";
		    
			return null; 	
	}		
	
}

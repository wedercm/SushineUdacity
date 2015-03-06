package br.com.udacity.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener{
	private Uri localizacao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new ForecastFragment()).commit();
		}
		LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		Log.i("MainActivity","TALEA<EA<E");
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		if(id == R.id.action_location)
		{
			Intent intent = new Intent(LOCATION_SERVICE, null);
			if(intent.resolveActivity(getPackageManager()) != null)
			{
			}else Toast.makeText(getApplicationContext(), "Não existe APP de localização.", Toast.LENGTH_LONG).show();
			showMap(localizacao);
		}
		return super.onOptionsItemSelected(item);
	}
	public void showMap(Uri geoLocation) {
	    Intent intent = new Intent(Intent.ACTION_VIEW);
	    intent.setData(geoLocation);
	    if (intent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(intent, 1);
	    }
	}


	@Override
	public void onLocationChanged(Location location) {
		localizacao = Uri.parse("geo:+"+location.getLatitude()+","+location.getLongitude());		
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub		
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
}

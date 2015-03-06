package br.com.udacity.sunshine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ForecastFragment extends Fragment {
	private static String arrayTempo[] = {};
	public static final String EXTRA_FORECAST_FRAGMENT = "extraForecastFragment";
	ArrayAdapter<String> mForecastAdapter;
	public ForecastFragment() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		List<String> weekForecast = new ArrayList<String>(Arrays.asList(arrayTempo));
		mForecastAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item_forecast, 
				R.id.list_item_forecast_textView, 
				weekForecast);
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		ListView lvTempo = (ListView) rootView.findViewById(R.id.listview_forecast);
		lvTempo.setAdapter(mForecastAdapter);
		lvTempo.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getActivity(),DetailActivity.class);
				intent.putExtra(EXTRA_FORECAST_FRAGMENT,mForecastAdapter.getItem(position).toString());
				startActivity(intent);
			}
		});
				
		return rootView;
	}
	@Override
	public void onStart() {
		super.onStart();
		updateWeather();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.forecast_fragment, menu);
	}
	public void updateWeather()
	{
		FetchWeatherTask weatherTask = new FetchWeatherTask();
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());		
		String cidade = mPreferences.getString(getString(R.string.pref_city_key), getString(R.string.pref_city_default));
		String units = mPreferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));
		String[] valuesForAPI = {cidade,units};
		weatherTask.execute(valuesForAPI);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_refresh)
		{
			
		}
		return super.onOptionsItemSelected(item);
	}

	public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
		private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

		@Override
		protected String[] doInBackground(String... valuesForAPI) {
			String cidade = valuesForAPI[0];
			String units = valuesForAPI[1];
			try {
				String[] weather = getForecast(getUrl(cidade,units),LOG_TAG,7);
				return weather;
			} catch (JSONException e) {
				e.printStackTrace();
			}				
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			if(result != null)
			{
				mForecastAdapter.clear();
				for(String a : result)
				{
					mForecastAdapter.add(a);					
				}
			}
		}							
	}
	public String getUrl(String cidade,String units)
	{
		final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
		final String QUERY_PARAM = "q";
		final String FORMAT_PARAM = "mode";
		final String UNITS_PARAM = "units";
		final String DAYS_PARAM = "cnt";
		final String LANG_PARAM = "lang";
		String mode = "json";
		String days = "7";
		String language = "pt";
		if(units.equals(getString(R.string.celsius)))
		{
			units = "metric";
		}else units = "imperial"; 
		Uri.Builder builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
		.appendQueryParameter(QUERY_PARAM, cidade)
		.appendQueryParameter(UNITS_PARAM, units)
		.appendQueryParameter(FORMAT_PARAM, mode)
		.appendQueryParameter(DAYS_PARAM, days)
		.appendQueryParameter(LANG_PARAM, language);
		
		return builtUri.build().toString();
	}
	private String[] getForecast(String path,String logTag, int day) throws JSONException
	{
		HttpURLConnection mUrlConnection = null;
		BufferedReader mBufferedReader = null;
		
		String forecastJsonStr = null;
		
		try {
			URL url  = new URL(path);			
			mUrlConnection = (HttpURLConnection) url.openConnection();
			mUrlConnection.setRequestMethod("GET");
			mUrlConnection.connect();
			
			
			InputStream mInputStream = mUrlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			if(mInputStream == null)
			{
				return null;
			}
			mBufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
			String line;
			while((line = mBufferedReader.readLine()) != null)
			{
				buffer.append(line + "\n");
			}
			if(buffer.length() == 0)
			{
				return null;
			}
			forecastJsonStr = buffer.toString();
		} catch (Exception e) {
			forecastJsonStr = null;
		}finally{
			if(mUrlConnection != null)
				mUrlConnection.disconnect();
			if(mBufferedReader != null)
			{
				try {
					mBufferedReader.close();
				} catch (Exception e) {
				}
			}
		}				
		return getWeatherDataFromJson(forecastJsonStr, day);		
	}
	private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }        
        return resultStrs;

    }
	
	
}

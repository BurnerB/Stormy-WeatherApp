package tk.barnabykamau.stormy;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tk.barnabykamau.stormy.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private Currentweather currentweather;

    public static final String TAG = MainActivity.class.getSimpleName();

    final double latitude = 37.8267;
    final double longitude = -122.4233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForcast(latitude,longitude);
    }

    private void getForcast(double latitude, double longitude) {
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this,
                R.layout.activity_main);

        TextView darkSky = findViewById(R.id.darksky_Attrib);

        darkSky.setMovementMethod(LinkMovementMethod.getInstance());

        String ApIKey = "ca8db57c66b53e36148828904a84e2a1";


        String forecastURL = "https://api.darksky.net/forecast/" + ApIKey
                + "/" + latitude + "," + longitude;


        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG,jsonData );
                        if (response.isSuccessful()) {
                            currentweather = getCurrentDetails(jsonData);

                            Currentweather displayWeather = new Currentweather(
                                    currentweather.getLocationLabel(),
                                    currentweather.getIcon(),
                                    currentweather.getTime(),
                                    currentweather.getTemperature(),
                                    currentweather.getHumidity(),
                                    currentweather.getPrecipChance(),
                                    currentweather.getSummary(),
                                    currentweather.getTimezone()
                            );

                            binding.setWeather(displayWeather);

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception caught: ", e);
                    }catch (JSONException e){
                        Log.e(TAG, "JSON Exception caught: ",e);
                    }

                }
            });
        }
    }

    private Currentweather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);

        String timeZone = forecast.getString("timeZone");
        Log.i(TAG,"from JSON: "+timeZone);

        JSONObject currently = forecast.getJSONObject("currently");

        Currentweather currentweather = new Currentweather();

        currentweather.setHumidity(currently.getDouble("humidity"));
        currentweather.setTime(currently.getLong("time"));
        currentweather.setIcon(currently.getString("icon"));
        currentweather.setLocationLabel("Alcatraz Island, CA");
        currentweather.setPrecipChance(currently.getDouble("precipProbability"));
        currentweather.setSummary(currently.getString("summary"));
        currentweather.setTemperature(currently.getDouble("temperature"));
        currentweather.setTimezone(timeZone);

        Log.d(TAG,currentweather.getFormattedTime());

        return currentweather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }else{
            Toast.makeText(this, getString(R.string.network_unavailable_message),
                    Toast.LENGTH_LONG).show();
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"error_dialog");
    }

    public void refreshOnClick(View view){
        Toast.makeText(this, "Refreshing data", Toast.LENGTH_SHORT).show();
        getForcast(latitude,longitude);

    }
}

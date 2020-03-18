package com.kratosle.magnometr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TextView sText;
    private LineChart chart;
    private ArrayList<Entry> chartValues = new ArrayList<>();
    private LineDataSet lnData;
    private ArrayList<ILineDataSet> dtSet = new ArrayList<>();
    String ip;
    long lastValue;
    private long x = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Akbar Magnetic Sensor");
        sText = findViewById(R.id.sensor);
        chart = findViewById(R.id.chart);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);
        ((Button)findViewById(R.id.host)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Host to");
                alertDialog.setMessage("Enter IP-adress");

                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);


                alertDialog.setPositiveButton("HOST",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ip =  input.getText().toString();
                               if (!ip.isEmpty()){
                                   new Async().execute();
                               }
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        lineobnovitkun(sensorEvent.values[0]);

        sText.setText(getResources().getString(R.string.magnetic_field)+" "+String.valueOf(sensorEvent.values[0]));
        Log.e(getResources().getString(R.string.app_name),String.valueOf(sensorEvent.values[0]));
        if (ip!=null && !ip.isEmpty()){
            new Async().execute();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }





    private void lineobnovitkun(float value){
        chartValues.add(new Entry(x,value));
        x = x+1;
        if(chartValues.size()>50){
            chartValues.remove(0);
        }
        lastValue = (long) value;




        lnData = new LineDataSet(chartValues,"Akbar Magnetics");
        dtSet.add(lnData);
        LineData lD = new LineData(dtSet);
        chart.setData(lD);
        chart.invalidate();

    }


    private class Async extends AsyncTask<String,  String, String>{

        @Override
        protected String doInBackground(String... strings) {
            BufferedReader reader=null;
            try

            {

                // Defined URL  where to send data
                URL url = new URL(ip);

                // Send POST data request

                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(String.valueOf(lastValue));
                wr.flush();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }


            }
            catch(Exception ex)
            {

            }
            finally
            {
                try
                {

                    reader.close();
                }

                catch(Exception ex) {}
            }

            // Show response on activity



            return null;
        }
    }

}

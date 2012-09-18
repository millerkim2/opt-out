package com.senddroid.optout;

import java.io.IOException;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


public class SenddroidOptoutActivity extends Activity implements OnClickListener {

    String deviceId;

    OptOutAsyncTask optOutTask = null;

    private class OptOutAsyncTask extends AsyncTask<String, Integer, String> {
        final static String OPTOUT_URL = "http://push.senddroid.com/optout.php";

        @Override
        protected String doInBackground(String...udids) {
            if(udids.length == 1) {
                final String udid = udids[0];
                final String url = OPTOUT_URL + "?udid=" + udid;
                Log.d("SendDROID", url);
                return doRestCall(url);
            }
            return null;
        }

        private String doRestCall(String url) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                return httpclient.execute(request, handler);
            } catch (HttpResponseException e) {
                Log.e("SendDROID", "Opt-Out Failure", e);
            } catch (ClientProtocolException e) {
                Log.e("SendDROID", "Opt-Out Failure", e);
            } catch (IOException e) {
                Log.e("SendDROID", "Opt-Out Failure", e);
            } finally {
                httpclient.getConnectionManager().shutdown();
            }

            return "ERROR";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            boolean wasSuccessful = "".equals(result);
            onOptOutResults(wasSuccessful);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        deviceId = Utils.getDeviceId(this);
        if(deviceId != null) {
            TextView deviceIdTextbox = (TextView)findViewById(R.id.deviceIdTextbox);
            deviceIdTextbox.setText("Device ID: " + deviceId);

            findViewById(R.id.button).setOnClickListener(this);
        }
        else {
            findViewById(R.id.button).setVisibility(View.GONE);
            TextView failText = (TextView)findViewById(R.id.failText);
            final SpannableString linkedVerbiage = new SpannableString(failText.getText());
            Linkify.addLinks(linkedVerbiage, Linkify.ALL);
            failText.setText(linkedVerbiage);
            failText.setMovementMethod(LinkMovementMethod.getInstance());
            failText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        new AlertDialog.Builder(this)
        .setTitle("")
        .setMessage("Are you sure you want to opt out of receiving SendDROID Ads?")
        .setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String udid = Utils.getDeviceIdMD5(SenddroidOptoutActivity.this);
                        Log.v("SendDROID", "Opting out device udid: " + udid);
                        View disabler = findViewById(R.id.disabler);
                        disabler.setVisibility(View.VISIBLE);
                        optOutTask = new OptOutAsyncTask();
                        optOutTask.execute(udid);
                    }
                })
        .setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
        .create().show();
    }

    public void onOptOutResults(boolean wasSuccessful) {
        final String msg;
        if(wasSuccessful) {
            msg = getString(R.string.goodby);
        }
        else {
            msg = getString(R.string.error);
        }
        cleanup();
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void cleanup() {
        if(optOutTask != null) {
            optOutTask.cancel(true);
            optOutTask = null;
        }
        View disabler = findViewById(R.id.disabler);
        disabler.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        cleanup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanup();
    }
}
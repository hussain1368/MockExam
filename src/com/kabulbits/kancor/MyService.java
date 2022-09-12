package com.kabulbits.kancor;

import java.io.IOException;

import net.kabulsoft.kancor.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyService extends IntentService {
	
	private String url = "http://kabulbits.com/kankor/kankor_high_score.php";
	
    public MyService() {
        super("Find High Score");
    }
    
    public static final String TAG = "MHK";
    private NotificationManager notif;
    NotificationCompat.Builder builder;

    @Override
    protected void onHandleIntent(Intent intent) {
    	new Thread(new Runnable() {
			public void run() {
				int newScore = getScore();
				Log.i("mhk", String.valueOf(newScore));
				SharedPreferences profile = getSharedPreferences("PROFILE", 0);
				int oldScore = profile.getInt("globalScore", 0);
				if(newScore > oldScore){
					String msg = getText(R.string.notif_new_score).toString() + newScore;
					sendNotification(msg);
					SharedPreferences.Editor edit = profile.edit();
					edit.putInt("globalScore", newScore);
					edit.commit();
				}
			}
		}).start();
    	MyAlarm.completeWakefulIntent(intent);
    }
    
    private void sendNotification(String msg) {
    	
        notif = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, Home.class);
        intent.putExtra("goto", 0);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(getText(R.string.notif_new_score_title))
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        builder.setContentIntent(contentIntent);
        notif.notify(1, builder.build());
    }
 
    private int getScore(){
        
    	HttpClient client = new DefaultHttpClient();
    	client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);
    	HttpGet get = new HttpGet(url);
    	HttpResponse resp;
		try {
			resp = client.execute(get);
			HttpEntity entity = resp.getEntity();
			String score = EntityUtils.toString(entity).trim();
			return Integer.parseInt(score);
		} 
		catch (ClientProtocolException e1) {
			Log.i("mhk", "client error");
		} 
		catch (IOException e1) {
			Log.i("mhk", "io error");
		} 
		catch (NumberFormatException e) {
			Log.i("mhk", "number error");
		}
		return 0;
    }
}

package com.kabulbits.kancor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import net.kabulsoft.kancor.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.ListFragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class World extends ListFragment {

	private String url = "http://kabulbits.com/kankor/kankor_scores.php";
	private GetData task;
	
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setRetainInstance(true);
		MyAdapter adapter = new MyAdapter(getActivity(), new ArrayList<Person>());
		setListAdapter(adapter);
		task = new GetData();
		task.execute(adapter);
		Log.i("MHK", "oncreate");
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		
		return inflater.inflate(R.layout.world, container, false);
	}
	
	private class MyAdapter extends ArrayAdapter<Person> {

		private ArrayList<Person> persons;
		
		public MyAdapter(Context context, ArrayList<Person> objects) {
			super(context, android.R.layout.simple_list_item_1, objects);
			Log.i("mhk", "list update");
			persons = objects;
		}
		
		@SuppressLint("ViewHolder")
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(R.layout.list_item, parent, false);

			TextView pname = (TextView) row.findViewById(R.id.p_name);
			TextView pprov = (TextView) row.findViewById(R.id.p_prov);
			TextView pscore = (TextView) row.findViewById(R.id.p_score);
			ImageView photo = (ImageView) row.findViewById(R.id.p_pic);
			
			Person person = persons.get(position);
			pname.setText(person.name);
			pprov.setText(person.province);
			pscore.setText("امتیاز: " + person.score);
			if(person.photo != null)
				photo.setImageDrawable(person.photo);
			
			return row;
		}
	}
	
	private class GetData extends AsyncTask<MyAdapter, Person, Boolean>{
		
		private String picurl = "http://kabulbits.com/kankor/uploads/";
		private MyAdapter adapt;
		@Override
		protected Boolean doInBackground(MyAdapter... params) {
			adapt = params[0];
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
			HttpGet get = new HttpGet(url);
			try {
				HttpResponse resp = client.execute(get);
				HttpEntity entity = resp.getEntity();
				String data = EntityUtils.toString(entity, HTTP.UTF_8).trim();
				
				JSONObject json = new JSONObject(data);
				JSONArray array = json.getJSONArray("data");
				for(int i=0; i<array.length(); i++){

					JSONObject row = array.getJSONObject(i);
					String name = row.getString("person_name");
					String prov = row.getString("person_province");
					String score = row.getString("person_score");
					String uuid = row.getString("person_device");
					
					publishProgress(new Person(name, prov, score, getPic(uuid)));
				}
			} 
			catch (ClientProtocolException e) {
				Log.i("mhk", "the protocol error");
				return false;
			} 
			catch (JSONException e) {
				Log.i("mhk", "the json error");
				return false;
			}
			catch (IOException e) {
				Log.i("mhk", "the io error");
				return false;
			} 
			return true;
		}
		@Override
		protected void onProgressUpdate(Person... values) {
			super.onProgressUpdate(values);
			Person person = values[0];
			adapt.add(person);
			adapt.notifyDataSetChanged();
		}
		private Drawable getPic(String uuid){
			try {
				URL url = new URL(picurl + uuid + ".jpg");
				return Drawable.createFromStream(url.openStream(), "src");
			} catch (IOException e) {
				Log.i("MHK", "pic error");
			}
			return null;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("MHK", "DESTROY");
		task.cancel(false);
	}
}

class Person{

	public String name = "";
	public String province = "";
	public String score = "";
	public Drawable photo;
	
	public Person(String name, String province, String score, Drawable photo){
		
		this.name = name;
		this.province = province;
		this.score = score;
		this.photo = photo;
	}
}

















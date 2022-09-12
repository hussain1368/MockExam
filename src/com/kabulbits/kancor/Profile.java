package com.kabulbits.kancor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.kabulsoft.kancor.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Profile extends Fragment {

	private String url = "http://kabulbits.com/kankor/kankor_reg.php";
	private String imagePath;
	private String name, province, score, uuid;
	private SharedPreferences profile;
	private ImageView photo;
	private Button send;
	private ProgressDialog prog;
	private boolean isProg = false;
	protected HttpClient client;
	protected HttpPost post;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		prog = new ProgressDialog(getActivity());
		prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		prog.setMessage(getText(R.string.msg_wait_send));
		prog.setIndeterminate(true);
		prog.setCancelable(false);
	}
	@Override
	public void onActivityCreated(@Nullable Bundle bundle) {
		super.onActivityCreated(bundle);
		if(isProg){
			prog.show();
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		
		final View root = inflater.inflate(R.layout.profile, container, false);
		photo = (CircleImage) root.findViewById(R.id.photo);
		final EditText nameField = (EditText) root.findViewById(R.id.name);
		final EditText provField = (EditText) root.findViewById(R.id.province);
		
		profile = getActivity().getSharedPreferences("PROFILE", 0);
		name = profile.getString("name", "");
		province = profile.getString("province", "");
		score = Integer.toString(profile.getInt("myScore", 0));

		nameField.setText(name);
		provField.setText(province);
		
		setDeviceID();
		imagePath = getActivity().getApplicationContext().getFilesDir() + "/" + uuid + ".jpg";

		try {
			File file = new File(imagePath);
			if(file.exists()){
				photo.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(file)));
			}
		} catch (FileNotFoundException e) {
			Log.i("mhk", "file error");
		}
		
		send = (Button)root.findViewById(R.id.save_info);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {

				String newName = nameField.getText().toString().trim();
				String newProv = provField.getText().toString().trim();
				
				if(newName.length() == 0 || newProv.length() == 0) return;
				
				name = newName;
				province = newProv;
				
				SharedPreferences.Editor editor = profile.edit();
				editor.putString("name", name);
				editor.putString("province", province);
				editor.commit();

				sendData();
			}
		});

		photo.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle(R.string.msg_photo_title);
				builder.setMessage(R.string.msg_photo);

				builder.setPositiveButton(R.string.btn_camera,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								Intent intent = new Intent(
										android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
								startActivityForResult(intent, 1);
							}
						});
				builder.setNegativeButton(R.string.btn_file,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								Intent intent = new Intent(Intent.ACTION_PICK);
								intent.setType("image/*");
								startActivityForResult(intent, 2);
							}
						});
				builder.create().show();
			}
		});
		
		return root;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if(resultCode != Activity.RESULT_OK) return;
			Bitmap small = null;
			if(requestCode == 1){
				Bitmap large = (Bitmap) data.getExtras().get("data");
				small = resize(large);
			}
			else if(requestCode == 2){
				Uri uri = data.getData();
				InputStream is = getActivity().getContentResolver().openInputStream(uri);
				Bitmap large = BitmapFactory.decodeStream(is);
				small = resize(large);
			}
			if(small != null){
				photo.setImageBitmap(small);
				copy(small);
			}
		} 
		catch (IOException e) {
			Log.i("mhk", "io exc");
		}
		catch (NullPointerException e){
			Log.i("mhk", "null exc");
		}
	}
	
	private Bitmap resize(Bitmap bm){
		
		int width = bm.getWidth();
		int height = bm.getHeight();
		
		if(width == height){
			width = height = 200;
		}
		else if(width < height){
			height = (height*200)/width;
			width = 200;
		}
		else if(height < width){
			width = (width*200)/height;
			height = 200;
		}
		return Bitmap.createScaledBitmap(bm, width, height, true);
	}
	
	private boolean copy(Bitmap bm){
		try {
			FileOutputStream dest = new FileOutputStream(imagePath);
			if(bm.compress(Bitmap.CompressFormat.JPEG, 100, dest)){
				SharedPreferences.Editor editor = profile.edit();
				editor.putBoolean("picModified", true);
				editor.commit();
			}
			dest.close();
			return true;
		} 
		catch (IOException e) {
			return false;
		}
	}
	
	private void sendData(){
		new AsyncTask<Integer, Void, Boolean>() {
			@Override
			protected void onPreExecute() {
				prog.show();
				isProg = true;
			};
			@Override
			protected Boolean doInBackground(Integer... arg0) {
				// HttpClient
				client = new DefaultHttpClient();
				post = new HttpPost(url);
				client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
				client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
				
				if(profile.getBoolean("picModified", false)){
					return postText() && postFile();
				}
				else{
					return postText();
				}
			}
			@Override
			protected void onPostExecute(Boolean success) {
				super.onPostExecute(success);
				if(prog.isShowing()){
					prog.dismiss();
				}
				isProg = false;
				if(success){
					Toast.makeText(getActivity(), R.string.msg_send_suc, Toast.LENGTH_LONG).show();
					SharedPreferences.Editor editor = profile.edit();
					editor.putBoolean("picModified", false);
					editor.commit();
				}else{
					Toast.makeText(getActivity(), R.string.msg_send_fail, Toast.LENGTH_LONG).show();
				}
			}
		}.execute();
	}
	@Override
	public void onDetach() {
		super.onDetach();
		if(prog != null){
			if(prog.isShowing()){
				prog.dismiss();
			}
		}
	}
	// Send data to server
	private boolean postText(){
		try{
			// add data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("name", name));
			nameValuePairs.add(new BasicNameValuePair("province", province));
			nameValuePairs.add(new BasicNameValuePair("score", score));
			nameValuePairs.add(new BasicNameValuePair("uuid", uuid));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			
			// execute HTTP post request
			HttpResponse response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				String responseStr = EntityUtils.toString(resEntity, HTTP.UTF_8).trim().substring(1, 3);
				Log.i("MHK", "Response: " +  responseStr);
				if(responseStr.equals("ok")){
					Log.i("mhk","text come");
					return true;
				}
			}
		} catch (ClientProtocolException e) {
			Log.i("mhk", "protocol error");
		} catch (IOException e) {
			Log.i("mhk", "io error");
		}
		return false;
	}
	
	private boolean postFile(){
		try{
			File file = new File(imagePath);
			if(!file.exists()) return false;
			
			// add file
			FileBody fileBody = new FileBody(file);
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("uploaded_file", fileBody);
			post.setEntity(reqEntity);
			
			// execute HTTP post request
			HttpResponse response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				String responseStr = EntityUtils.toString(resEntity, HTTP.UTF_8).trim().substring(1, 3);
				Log.i("MHK", "Response: " +  responseStr);
				if(responseStr.equals("ok")){
					Log.i("mhk", "file come");
					return true;
				}
			}
		} catch (NullPointerException e) {
			Log.i("mhk", "null error");
		} catch (ClientProtocolException e) {
			Log.i("mhk", "protocol error");
		} catch (IOException e) {
			Log.i("mhk", "io error");
		} 
		return false;
	}
	
	private void setDeviceID(){
		String oldid = profile.getString("uuid", "");
		if(oldid.trim().length() == 0){
			String serial = android.os.Build.SERIAL; 
			String random = UUID.randomUUID().toString().substring(0, 8);
			String newid = serial.trim().length() > 0 ? serial : random;
			SharedPreferences.Editor editor = profile.edit();
			editor.putString("uuid", newid);
			editor.commit();
			uuid = newid;
		}else{
			uuid = oldid;
		}
	}
}


































package com.kabulbits.kancor;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import net.kabulsoft.kancor.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Exam extends Activity {

	private TextView[] options;
	private int [] ids;
	private Cursor cursor;
	private TextView timeField, wrongsField, 
	correctsField, question, selected, correct;
	private Animation anim0, anim1;
	private ProgressBar prog;
	private CountDownTimer counter;
	private Timer timer;
	private ArrayList<Integer> wrongs;
	private int time = 0;
	private int cat = 0;
	private int count = 10;
	private int theWrongs = 0;
	private int theCorrects = 0;
	private boolean stop = false;
	private Ringtone ring;
	private Vibrator viber;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.exam);
		
		ids = new int [4];
		ids[0] = R.id.ans1;
		ids[1] = R.id.ans2;
		ids[2] = R.id.ans3;
		ids[3] = R.id.ans4;
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/majallab.ttf");
		options = new TextView[ids.length];
		for(int i=0; i<ids.length; i++){
			options[i] = (TextView)findViewById(ids[i]);
			options[i].setTypeface(tf);
		}
		
		question = (TextView) findViewById(R.id.question);
		timeField = (TextView) findViewById(R.id.time);
		wrongsField = (TextView) findViewById(R.id.wrongs);
		correctsField = (TextView) findViewById(R.id.corrects);
		prog = (ProgressBar) findViewById(R.id.progressBar1);
		question.setTypeface(tf);
		
		anim0 = AnimationUtils.loadAnimation(Exam.this, R.anim.animation);
		anim1 = AnimationUtils.loadAnimation(Exam.this, R.anim.for_question);
		wrongs = new ArrayList<Integer>();
		
		Uri beep = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		ring = RingtoneManager.getRingtone(this, beep);
		viber = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		cat = getIntent().getExtras().getInt("cat", 0);
		count = getIntent().getExtras().getInt("num", 10);
		
		if(bundle != null){
			if(bundle.getBoolean("origin", true)){
				cursor = setupCursor();
			}else{
				cursor = Kancor.CURSOR;
			}
			if(bundle.getInt("time", -1) != -1){
				time = bundle.getInt("time");
			}
			if(bundle.getInt("corrects", -1) != -1){
				theCorrects = bundle.getInt("corrects");
				correctsField.setText(String.valueOf(theCorrects));
			}
			if(bundle.getIntegerArrayList("wrongs") != null){
				wrongs = bundle.getIntegerArrayList("wrongs");
				theWrongs = wrongs.size();
				wrongsField.setText(String.valueOf(theWrongs));
			}
		}else{
			cursor = setupCursor();
		}
		question.setText(cursor.getString(cursor.getColumnIndex("question")));
		
		counter = new CountDownTimer(10000, 1) {
			public void onTick(long t) {
				prog.setProgress((int)(10000 - t));
			}
			public void onFinish() {
				prog.setProgress(10000);
				nextQuestions(-1);
			}
		};
		counter.start();
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						timeField.setText(String.valueOf(time++));
					}
				});
			}
		}, 0, 1000);

		OnClickListener listen = new OnClickListener() {
			public void onClick(View view) {
				counter.cancel();
				int index = ((TextView)view).getId();
				nextQuestions(index);
			}
		};
		int i = cursor.getColumnIndex("answer1");
		for(TextView op : options){
			op.setText(cursor.getString(i++));
			op.setOnClickListener(listen);
		}
	}
	
	private Cursor setupCursor(){
		Database db = new Database(this);
		cursor = (cat == 0)? db.getQuestions(String.valueOf(count)) : 
			db.getQuestions(String.valueOf(cat), String.valueOf(count));
		Kancor.CURSOR = cursor;
		return cursor;
	}
	
	private void nextQuestions(int index){
		
		if(stop) return;
		if(index != -1){
			selected = (TextView)findViewById(index);
			if(index != ids[cursor.getInt(cursor.getColumnIndex("correct"))]){
				selected.setBackgroundColor(Color.parseColor("#FF6666"));
				wrongs.add(cursor.getInt(cursor.getColumnIndex("_id")));
				wrongsField.setText(String.valueOf(++theWrongs));
				viber.vibrate(250);
			}else{
				correctsField.setText(String.valueOf(++theCorrects));
			}
		}else{
			wrongs.add(cursor.getInt(cursor.getColumnIndex("_id")));
			wrongsField.setText(String.valueOf(++theWrongs));
			ring.play();
		}
		
		correct = options[cursor.getInt(cursor.getColumnIndex("correct"))];
		correct.setBackgroundColor(Color.parseColor("#33CC99"));

		for(TextView op : options){
			op.setClickable(false);
		}
		
		new Handler().postDelayed(new Runnable() {
			public void run() {
				if(cursor.moveToNext()){
					question.setText(cursor.getString(cursor.getColumnIndex("question")));
					question.startAnimation(anim1);
					if(correct != null){
						correct.setBackgroundColor(Color.parseColor("#6699CC"));
					}
					if(selected != null){
						selected.setBackgroundColor(Color.parseColor("#6699CC"));
					}
					int i = cursor.getColumnIndex("answer1");
					for(TextView op : options){
						op.startAnimation(anim0);
						op.setText(cursor.getString(i++));
					}
					new Handler().postDelayed(new Runnable() {
						public void run() {
							for(TextView op : options){
								op.setClickable(true);
							}
							counter.start();
						}
					}, 500);
				}
				else{
					counter.cancel();
					timer.cancel();
					finish();
					Intent result = new Intent(Exam.this, Result.class);
					result.putExtra("wrongs", wrongs);
					result.putExtra("correct", theCorrects);
					result.putExtra("count", count);
					result.putExtra("time", time);
					startActivity(result);
				}
			}
		}, 300);
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		counter.cancel();
		timer.cancel();
		stop = true;
	}
	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		counter.cancel();
		timer.cancel();
		stop = true;
	}
	@Override
	protected void onPause() {
		super.onPause();
		counter.cancel();
		timer.cancel();
		stop = true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(!stop) return;
		stop = false;
		counter.start();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						timeField.setText(String.valueOf(time++));
					}
				});
			}
		}, 0, 1000);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("origin", false);
		outState.putInt("time", time);
		outState.putInt("corrects", theCorrects);
		outState.putIntegerArrayList("wrongs", wrongs);
		timer.cancel();
		counter.cancel();
	}
}























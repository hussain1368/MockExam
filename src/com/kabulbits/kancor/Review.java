package com.kabulbits.kancor;

import net.kabulsoft.kancor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Review extends Activity {

	private int[] ids;
	private TextView[] options;
	private TextView question;
	private TextView counterField;
	private int cat;
	private int count;
	private Cursor cursor;
	private TextView correct;
	private Animation anim0, anim1;
	private Database db;

	@Override
	protected void onCreate(Bundle bundle) {

		super.onCreate(bundle);
		setContentView(R.layout.study);

		db = new Database(this);
		
		ids = new int [4];
		ids[0] = R.id.ans1;
		ids[1] = R.id.ans2;
		ids[2] = R.id.ans3;
		ids[3] = R.id.ans4;
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/majallab.ttf");
		options = new TextView[ids.length];
		for(int i=0; i<ids.length; i++){
			options[i] =  (TextView)findViewById(ids[i]);
			options[i].setTypeface(tf);
		}
		question = (TextView) findViewById(R.id.question);
		question.setTypeface(tf);
		counterField = (TextView) findViewById(R.id.counter);
		counterField.setBackgroundResource(R.drawable.circle_button);
		counterField.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				clearWrong();
			}
		});
		
		cat = getIntent().getExtras().getInt("cat", 0);
		count = getIntent().getExtras().getInt("num", 10);
		if(bundle != null){
			if(bundle.getBoolean("origin", true)){
				cursor = getCursor();
			}else{
				cursor = Kancor.CURSOR;
			}
		}else{
			cursor = getCursor();
		}
		
		anim0 = AnimationUtils.loadAnimation(this, R.anim.animation);
		anim1 = AnimationUtils.loadAnimation(this, R.anim.for_question);
		
		nextQuestion();
		
		Button next = (Button)findViewById(R.id.next);
		Button prev = (Button)findViewById(R.id.prev);
		next.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if(cursor.isLast() || cursor.isAfterLast())
					return;
				correct.setBackgroundColor(Color.parseColor("#6699CC"));
				if(cursor.moveToNext()){
					nextQuestion();
				}
			}
		});
		
		prev.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if(cursor.isFirst() || cursor.isBeforeFirst())
					return;
				correct.setBackgroundColor(Color.parseColor("#6699CC"));
				if(cursor.moveToPrevious()){
					nextQuestion();
				}
			}
		});
	}
	
	private Cursor getCursor(){
		cursor = (cat == 0)? db.wrongAnswers(String.valueOf(count)) : 
			db.wrongAnswers(String.valueOf(cat), String.valueOf(count));
		Kancor.CURSOR = cursor;
		return cursor;
	}
	
	private void nextQuestion(){
		counterField.setText(String.valueOf(cursor.getString(cursor.getColumnIndex("wrongs"))));
		question.setText(cursor.getString(cursor.getColumnIndex("question")));
		question.startAnimation(anim1);
		int i = cursor.getColumnIndex("answer1");
		for(TextView op : options){
			op.setText(cursor.getString(i++));
			op.startAnimation(anim0);
		}
		correct = options[cursor.getInt(cursor.getColumnIndex("correct"))];
		correct.setBackgroundColor(Color.parseColor("#33CC99"));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("origin", false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.review_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.del_one:
			clearWrong();
			break;
		case R.id.del_list:
			clearList();
			break;
		case R.id.del_all:
			clearAll();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Clear current wrong answer
	private void clearWrong(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(Review.this);
		builder.setMessage(R.string.msg_del_cur);
		builder.setTitle(R.string.msg_del_title);
		builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int arg1) {
				new Thread(new Runnable() {
					public void run() {
						db.clearWrong(cursor.getInt(cursor.getColumnIndex("_id")));
						final int pos = cursor.getPosition();
						cursor = getCursor();
						final int count = cursor.getCount();
						final int last = count - 1;
						if(count > 0){
							correct.post(new Runnable() {
								public void run() {
									correct.setBackgroundColor(Color.parseColor("#6699CC"));
									int thepos = pos < last ? pos : last;
									cursor.moveToPosition(thepos);
									nextQuestion();
								}
							});
						}else{
							finish();
						}
					}
				}).start();
			}
		});
		builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}
	
	// Clear wrong answers for this list
	private void clearList(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(Review.this);
		builder.setMessage(R.string.msg_del_list);
		builder.setTitle(R.string.msg_del_title);
		builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface arg0, int arg1) {
				new Thread(new Runnable() {
					public void run() {
						for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
							db.clearWrong(cursor.getInt(cursor.getColumnIndex("_id")));
						}
						cursor = getCursor();
						if(cursor.getCount() > 0){
							correct.post(new Runnable(){
								public void run() {
									correct.setBackgroundColor(Color.parseColor("#6699CC"));
									nextQuestion();
								}
							});
						}else{
							finish();
						}
					}
				}).start();
			}
		});
		builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	// Clear all of the wrong answers
	private void clearAll(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(Review.this);
		builder.setMessage(R.string.msg_del_all);
		builder.setTitle(R.string.msg_del_title);
		builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface arg0, int arg1) {
				new Thread(new Runnable() {
					public void run() {
						db.clearAllWrongs();
						finish();
					}
				}).start();
			}
		});
		builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
















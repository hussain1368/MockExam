package com.kabulbits.kancor;

import net.kabulsoft.kancor.R;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Study extends Activity {

	private int[] ids;
	private TextView[] options;
	private TextView question;
	private TextView counterField;
	private int cat;
	private int count;
	private Cursor cursor;
	private TextView correct;
	private Animation anim0;
	private Animation anim1;

	@Override
	protected void onCreate(Bundle bundle) {

		super.onCreate(bundle);
		setContentView(R.layout.study);
		
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
		Database db = new Database(this);
		cursor = (cat == 0)? db.getQuestions(String.valueOf(count)) : 
			db.getQuestions(String.valueOf(cat), String.valueOf(count));
		Kancor.CURSOR = cursor;
		return cursor;
	}
	
	private void nextQuestion(){
		counterField.setText(String.valueOf(cursor.getPosition()+1));
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
}
















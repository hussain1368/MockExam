package com.kabulbits.kancor;

import java.util.ArrayList;

import net.kabulsoft.kancor.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Result extends Activity {

	private int myScore;
	private ArrayList<Integer> wrongs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		
		SharedPreferences scores = getSharedPreferences("PROFILE", 0);
		myScore = scores.getInt("myScore", 0);
		
		Animation anim0 = AnimationUtils.loadAnimation(this, R.anim.result_text);
		final Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.result_info);
		
		TextView [] texts = {
				(TextView)findViewById(R.id.text1),
				(TextView)findViewById(R.id.text2),
				(TextView)findViewById(R.id.text3),
				(TextView)findViewById(R.id.text4),
				(TextView)findViewById(R.id.text5),
				(TextView)findViewById(R.id.text6),
		};
		
		final TextView [] info = {
				(TextView)findViewById(R.id.res_num),
				(TextView)findViewById(R.id.res_correct),
				(TextView)findViewById(R.id.res_wrong),
				(TextView)findViewById(R.id.res_time),
				(TextView)findViewById(R.id.res_score),
				(TextView)findViewById(R.id.res_high),
		};
		
		for(TextView view : texts){
			view.startAnimation(anim0);
		}
		new Handler().postDelayed(new Runnable() {
			public void run() {
				for(TextView view : info){
					view.setVisibility(View.VISIBLE);
					view.startAnimation(anim1);
				}
			}
		}, 2000);
		
		wrongs = getIntent().getExtras().getIntegerArrayList("wrongs");
		if(wrongs != null){
			int correct = getIntent().getExtras().getInt("correct", 0);
			int time = getIntent().getExtras().getInt("time", 0);
			int score = correct*1000/time;
			myScore = (myScore > score)? myScore : score;
			info[0].setText(String.valueOf(getIntent().getExtras().getInt("count", 0)));
			info[1].setText(String.valueOf(correct));
			info[2].setText(String.valueOf(wrongs.size()));
			info[3].setText(String.valueOf(time));
			info[4].setText(String.valueOf(score));
			info[5].setText(String.valueOf(myScore));
		}
		((LinearLayout)findViewById(R.id.screen)).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Result.this.finish();
			}
		});
	}
	
	protected void onStop() {
		super.onStop();
		save();
	}
	
	private void save(){
		SharedPreferences scores = getSharedPreferences("PROFILE", 0);
		SharedPreferences.Editor editor = scores.edit();
		editor.putInt("myScore", myScore);
		editor.commit();
		new Thread(new Runnable() {
			public void run() {
				Database db = new Database(Result.this);
				for(int id : wrongs){
					db.incrementWrong(id);
				}
				
			}
		}).start();
	}
}







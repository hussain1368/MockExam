package com.kabulbits.kancor;

import net.kabulsoft.kancor.R;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Start extends Fragment {

	private RadioGroup nums;
	private int cat = -1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View root = inflater.inflate(R.layout.start, container, false);
		
		nums = (RadioGroup)root.findViewById(R.id.num);
		
		final Button category = (Button) root.findViewById(R.id.category);
		Button exam = (Button) root.findViewById(R.id.exam);
		Button study = (Button) root.findViewById(R.id.study);
		Button review = (Button) root.findViewById(R.id.review);
		
		category.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.cat_title);
				builder.setIcon(android.R.drawable.ic_menu_info_details);
				builder.setItems(R.array.subjects, new DialogInterface.OnClickListener() {
					@SuppressLint("Recycle")
					public void onClick(DialogInterface dialog, int which) {
						category.setText(getResources().obtainTypedArray(R.array.subjects).getString(which));
						cat = which;
					}
				});
				builder.create().show();
			}
		});
		
		exam.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				go(Exam.class);
			}
		});
		
		study.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				go(Study.class);
			}
		});
		
		review.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Database db = new Database(getActivity());
				boolean exists = cat == 0 ? db.countWrongs() : db.countWrongs(String.valueOf(cat));
				if(!exists){
					Toast.makeText(getActivity(), R.string.msg_no_wrong, Toast.LENGTH_SHORT).show();
					return;
				}
				go(Review.class);
			}
		});
		
		return root;
	}
	
	private void go(Class<?> cls){
		
		int num = nums.getCheckedRadioButtonId();
		
		int n = -1;
		switch(num){
		case R.id.n50: n = 50; break;
		case R.id.n100: n = 100; break;
		case R.id.n200: n = 200; break;
		}
		if(cat == -1 || n == -1) return;
		
		Intent intent = new Intent(getActivity(), cls);
		intent.putExtra("cat", cat);
		intent.putExtra("num", n);
		startActivity(intent);
	}
}

package com.kabulbits.kancor;

import net.kabulsoft.kancor.R;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Home extends FragmentActivity {

	ViewPager pager;
	
	@Override
	protected void onCreate(Bundle arg0) {
		
		super.onCreate(arg0);
		setContentView(R.layout.screen);
		
		//Start Service
		new MyAlarm().setAlarm(this);
		
		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
			
			World world = new World();
			Start start = new Start();
			Profile profile = new Profile();
			
			public Fragment getItem(int pos) {
				switch(pos){
				case 0:
					return world;
				case 1:
					return start;
				case 2:
					return profile;
				}
				return new Start();
			}

			public int getCount() {
				return 3;
			}
		});
		
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
			}
		});
		
		ActionBar.TabListener tabLis = new ActionBar.TabListener() {
			
			public void onTabSelected(Tab tab, FragmentTransaction arg1) {
				pager.setCurrentItem(tab.getPosition());
			}
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}
		};
		
		bar.addTab(bar.newTab().setText(R.string.tab_world).setTabListener(tabLis));
		bar.addTab(bar.newTab().setText(R.string.tab_start).setTabListener(tabLis));
		bar.addTab(bar.newTab().setText(R.string.tab_profile).setTabListener(tabLis));

		if(getIntent().hasExtra("goto")){
			if(getIntent().getExtras().getInt("goto", -1) == 0){
				pager.setCurrentItem(0);
				return;
			}
		}
		pager.setCurrentItem(1);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.about:
			startActivity(new Intent(this, About.class));
			break;
		case R.id.help:
			startActivity(new Intent(this, Help.class));
			break;
		case R.id.kspage:
			Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.kabulbits.com"));
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}

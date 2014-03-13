package com.talis.puc2014;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.orangepixel.twitter.OAuthVerify;
import com.orangepixel.twitter.TwitterUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint("NewApi")
public class HomeActivity extends Activity {
	private TextView fullDate;
	private TextView dayNumber;
	private TextView totalNumber;
	private TextView challengeTick;
	private ProgressBar progressSpinner;
	
	String tweetMessage;
	final Handler twitHandler = new Handler();
	public static SharedPreferences prefs;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (android.os.Build.VERSION.SDK_INT > 8) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_home);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		setup();
	}
	
	private void setup() {
		fullDate = (TextView)findViewById(R.id.fullDateLabel);
		dayNumber = (TextView)findViewById(R.id.dayNumberLabel);	
		totalNumber = (TextView)findViewById(R.id.totalNumberLabel);
		challengeTick = (TextView)findViewById(R.id.dayChallengeTick);
		progressSpinner = (ProgressBar)findViewById(R.id.progressSpinner);
		
		// use font-awesome "tick"
		Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
		challengeTick.setTypeface(myTypeface);
		challengeTick.setText("\uf00c");

		final Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		
		dayNumber.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// animation of alpha doesn't work unless you change the visibility state it seems...
		    	challengeTick.setVisibility(View.VISIBLE);
		    	challengeTick.startAnimation(myFadeInAnimation);

		    	// store the done date in the preferences
		    	setDoneDate();
		    	
		    	// reset the UI values
		    	setUIValues();
		    	
		    	Log.i("tweet", "#PUC2014 I just completed my daily challenge of " + dayNumber.getText() + " push-ups! Year total: " + totalNumber.getText());

		    	// tweet a message
				shareTwitter("#PUC2014 I just completed my daily challenge of " + dayNumber.getText() + " push-ups! Year total: " + totalNumber.getText());		    	
		    }
		});
	}
	
	private void setDoneDate() {
		PUCPreferenceManager preferences = new PUCPreferenceManager(this);
		preferences.storeDoneDate();
	}
	
	private void setUIValues() {
		Calendar c = Calendar.getInstance();

		// set full date
		SimpleDateFormat df = new SimpleDateFormat("EEEE dd MMMM yyyy");
		String formattedDate = df.format(c.getTime());		
		
		fullDate.setText(formattedDate);

		SimpleDateFormat df1 = new SimpleDateFormat("D");
		String formattedDay = df1.format(c.getTime());		
		
		dayNumber.setText(formattedDay);		

		// check if a new day has happened
		boolean sameDay = checkSameDay();

		// work out how many done up to todays date: n*(n+1)/2
		int start = (Integer.parseInt(formattedDay) - (sameDay ? 0 : 1));
		int total = start*(start + 1)/2; 
		
		totalNumber.setText(String.valueOf(total));
				
		Log.i("info", "sameDay:" + sameDay);
		
		if (!sameDay) {
			// it's a new day so hide the check!
			challengeTick.setVisibility(View.INVISIBLE);

			Log.i("info", "IT'S A NEW DAY");
		} else {
			// it's a new day so hide the check!
			challengeTick.setVisibility(View.VISIBLE);

			Log.i("info", "IT'S THE SAME DAY");			
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		setUIValues();
		
		Log.i("info", ">>>>SET DATES");
	}
	
	private void showProgress() {
		progressSpinner.setVisibility(View.VISIBLE);
	}
	
	private void hideProgress() {
		progressSpinner.setVisibility(View.GONE);
	}
	
	private boolean checkSameDay() {
		boolean sameDay = false;
		PUCPreferenceManager preferences = new PUCPreferenceManager(this);
		
		Date lastDone = preferences.getDoneDate();
		
		if (lastDone != null) {
			Calendar calendar = Calendar.getInstance();
			Calendar doneCalendar = Calendar.getInstance();

			// set up the doneCalendar
			doneCalendar.setTime(lastDone);
			
			Log.i("info", "current: " + calendar.get(Calendar.YEAR) + " done: " + doneCalendar.get(Calendar.YEAR) + " current: " + calendar.get(Calendar.DAY_OF_YEAR) + " done: " + doneCalendar.get(Calendar.DAY_OF_YEAR));
			
			sameDay = (calendar.get(Calendar.YEAR) == doneCalendar.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == doneCalendar.get(Calendar.DAY_OF_YEAR));
		}
		
		return sameDay;
	}
	
	public final void shareTwitter(String yourTweet) {
		tweetMessage = yourTweet;

		// show the spinner
		showProgress();
		
		if (TwitterUtils.isAuthenticated( prefs )) {
			// already authenticated before
			Log.i("info", "Already authenticated with Twitter");
			twitHandler.post(sendTweet);
		} else {
			// needs authenticating, so take care of it
			Log.i("info", "Authentication with Twitter required");
			Intent i = new Intent(this, OAuthVerify.class);
			i.putExtra("tweet_msg", tweetMessage);
			startActivity(i);
		}
		
		// hide the spinner
		hideProgress();
	}

	final Runnable sendTweet = new Runnable() {
		public void run() {
			try {
				TwitterUtils.sendTweet(prefs, tweetMessage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};	
}

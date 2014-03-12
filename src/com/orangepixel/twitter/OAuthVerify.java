package com.orangepixel.twitter;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class OAuthVerify extends Activity {

    private Twitter mTwitter;

    private OAuthConsumer consumer; 
    private OAuthProvider provider;
	private SharedPreferences prefs;
    
	private String tweetMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
    	try {
    		consumer = new CommonsHttpOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
    	    provider = new CommonsHttpOAuthProvider(Constants.REQUEST_URL,Constants.ACCESS_URL,Constants.AUTHORIZE_URL);
    	} catch (Exception e) {
    		Log.e("opdebug", "Error creating consumer / provider",e);
		}
        
        
        // Add here initialize mTwitter object with Twitter4J
        mTwitter = new TwitterFactory().getInstance();
        mTwitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
        
        tweetMessage=getIntent().getExtras().getString("tweet_msg");

        
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(200,240); //LayoutParams.FILL_PARENT, 416);
        WebView mWebView = new WebView(this);
        mWebView.setLayoutParams(p);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.setWebViewClient(new WebViewClient(){
        	@Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
        		Log.i("web", "URL:" + url);
                if (url.startsWith(Constants.OAUTH_CALLBACK_URL)) {
                    Uri uri = Uri.parse(url);
                    completeVerify(uri);
                    return true;
                }
                return false;
            }
        });
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.clearCache(true);
        mWebView.clearFormData();
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        final Activity activity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView w, int p) {
                activity.setProgress(p * 100);
            }
        });

        
        try {
        	String url = provider.retrieveRequestToken(consumer, Constants.OAUTH_CALLBACK_URL);
        	Log.i("web", "start URL:" + url);
        	mWebView.loadUrl(url);	
		} catch (Exception e) {
			Log.e("opdebug", "Error during OAUth retrieve request token", e);
		}
        	

        setContentView(mWebView);
    }

    
    private void completeVerify(Uri uri) {
        if (uri != null) {
            String oauth_verifier = uri.getQueryParameter("oauth_verifier");
            try {
            	provider.retrieveAccessToken(consumer, oauth_verifier);
				final Editor edit = prefs.edit();
				edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
				edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
				edit.commit();
				
				String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
				String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
				
				consumer.setTokenWithSecret(token, secret);

	        	try {
	        		TwitterUtils.sendTweet(prefs, tweetMessage);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

					
            } catch (Exception e) {
                Log.d("opdebug", "Cannot get AccessToken: " + e.getMessage());
            }
            setResult(Activity.RESULT_OK); //, mIntent);
            finish();
        }
    }
}
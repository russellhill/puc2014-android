package com.orangepixel.twitter;

public class Constants {

	public static final String CONSUMER_KEY = "i4uFXuOy1XZfKeWftC4NQ";
	public static final String CONSUMER_SECRET= "jkBMMNgeACQ5LlAQ88axL8bb1AsEilsFlWj41gso0";
	
	public static final String REQUEST_URL = "https://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "https://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
	
	public static final String	OAUTH_CALLBACK_SCHEME	= "puc-twitter";
	public static final String	OAUTH_CALLBACK_HOST		= "callback";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

}
package com.raibaz.lupus.facebook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;
import com.raibaz.lupus.game.Invite;
import com.raibaz.lupus.game.Player;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookGraphException;
import com.restfb.json.JsonException;
import com.restfb.json.JsonObject;
import com.restfb.types.User;

public class FacebookAPI {

	private static final Logger log = Logger.getLogger(FacebookAPI.class.getName());
	
	private String accessToken;
	
	public FacebookAPI(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public static String getAccessTokenFromSignedRequest(String signedRequest) {
		try {
			return getDataFromSignedRequest(signedRequest).getString("oauth_token");
		} catch (Exception e) {
			return null;
		}
	
	}
	
	public static String getUserIdFromSignedRequest(String signedRequest) {
		try {
			return getDataFromSignedRequest(signedRequest).getString("user_id");
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<Invite> getGamesFromRequests(String requestIds) {
		DefaultFacebookClient client = new DefaultFacebookClient(accessToken);
		ArrayList<Invite> ret = new ArrayList<Invite>();		
		String[] split = requestIds.split(",");
		
		for(String s : split) {		
			JsonObject requestJson = client.fetchObject(s, JsonObject.class);		
			try {
				String gameId = requestJson.getString("data");
				String inviterId = requestJson.getJsonObject("from").getString("id");
				if(gameId != null && inviterId != null) {
					Invite i = new Invite();
					i.setGameId(gameId);
					i.setInviterId(inviterId);
					ret.add(i);
				}
								
				client.deleteObject(s);
			} catch (JsonException jsone) {
				//Do nothing
			} catch (FacebookGraphException fge) {
				//Do nothing, probably requested a request_id already deleted
			}			
		}		
		return ret;
	}
			
	private static JSONObject getDataFromSignedRequest(String signedRequest) {
		String[] split = signedRequest.split("\\.");
		String decodedData = "";
		
		try {
			decodedData = new String(Base64.decodeWebSafe(split[1]));			
		} catch (Base64DecoderException uee) {}

		try {
			return new JSONObject(decodedData);			
		} catch (JSONException jsone) {
			return null;
		}
	}
	
	public Player getPlayerProfile() {
		DefaultFacebookClient client = new DefaultFacebookClient(accessToken);
		User fbUser = client.fetchObject("/me", User.class, Parameter.with("fields", "name,picture"));
		Player ret = new Player();
		ret.setFbId(fbUser.getId());
		ret.setName(fbUser.getName());
		ret.setPictureUrl("http://graph.facebook.com/" + fbUser.getId() + "/picture");
		ret.setFbToken(accessToken);
		return ret;
	}
	
	public static String getAccessToken(String code) {
		String callbackUrl = "http://localhost:888/fb_oauth";
		
		try {
			URL url = new URL("https://graph.facebook.com/oauth/access_token?client_id=183863271686299&redirect_uri=" + callbackUrl + "&client_secret=49460fbc6bab1e9481fc08a5696a8897&code=" + code);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String res = "";
			while ((line = br.readLine()) != null) {
				res += line;
			}
			String accessToken = res.substring(res.indexOf("=") + 1);
			return accessToken;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

package com.raibaz.lupus.test;

import junit.framework.Assert;

import org.junit.Test;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class JSONTest extends BaseTest {
	
	@Test
	public void testSerializeGame() {
		try {
			JSONObject json = new JSONObject(g.toJSONString());
		} catch (JSONException jsone) {
			Assert.fail(jsone.getMessage());
		}
	}

}

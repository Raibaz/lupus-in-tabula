package com.raibaz.lupus.test;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;


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

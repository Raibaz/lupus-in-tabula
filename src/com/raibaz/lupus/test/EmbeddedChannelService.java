package com.raibaz.lupus.test;

import org.junit.rules.ExternalResource;

import com.google.appengine.tools.development.testing.LocalChannelServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class EmbeddedChannelService extends ExternalResource {
	
	private static LocalServiceTestHelper helper;
	
	 @Override
	  protected void before() throws Throwable {		 
	    helper = new LocalServiceTestHelper(new LocalChannelServiceTestConfig());
	    helper.setUp();
	  }

	  @Override
	  protected void after() {
	    helper.tearDown();
	  }

}

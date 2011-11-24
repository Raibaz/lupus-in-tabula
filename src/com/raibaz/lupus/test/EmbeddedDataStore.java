package com.raibaz.lupus.test;

import org.junit.rules.ExternalResource;

import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;


public class EmbeddedDataStore extends ExternalResource {
  private static LocalServiceTestHelper helper;
    
  @Override
  protected void before() throws Throwable {
    helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
      new LocalBlobstoreServiceTestConfig(), new LocalTaskQueueTestConfig(),
      new LocalMemcacheServiceTestConfig());
    helper.setUp();
  }

  @Override
  protected void after() {
    helper.tearDown();
  }
}

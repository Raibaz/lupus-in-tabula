package com.raibaz.lupus.test;

import junit.framework.Assert;

import org.junit.Test;

import com.raibaz.lupus.game.GameConfiguration;

public class GameConfigurationTest {
	
	@Test
	public void testConfigurationFactory() {
		GameConfiguration conf = GameConfiguration.getDefaultConfiguration(8);
		Assert.assertEquals(2, conf.getHowManyWolves());
		Assert.assertTrue(conf.hasSeer());
		Assert.assertTrue(conf.hasMedium());
		Assert.assertFalse(conf.hasOwl());
		Assert.assertTrue(conf.hasIndemoniated());
		Assert.assertFalse(conf.hasBodyguard());
		
		conf = GameConfiguration.getDefaultConfiguration(10);
		Assert.assertEquals(3, conf.getHowManyWolves());
		Assert.assertTrue(conf.hasSeer());
		Assert.assertTrue(conf.hasMedium());
		Assert.assertTrue(conf.hasOwl());
		Assert.assertTrue(conf.hasIndemoniated());
		Assert.assertFalse(conf.hasBodyguard());
		
		conf = GameConfiguration.getDefaultConfiguration(14);
		Assert.assertEquals(3, conf.getHowManyWolves());
		Assert.assertTrue(conf.hasSeer());
		Assert.assertTrue(conf.hasMedium());
		Assert.assertTrue(conf.hasOwl());
		Assert.assertTrue(conf.hasIndemoniated());
		Assert.assertTrue(conf.hasBodyguard());
	}

}

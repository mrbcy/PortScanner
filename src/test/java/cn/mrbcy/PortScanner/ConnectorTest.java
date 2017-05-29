package cn.mrbcy.PortScanner;

import org.junit.Assert;
import org.junit.Test;

import cn.mrbcy.PortScanner.Connector;

public class ConnectorTest {
	@Test
	public void testNotOpen(){
		Connector sc = new Connector();
		boolean result = sc.connect("127.0.0.1",80,3000);
		Assert.assertFalse(result);
		
	}
	@Test
	public void testOpen(){
		Connector sc = new Connector();
		boolean result = sc.connect("www.baidu.com",80, 30000);
		Assert.assertTrue(result);
	}
	
	@Test
	public void testTimeout(){
		Connector sc = new Connector();
		boolean result = sc.connect("www.baidu.com",80, 1);
		Assert.assertFalse(result);
	}
	
}

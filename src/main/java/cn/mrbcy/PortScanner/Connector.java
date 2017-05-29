package cn.mrbcy.PortScanner;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;


public class Connector {
	private Logger logger = Logger.getLogger(Connector.class);
	
	public boolean connect(String host, int port, int timeoutMillSeconds) {
		Socket s;
		try {
			long startTime = System.currentTimeMillis();
//			s = new Socket(InetAddress.getByName(host),port);
			s = new Socket();
			s.connect(new InetSocketAddress(host, port), timeoutMillSeconds);
			s.close();
			long endTime = System.currentTimeMillis();
			if(endTime - startTime <= timeoutMillSeconds)
			{
				String log = String.format("OPEN %s:%d is open, scan costs %d ms", host,port,endTime - startTime);
				logger.info(log);
				return true;
			}else{
				String log = String.format("TIMEOUT %s:%d is open, scan costs %d ms", host,port,endTime - startTime);
				logger.warn(log);
				return false;
			}
		} catch (Exception e) {
			
		}
		String log = String.format("CLOSE %s:%d is close or unreachable", host,port,0);
		logger.warn(log);
		return false;
	}

}

package cn.mrbcy.PortScanner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;


public class ScanRunner implements Runnable {
	private List<String> cachedOpenList;
	private List<String> cachedCloseList;
	private ReadWriteLock rwLock;
	private int timeoutMillSeconds;
	private String ip;
	private int port;
	private Set<String> allOpenSets;

	public ScanRunner(String ip, int port, List<String> cachedOpenList,
			List<String> cachedCloseList, Set<String> allOpenSets, ReadWriteLock rwLock, int timeoutMillSeconds) {
		this.ip = ip;
		this.port = port;
		this.cachedOpenList = cachedOpenList;
		this.cachedCloseList = cachedCloseList;
		this.allOpenSets = allOpenSets;
		this.rwLock = rwLock;
		this.timeoutMillSeconds = timeoutMillSeconds;
	}

	public void run() {
		Connector connector = new Connector();
		boolean isOpen = connector.connect(ip, port, timeoutMillSeconds);
		String result = String.format("%s:%d", ip,port);
		
		rwLock.writeLock().lock();
		if(isOpen){
			cachedOpenList.add(result);
			allOpenSets.add(result);
		}else {
			cachedCloseList.add(result);
		}
		rwLock.writeLock().unlock();
	}

}

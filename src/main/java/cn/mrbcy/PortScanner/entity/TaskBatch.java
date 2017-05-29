package cn.mrbcy.PortScanner.entity;

public class TaskBatch {
	private long startIP;
	private long endIP;
	private int port;
	
	public TaskBatch(long startIP, long endIP, int port){
		this.startIP = startIP;
		this.endIP = endIP;
		this.port = port;
	}

	public long getStartIP() {
		return startIP;
	}

	public void setStartIP(long startIP) {
		this.startIP = startIP;
	}

	public long getEndIP() {
		return endIP;
	}

	public void setEndIP(long endIP) {
		this.endIP = endIP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
}

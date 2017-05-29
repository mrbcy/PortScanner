package cn.mrbcy.PortScanner.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.mrbcy.PortScanner.entity.Task;
import cn.mrbcy.PortScanner.entity.TaskBatch;

public class TaskIterator {
	private long restTaskCount = 0;
	private long curIP = 0;
	private long endIP = -1;
	private int port = 0;
	private List<TaskBatch> taskPool = new ArrayList<TaskBatch>();
	private ReadWriteLock rwLock;
	
	public TaskIterator(){
		rwLock = new ReentrantReadWriteLock();
	}
	
	/**
	 * 添加一批需要扫描的IP地址
	 * @param startIP 起始IP
	 * @param endIP 结束IP
	 * @param port 端口号
	 */
	public void addTaskBatch(String startIP, String endIP, int port) {
		long sIP = addrToLong(startIP);
		long eIP = addrToLong(endIP);
		
		if(eIP < sIP){
			throw new IllegalArgumentException("startIP is less than or equals to endIP");
		}
		
		if(port > 65535 || port < 1){
			throw new IllegalArgumentException("port should between 1 and 65535");
		}
		rwLock.writeLock().lock();
		restTaskCount += (eIP - sIP + 1);
		taskPool.add(new TaskBatch(sIP, eIP, port));
		rwLock.writeLock().unlock();
	}


	/**
	 * 获得剩余的扫描任务数
	 * @return
	 */
	public long getRestTaskCount() {
		rwLock.readLock().lock();
		long restCount = restTaskCount;
		rwLock.readLock().unlock();
		return restCount;
	}
	
	/**
	 * 得到下一个任务
	 * @return 如果没有下一个扫描任务，返回null
	 */
	public Task getNextTask() {
		if(!hasNextTask()){
			return null;
		}
		rwLock.writeLock().lock();
		if(curIP > endIP){
			// 我们已经把解包出来的Task用完了，需要再解包一个
			TaskBatch batch = taskPool.remove(0);
			curIP = batch.getStartIP();
			endIP = batch.getEndIP();
			port = batch.getPort();
		}
		String ipAddr = longToAddr(curIP);
		Task task = new Task();
		task.setIp(ipAddr);
		task.setPort(port);
		restTaskCount--;
		curIP++;
		rwLock.writeLock().unlock();
		return task;
	}

	/**
	 * 将long类型的IP地址再转回String类型
	 * @param ip
	 * @return
	 */
	public static String longToAddr(long ip) {
		String ipStr = String.format("%d.%d.%d.%d", (0xFF & (ip >>> 24)),(0xFF & (ip >>> 16)),
				(0xFF & (ip >>> 8)),(0xFF & ip)) ;
		return ipStr;
	}


	/**
	 * 将字符串类型的IP地址转换成long
	 * @param ipAddr
	 * @return
	 */
	public static long addrToLong(String ipAddr) {
		Pattern pattern = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}");
		Matcher matcher = pattern.matcher(ipAddr);
		
		if(!matcher.matches()){
			throw new IllegalArgumentException("[" + ipAddr + "] is not a valid ip address"); 
		}

	    final String[] ipNums = ipAddr.split("\\.");  
	    return (Long.parseLong(ipNums[0]) << 24)  
	            + (Long.parseLong(ipNums[1]) << 16)  
	            + (Long.parseLong(ipNums[2]) << 8)  
	            + (Long.parseLong(ipNums[3]));
	}

	/**
	 * 是否还有任务
	 * @return
	 */
	public boolean hasNextTask() {
		rwLock.readLock().lock();
		boolean hasNext = restTaskCount > 0;
		rwLock.readLock().unlock();
		return hasNext;
	}

	
}

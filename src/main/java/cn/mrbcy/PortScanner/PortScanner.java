package cn.mrbcy.PortScanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import cn.mrbcy.PortScanner.entity.Task;
import cn.mrbcy.PortScanner.listener.ScanProgressListener;
import cn.mrbcy.PortScanner.util.TaskIterator;

public class PortScanner implements Runnable{
	private ThreadPoolExecutor scanThreadPool; 
	
	private TaskIterator taskIterator = new TaskIterator();
	private List<String> cachedOpenList = new ArrayList<String>();
	private List<String> cachedCloseList = new ArrayList<String>();
	private Set<String> allOpenSets = new HashSet<String>();
	private ScanProgressListener progressListener;
	private boolean isWorking = false;
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private int timeoutMillSeconds;
	Timer notificationTimer = new Timer();


	public void addTaskBatch(String startIP, String endIP, int port){
		taskIterator.addTaskBatch(startIP, endIP, port);
	}

	public void startScan(ScanProgressListener progressListener, int scanThreadNum, int timeoutMillSeconds) {
		if(isWorking){
			return;
		}
		isWorking = true;
		this.progressListener = progressListener;
		this.timeoutMillSeconds = timeoutMillSeconds > 1 ? timeoutMillSeconds : 1;
		int threadNum = scanThreadNum < 5?5:scanThreadNum;
		
		// 创建线程池
		scanThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum);
		
		// 创建定时通知任务

		notificationTimer.schedule(new NotifyTimeTask(this), 5000, 4500);
		new Thread(this).start();
	}
	
	public void stopScan() {
		if(scanThreadPool.isShutdown()){
			return;
		}
		System.out.println("扫描即将结束，等待所有已提交任务完成...");
		notificationTimer.cancel();
		scanThreadPool.shutdown();
		try {
			scanThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}
		if(progressListener != null){
			// 再调一次进度通知
			new NotifyTimeTask(this).run();
			progressListener.onScanComplete(allOpenSets);
		}
	}
	
	public void run() {
		while(true){
			while(taskIterator.hasNextTask()){
				try {	
					if(scanThreadPool.getQueue().size() > 1000){
						Thread.sleep(1000);
						continue;
					}
				} catch (InterruptedException e) {
				}
				Task task = taskIterator.getNextTask();
				scanThreadPool.execute(new ScanRunner(task.getIp(),task.getPort(),cachedOpenList,
						cachedCloseList,allOpenSets,rwLock,timeoutMillSeconds));
				
			}
			if(scanThreadPool.isTerminating()){
				System.out.println("终止新任务添加，等待所有任务完成后结束扫描...");
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
	
	class NotifyTimeTask extends TimerTask{
		private PortScanner scanner;
		
		public NotifyTimeTask(PortScanner scanner){
			this.scanner = scanner;
			
		}
		@Override
		public void run() {
			Set<String> opSet = new HashSet<String>();
			Set<String> closeSet = new HashSet<String>();
			
			scanner.rwLock.writeLock().lock();
			opSet.addAll(cachedOpenList);
			closeSet.addAll(cachedCloseList);
			long restNum = scanThreadPool.getQueue().size() + taskIterator.getRestTaskCount();
			cachedCloseList.clear();
			cachedOpenList.clear();
			scanner.rwLock.writeLock().unlock();
			progressListener.onScanProgress(opSet, closeSet, restNum);
			
			if(restNum == 0 && !scanner.scanThreadPool.isTerminated()){
				if(!progressListener.onTaskQueueEmpty(scanner)){
					scanner.stopScan();
				}
			}
		}
		
	}

}

	

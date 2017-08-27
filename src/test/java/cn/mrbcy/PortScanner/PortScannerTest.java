package cn.mrbcy.PortScanner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import cn.mrbcy.PortScanner.listener.ScanProgressListener;


public class PortScannerTest {
	final CountDownLatch countDownLatch = new CountDownLatch(1);

	public static void main(String[] args){
	    new PortScannerTest().simpleUsageTest();
	}
	@Test
	public void simpleUsageTest(){
		
		ProgressListener listener = new ProgressListener();
		PortScanner scanner = new PortScanner();
		scanner.addTaskBatch("192.168.1.1", "192.168.1.254", 80);
		scanner.addTaskBatch("192.168.2.1", "192.168.2.254", 80);
		scanner.addTaskBatch("220.181.111.188", "220.181.111.188", 80);
		scanner.startScan(listener,200,3000);
		
		System.out.println("开始扫描，等待扫描完成...");
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
		}
		Assert.assertEquals(254*3, listener.closeSet.size());
		Assert.assertEquals(1, listener.opSet.size());
		Assert.assertEquals(1, listener.allOpenRecords.size());
		
	}
	
	class ProgressListener implements ScanProgressListener{
		public Set<String> opSet = new HashSet<String>();
		public Set<String> closeSet = new HashSet<String>();
		public Set<String> allOpenRecords;
		private boolean isFirstEmpty = true;
		
		public void onScanProgress(Set<String> openRecords,
				Set<String> closeRecords, long restNum) {
			opSet.addAll(openRecords);
			closeSet.addAll(closeRecords);
			String log = String.format("开启 %d个，关闭%d个，剩余 %d 个",openRecords.size(),closeRecords.size(),restNum);
			System.out.println(log);
		}

		public void onScanComplete(Set<String> allOpenRecords) {
			System.out.println("扫描完成，共有 " + allOpenRecords.size() + " 个开启的端口");
			Iterator<String> iterator = allOpenRecords.iterator();
			while(iterator.hasNext()){
				System.out.println(iterator.next() + " 开启");
			}
			this.allOpenRecords = allOpenRecords;
			countDownLatch.countDown();
		}

		public boolean onTaskQueueEmpty(PortScanner scanner) {
			if(isFirstEmpty){
				isFirstEmpty = false;
				System.out.println("扫描队列已空，添加一批任务");
				scanner.addTaskBatch("192.168.3.1", "192.168.3.254", 80);
				return true;
			}
			System.out.println("扫描队列已空，结束扫描...");
			return false;
		}
		
	}
}

package cn.mrbcy.PortScanner.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cn.mrbcy.PortScanner.entity.Task;
import cn.mrbcy.PortScanner.entity.TaskBatch;

public class TaskIteratorTest {
	private TaskIterator iterator;
	
	@Before
	public void init(){
		iterator = new TaskIterator();
	}
	
	@Test
	public void testAddTaskBatch(){
		iterator.addTaskBatch("192.168.1.1","192.168.1.254",80);
		iterator.addTaskBatch("192.168.2.1","192.168.2.254",82);
		long restCount = iterator.getRestTaskCount();
		Assert.assertEquals(254*2, restCount);
	}
	
	@Test
	public void testSimpleUsage(){
		iterator.addTaskBatch("192.168.1.1","192.168.1.254",80);
		iterator.addTaskBatch("192.168.2.1","192.168.2.254",82);
		for(int i = 0; i < 80; i++){
			iterator.getNextTask();
		}
		Task task = iterator.getNextTask();
		Assert.assertNotNull(task);
		Assert.assertEquals("192.168.1.81",task.getIp());
		Assert.assertEquals(80, task.getPort());
		
		for(int i = 0; i < 426; i++){
			iterator.getNextTask();
		}
		task = iterator.getNextTask();
		Assert.assertNotNull(task);
		Assert.assertEquals("192.168.2.254",task.getIp());
		Assert.assertEquals(82, task.getPort());
		Assert.assertTrue(iterator.hasNextTask() == false);
		task = iterator.getNextTask();
		Assert.assertNull(task);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalValue1(){
		iterator.addTaskBatch("alala", "eee", 80);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalValue2(){
		iterator.addTaskBatch("192.168.1.1", "192.168.1.254", 65536);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalValue3(){
		iterator.addTaskBatch("192.168.1.1", "192.168.1.254", 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalValue4(){
		iterator.addTaskBatch("192.168.1.254", "192.168.1.1", 1);
	}
	
	@Test
	public void testThreadSafety(){
		final CountDownLatch countDownLatch = new CountDownLatch(2);
		
		class NextTaskRunner implements Runnable{
			private TaskIterator iterator;
			private Set<String> taskSets = new HashSet<String>();
			
			public NextTaskRunner(TaskIterator iterator){
				this.iterator = iterator;
			}
			
			public void run() {
				while(iterator.hasNextTask()){
					Task task = iterator.getNextTask();
					String ipTask = task.getIp() + ":" + task.getPort();
					taskSets.add(ipTask);
				}
				countDownLatch.countDown();
			}
			
			public int getTaskSize(){
				return taskSets.size();
			}
		}
		class AddTaskRunner implements Runnable{
			private List<TaskBatch> taskPool = new ArrayList<TaskBatch>();
			private TaskIterator iterator;
			
			public AddTaskRunner(TaskIterator iterator){
				this.iterator = iterator;
			}
			
			public void run(){
				taskPool.add(new TaskBatch(TaskIterator.addrToLong("10.8.11.1"), TaskIterator.addrToLong("10.8.11.254"), 8989));
				taskPool.add(new TaskBatch(TaskIterator.addrToLong("10.8.12.1"), TaskIterator.addrToLong("10.8.12.254"), 8989));
				taskPool.add(new TaskBatch(TaskIterator.addrToLong("10.8.13.1"), TaskIterator.addrToLong("10.8.13.254"), 8989));
				taskPool.add(new TaskBatch(TaskIterator.addrToLong("10.8.14.1"), TaskIterator.addrToLong("10.8.14.254"), 8989));
				taskPool.add(new TaskBatch(TaskIterator.addrToLong("10.8.15.1"), TaskIterator.addrToLong("10.8.15.254"), 8989));
				for(TaskBatch batch : taskPool){
					iterator.addTaskBatch(TaskIterator.longToAddr(batch.getStartIP()),
							TaskIterator.longToAddr(batch.getEndIP()),batch.getPort());
				}
			}
		}
		
		iterator.addTaskBatch("192.168.1.1", "192.170.1.1", 8089);
		iterator.addTaskBatch("10.8.1.1", "10.10.1.1", 8089);
		
		NextTaskRunner runner1 = new NextTaskRunner(iterator);
		NextTaskRunner runner2 = new NextTaskRunner(iterator);
		AddTaskRunner runner3 = new AddTaskRunner(iterator);
		
		new Thread(runner1).start();
		new Thread(runner3).start();
		new Thread(runner2).start();
		
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(131073*2 + 254*5, runner1.getTaskSize() + runner2.getTaskSize());
	}
}

package cn.mrbcy.PortScanner.listener;

import java.util.Set;

import cn.mrbcy.PortScanner.PortScanner;

public interface ScanProgressListener {

	void onScanProgress(Set<String> openRecords,Set<String> closeRecords, long restNum);

	void onScanComplete(Set<String> allOpenRecords);
	
	/**
	 * 扫描任务已经全部提交到扫描序列中
	 * @return 返回true则Scanner会继续保持运行状态直到下次调用该函数。
	 * 在那期间可以继续调用addTaskBatch函数
	 * 返回false则不能再调用addTaskBatch函数添加任务，
	 * Scanner会等待所有已经提交的任务完成，然后调用onScanComplete函数并结束扫描任务
	 */
	boolean onTaskQueueEmpty(PortScanner scanner);
}

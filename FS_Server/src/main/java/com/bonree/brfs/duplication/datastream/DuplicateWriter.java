package com.bonree.brfs.duplication.datastream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonree.brfs.common.asynctask.AsyncExecutor;
import com.bonree.brfs.common.asynctask.AsyncTaskGroup;
import com.bonree.brfs.common.asynctask.AsyncTaskGroupCallback;
import com.bonree.brfs.common.asynctask.AsyncTaskResult;
import com.bonree.brfs.duplication.FidBuilder;
import com.bonree.brfs.duplication.coordinator.DuplicateNode;
import com.bonree.brfs.duplication.coordinator.FileNode;
import com.bonree.brfs.duplication.datastream.connection.DiskNodeConnection;
import com.bonree.brfs.duplication.datastream.connection.DiskNodeConnectionPool;
import com.bonree.brfs.duplication.datastream.file.FileLimiter;
import com.bonree.brfs.duplication.datastream.file.FileLounge;
import com.bonree.brfs.duplication.datastream.handler.DataItem;
import com.bonree.brfs.duplication.datastream.tasks.DataWriteTask;
import com.bonree.brfs.duplication.datastream.tasks.WriteTaskResult;
import com.bonree.brfs.duplication.recovery.FileRecovery;

public class DuplicateWriter {
	private static final Logger LOG = LoggerFactory.getLogger(DuplicateWriter.class);
	
	private static final int DEFAULT_THREAD_NUM = 5;
	private AsyncExecutor executor = new AsyncExecutor(DEFAULT_THREAD_NUM);
	
	private DiskNodeConnectionPool connectionPool;
	private FileLounge fileLounge;
	private FileRecovery fileRecovery;
	
	public DuplicateWriter(FileLounge fileLounge, FileRecovery fileRecovery, DiskNodeConnectionPool connectionPool) {
		this.fileLounge = fileLounge;
		this.fileRecovery = fileRecovery;
		this.connectionPool = connectionPool;
	}
	
	public void write(int storageId, DataItem[] items, DataHandleCallback<DataWriteResult> callback) {
		EmitResultGather resultGather = new EmitResultGather(items.length, callback);
		for(DataItem item : items) {
			if(item == null || item.getBytes() == null || item.getBytes().length == 0) {
				resultGather.putResultItem(new ResultItem(item.getSequence()));
				continue;
			}
			
			try {
				FileLimiter file = fileLounge.getFileInfo(storageId, item.getBytes().length);
				
				emitData(item, file.getFileNode(), resultGather);
			} catch (Exception e) {
				resultGather.putResultItem(new ResultItem(item.getSequence()));
			}
		}
	}
	
	private void emitData(DataItem item, FileNode fileNode, EmitResultGather resultGather) {
		DuplicateNode[] duplicates = fileNode.getDuplicateNodes();
		
		DiskNodeConnection[] connections = new DiskNodeConnection[duplicates.length];
		for(int i = 0; i < connections.length; i++) {
			connections[i] = connectionPool.getConnection(duplicates[i]);
		}
		
		AsyncTaskGroup<WriteTaskResult> taskGroup = new AsyncTaskGroup<WriteTaskResult>();
		for(DiskNodeConnection connection : connections) {
			taskGroup.addTask(new DataWriteTask(connection.getService().getServiceId(), connection, fileNode, item));
		}
		
		if(taskGroup.size() == 0) {
			resultGather.putResultItem(new ResultItem(item.getSequence()));
			return;
		}
		
		executor.submit(taskGroup, new DataWriteResultCallback(item, fileNode, resultGather));
	}
	
	private class DataWriteResultCallback implements AsyncTaskGroupCallback<WriteTaskResult> {
		private DataItem item;
		private FileNode fileNode;
		private EmitResultGather resultGather;
		
		public DataWriteResultCallback(DataItem item, FileNode fileNode, EmitResultGather resultGather) {
			this.item = item;
			this.fileNode = fileNode;
			this.resultGather = resultGather;
		}

		@Override
		public void completed(AsyncTaskResult<WriteTaskResult>[] results) {
			List<WriteTaskResult> taskResultList = getValidResultList(results);
			
			if(taskResultList.isEmpty()) {
				LOG.error("None correct result is return from DiskNode! FILE[" + fileNode.getName() + "]");
				
				resultGather.putResultItem(new ResultItem(item.getSequence()));
				return;
			}
			
			checkTaskResult(taskResultList);
			
			WriteTaskResult taskResult = taskResultList.get(0);
			
			ResultItem resultItem = new ResultItem(item.getSequence());
			resultItem.setFid(FidBuilder.getFid(fileNode, taskResult.getOffset(), taskResult.getSize()));//TODO build FID and notify
			resultGather.putResultItem(resultItem);
		}
		
		private List<WriteTaskResult> getValidResultList(AsyncTaskResult<WriteTaskResult>[] results) {
			List<WriteTaskResult> taskResultList = new ArrayList<WriteTaskResult>(results.length);
			for(AsyncTaskResult<WriteTaskResult> taskResult : results) {
				if(taskResult.getResult() != null) {
					taskResultList.add(taskResult.getResult());
				}
			}
			
			return taskResultList;
		}
		
		private void checkTaskResult(List<WriteTaskResult> results) {
			for(int i = 1; i < results.size(); i++) {
				if(!results.get(i - 1).equals(results.get(i))) {
					LOG.error("Results from DiskNode is not consistent, one is {}, another is {}", results.get(i -1), results.get(i));
				}
			}
		}
	}
	
	private class EmitResultGather {
		private DataHandleCallback<DataWriteResult> callback;
		
		private AtomicInteger count = new AtomicInteger();
		private ResultItem[] resultItems;
		
		public EmitResultGather(int count, DataHandleCallback<DataWriteResult> callback) {
			this.resultItems = new ResultItem[count];
			this.callback = callback;
		}
		
		public void putResultItem(ResultItem item) {
			int index = count.getAndIncrement();
			resultItems[index] = item;
			
			if((index + 1) == resultItems.length) {
				DataWriteResult writeResult = new DataWriteResult();
				writeResult.setItems(resultItems);
				callback.completed(writeResult);
			}
		}
	}
}

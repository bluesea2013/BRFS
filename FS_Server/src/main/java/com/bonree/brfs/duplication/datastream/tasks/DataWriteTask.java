package com.bonree.brfs.duplication.datastream.tasks;

import com.bonree.brfs.common.asynctask.AsyncTask;
import com.bonree.brfs.disknode.client.DiskNodeClient;
import com.bonree.brfs.disknode.client.WriteResult;
import com.bonree.brfs.duplication.coordinator.FileNode;
import com.bonree.brfs.duplication.datastream.connection.DiskNodeConnection;
import com.bonree.brfs.duplication.datastream.handler.DataItem;

public class DataWriteTask extends AsyncTask<WriteTaskResult> {
	private DiskNodeConnection connection;
	private FileNode fileNode;
	private DataItem item;
	
	private String taskId;
	
	public DataWriteTask(String taskId, DiskNodeConnection connection, FileNode fileNode, DataItem item) {
		this.taskId = taskId;
		this.connection = connection;
		this.fileNode = fileNode;
		this.item = item;
	}

	@Override
	public String getTaskId() {
		return taskId;
	}

	@Override
	public WriteTaskResult run() throws Exception {
		DiskNodeClient client = connection.getClient();
		if(client == null) {
			return null;
		}
		
		WriteResult result = client.writeData(fileNode.getName(), item.getBytes());
		WriteTaskResult taskResult = new WriteTaskResult();
		taskResult.setOffset(result.getOffset());
		taskResult.setSize(result.getSize());
		return taskResult;
	}
	
}

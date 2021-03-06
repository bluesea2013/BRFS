package com.bonree.brfs.disknode.server.handler;

import java.io.IOException;

import com.bonree.brfs.common.http.HandleResult;
import com.bonree.brfs.common.http.HandleResultCallback;
import com.bonree.brfs.common.http.MessageHandler;
import com.bonree.brfs.disknode.DiskReader;
import com.bonree.brfs.disknode.server.DiskMessage;

public class ReadMessageHandler implements MessageHandler<DiskMessage> {
	public static final String PARAM_READ_OFFSET = "read_offset";
	public static final String PARAM_READ_LENGTH = "read_length";

	@Override
	public void handle(DiskMessage msg, HandleResultCallback callback) {
		HandleResult result = new HandleResult();
		
		try {
			String offsetParam = msg.getParams().get(PARAM_READ_OFFSET);
			String lengthParam = msg.getParams().get(PARAM_READ_LENGTH);
			int offset = offsetParam == null ? 0 : Integer.parseInt(offsetParam);
			int length = lengthParam == null ? Integer.MAX_VALUE : Integer.parseInt(lengthParam);
			
			DiskReader reader = new DiskReader(msg.getFilePath());
			byte[] data = reader.read(offset, length);
			result.setSuccess(true);
			result.setData(data);
		} catch (IOException e) {
			result.setSuccess(false);
			result.setCause(e);
		} finally {
			callback.completed(result);
		}
		
	}

}

package com.bonree.brfs.disknode;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomWriteWorkerSelector implements WriteWorkerSelector {
	private static final Logger Log = LoggerFactory.getLogger(RandomWriteWorkerSelector.class);
	
	private static Random rand = new Random(System.currentTimeMillis());

	@Override
	public WriteWorker select(WriteWorker[] workers) {
		Log.info("Select a worker from {} workers", workers.length);
		return workers[rand.nextInt(workers.length)];
	}

}

package com.bonree.brfs.resourceschedule.model;

import com.alibaba.fastjson.JSONObject;
import com.bonree.brfs.resourceschedule.model.enums.PatitionEnum;

/*******************************************************************************
 * 版权信息：北京博睿宏远数据科技股份有限公司
 * Copyright: Copyright (c) 2007北京博睿宏远数据科技股份有限公司,Inc.All Rights Reserved.
 *
 * @date 2018-3-7
 * @author: <a href=mailto:zhucg@bonree.com>朱成岗</a>
 * Description: 
 * Version: 
 ******************************************************************************/
public class PatitionStatModel extends AbstractResourceModel{
    /**
     * 文件系统挂载点
     */
    private String mountPoint;
    /**
     * 已使用空间大小，单位kb
     */
    private long usedSize;
    /**
     * 未使用空间大小，单位kb
     */
    private long remainSize;
    /**
     * 写入数据大小，单位byte
     */
    private long writeDataSize;
    /**
     * 读取数据大小，单位byte
     */
    private long readDataSize;

    public PatitionStatModel(String mountPoint, long usedSize, long unusedSize, long writeDataSize, long readDataSize) {
        this.mountPoint = mountPoint;
        this.usedSize = usedSize;
        this.remainSize = unusedSize;
        this.writeDataSize = writeDataSize;
        this.readDataSize = readDataSize;
    }

    public PatitionStatModel(String mountPoint, long unusedSize, long writeDataSize, long readDataSize) {
        this.mountPoint = mountPoint;
        this.remainSize = unusedSize;
        this.writeDataSize = writeDataSize;
        this.readDataSize = readDataSize;
    }
    public JSONObject toJSONObject(){
    	JSONObject obj = new JSONObject();
    	obj.put(PatitionEnum.MOUNT_POINT.name(), this.mountPoint);
    	obj.put(PatitionEnum.USED_SIZE.name(), this.usedSize);
    	obj.put(PatitionEnum.REMAIN_SIZE.name(), this.remainSize);
    	obj.put(PatitionEnum.WIRTE_DATA_SIZE.name(), this.writeDataSize);
    	obj.put(PatitionEnum.READ_DATA_SIZE.name(), this.readDataSize);
    	return obj;
    }
    public String toString(){
    	return toJSONObject().toString();
    }
    public String toJSONString(){
    	return toJSONObject().toJSONString();
    }

    public PatitionStatModel() {
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(long usedSize) {
        this.usedSize = usedSize;
    }

    public long getWriteDataSize() {
        return writeDataSize;
    }

    public void setWriteDataSize(long writeDataSize) {
        this.writeDataSize = writeDataSize;
    }

    public long getReadDataSize() {
        return readDataSize;
    }

    public void setReadDataSize(long readDataSize) {
        this.readDataSize = readDataSize;
    }

	public long getRemainSize() {
		return remainSize;
	}

	public void setRemainSize(long remainSize) {
		this.remainSize = remainSize;
	}
    
}
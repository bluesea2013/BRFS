package com.bonree.brfs.server.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

    private final static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    /** 概述：创建目录
     * @param pathName 需要创建的目录名
     * @param isRecursion 是否递归创建
     * @user <a href=mailto:weizheng@bonree.com>魏征</a>
     */
    public static void createDir(String pathName, boolean isRecursion) {
        File file = new File(pathName);
        if (isRecursion) {
            if (!file.getParentFile().exists()) {
                createDir(file.getParent(), true);
            }
            file.mkdirs();
        } else {
            file.mkdirs();
        }
    }

    public static boolean isDirectory(String fileName) {
        File file = new File(fileName);
        return file.isDirectory();
    }

    public static boolean isExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static boolean createFile(String fileName, boolean isRecursion) {
        File file = new File(fileName);
        if (isRecursion) {
            if (!file.getParentFile().exists()) {
                createDir(file.getParent(), isRecursion);
            }
            try {
                return file.createNewFile();
            } catch (IOException e) {
                LOG.error("create file " + fileName + " fail!!", e);
                return false;
            }
        } else {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                LOG.error("create file " + fileName + " fail!!", e);
                return false;
            }
        }
    }

    public static List<String> readFileByLine(String fileName) {
        File file = new File(fileName);
        if (file.isDirectory()) {
            throw new IllegalArgumentException("fileName not is directory");
        }
        List<String> lines = new ArrayList<String>(128);
        InputStreamReader reader = null;
        BufferedReader br = null;
        String line = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            br = new BufferedReader(reader);
            while ((line = br.readLine()) != null) {
                if (StringUtils.isNotEmpty(line)) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error("read file error!", e);
        } catch (IOException e) {
            LOG.error("read file error!", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.error("close BufferedReader error!", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("close InputStreamReader error!", e);
                }
            }
        }
        return lines;
    }

    public static void writeFileFromList(String fileName, List<String> contents) {
        File file = new File(fileName);
        if (file.isDirectory()) {
            throw new IllegalArgumentException("fileName not is directory");
        }
        OutputStreamWriter writer = null;
        BufferedWriter bw = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            bw = new BufferedWriter(writer);
            for (String record : contents) {
                bw.write(record + "\n");
            }
            bw.flush();
        } catch (UnsupportedEncodingException e) {
            LOG.error("not supported Encoding:", e);
        } catch (FileNotFoundException e) {
            LOG.error("file not found:", e);
        } catch (IOException e) {
            LOG.error("write error:", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    LOG.error("close bw error:", e);
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOG.error("close writer error:", e);
                }
            }
        }
    }

}

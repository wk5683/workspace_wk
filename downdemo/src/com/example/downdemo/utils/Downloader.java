package com.example.downdemo.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.example.downdemo.bean.Info;
import com.example.downdemo.dao.InfoDao;

/**
 * @author wk
 *���ع�����
 */
public class Downloader {
	private int done;
	private int fileLen;
	private InfoDao dao;
	private Handler handler;
	private boolean isPause;
	
	public Downloader(Context context,Handler handler){
		dao = new InfoDao(context);
		this.handler = handler;
	}
	
	public void download(String path,int thCount) throws Exception{
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3000);
		if (conn.getResponseCode() == 200) {
			fileLen = conn.getContentLength();
			String name = path.substring(path.lastIndexOf("/")+1);
			File file = new File(Environment.getExternalStorageDirectory(),name);
			RandomAccessFile raf = new RandomAccessFile(file, "rws");
			raf.setLength(fileLen);
			raf.close();
			
			Message msg = new Message();
			msg.what = 0;
			msg.getData().putInt("fileLen", fileLen);
			handler.sendMessage(msg);
			
			int partLen = (fileLen+thCount - 1)/thCount;
			for (int i = 0; i < thCount; i++) {
				new DownloadThread(url, file, partLen, i).start();  
			}
		}else {
			throw new IllegalArgumentException("404 path: " + path);  
			
			
		}
		
		
	}
	
	
	private final class DownloadThread extends Thread {  
        private URL url;  
        private File file;  
        private int partLen;  
        private int id;  
  
        public DownloadThread(URL url, File file, int partLen, int id) {  
            this.url = url;  
            this.file = file;  
            this.partLen = partLen;  
            this.id = id;  
        }  
  
        /** 
         * д����� 
         */  
        public void run() {  
            // �ж��ϴ��Ƿ���δ�������  
            Info info = dao.query(url.toString(), id);  
            if (info != null) {  
                // �����, ��ȡ��ǰ�߳���������  
                done += info.getDone();  
            } else {  
                // ���û��, �򴴽�һ���¼�¼����  
                info = new Info(url.toString(), id, 0);  
                dao.insert(info);  
            }  
  
            int start = id * partLen + info.getDone(); // ��ʼλ�� += ��������  
            int end = (id + 1) * partLen - 1;  
  
            try {  
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
                conn.setReadTimeout(3000);  
                //��ȡָ��λ�õ����ݣ�Range��Χ������������������ݷ�Χ, ���Է���������ĩβΪ׼  
                conn.setRequestProperty("Range", "bytes=" + start + "-" + end);  
                RandomAccessFile raf = new RandomAccessFile(file, "rws");  
                raf.seek(start);  
                //��ʼ��д����  
                InputStream in = conn.getInputStream();  
                byte[] buf = new byte[1024 * 10];  
                int len;  
                while ((len = in.read(buf)) != -1) {  
                    if (isPause) {  
                        //ʹ���߳����������߳�  
                        synchronized (dao) {  
                            try {  
                                dao.wait();  
                            } catch (InterruptedException e) {  
                                e.printStackTrace();  
                            }  
                        }  
                    }  
                    raf.write(buf, 0, len);  
                    done += len;  
                    info.setDone(info.getDone() + len);  
                    // ��¼ÿ���߳������ص�������  
                    dao.update(info);   
                    //���߳�����Handler������Ϣ�����߳̽�����Ϣ  
                    Message msg = new Message();  
                    msg.what = 1;  
                    msg.getData().putInt("done", done);  
                    handler.sendMessage(msg);  
                }  
                in.close();  
                raf.close();  
                // ɾ�����ؼ�¼  
                dao.deleteAll(info.getPath(), fileLen);   
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
  
    //��ͣ����  
    public void pause() {  
        isPause = true;  
    }  
    //��������  
    public void resume() {  
        isPause = false;  
        //�ָ������߳�  
        synchronized (dao) {  
            dao.notifyAll();  
        }  
    }  
	

}

package com.example.downdemo.bean;

/**
 * @author wk
 * ���������Ϣ
 *
 */
public class Info {
	
	private String path;
	private int thid;
	private int done;
	public Info(String path,int thid,int done){
		this.path = path;
		this.thid = thid;
		this.done = done;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getThid() {
		return thid;
	}
	public void setThid(int thid) {
		this.thid = thid;
	}
	public int getDone() {
		return done;
	}
	public void setDone(int done) {
		this.done = done;
	}
	@Override
	public String toString() {
		return "Info [path=" + path + ", thid=" + thid + ", done=" + done + "]";
	}
	
	
	

}

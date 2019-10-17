package com.huanhong.decathlonstb.model;

public class DataComment {
	int score, upload_no;
	long time;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getUpload_no() {
		return upload_no;
	}

	public void setUpload_no(int upload_no) {
		this.upload_no = upload_no;
	}

	public String getPad_no() {
		return pad_no;
	}

	public void setPad_no(String pad_no) {
		this.pad_no = pad_no;
	}

	String pad_no;
}

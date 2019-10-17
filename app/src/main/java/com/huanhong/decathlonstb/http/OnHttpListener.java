package com.huanhong.decathlonstb.http;

public interface OnHttpListener {
	public void httpDone(int httpId, String result);

	public void httpError(int httpId);

	public void dataError(int httpId, String s);

}

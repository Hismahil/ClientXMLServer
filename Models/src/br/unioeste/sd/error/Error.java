package br.unioeste.sd.error;

public class Error {

	private String title;
	private String msg;
	
	public Error(String title, String msg) {
		super();
		this.title = title;
		this.msg = msg;
	}

	public Error(){}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}

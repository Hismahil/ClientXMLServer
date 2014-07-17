package br.unioeste.sd.msg;

public class Command {

	private String type;
	private String cmdString;
	
	public Command(String type, String cmdString) {
		super();
		this.type = type;
		this.cmdString = cmdString;
	}

	public Command(){}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getCmdString() {
		return cmdString;
	}
	
	public void setCmdString(String cmdString) {
		this.cmdString = cmdString;
	}
	
	
}

package util;

public class StringBean {

	private String key;
	private String value;
	
	public StringBean(){
	}
	
	public StringBean(String key, String value) {
		this.key = key;
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "StringBean [key=" + key + ", value=" + value + "]";
	}
}

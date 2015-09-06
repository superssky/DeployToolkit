package com.wyb.tool.bean;

public class DefaultListModelObject<V> {

	private V value;
	private String label;
	
	public DefaultListModelObject() {
		
	}
	
	public DefaultListModelObject(String label, V value) {
		this.value = value;
		this.label = label;
	}
	
	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return label;
	}
}

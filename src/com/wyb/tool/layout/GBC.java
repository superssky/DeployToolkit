package com.wyb.tool.layout;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GBC extends GridBagConstraints {
	private static final long serialVersionUID = 4950828372214871554L;

	public GBC() {
		super();
	}
	
	public GBC(int gridx, int gridy,
			int gridwidth, int gridheight) {
		this();
		this.gridx = gridx;
		this.gridy = gridy;
		this.gridwidth = gridwidth;
		this.gridheight = gridheight;
	}
	
	public GBC(int gridx, int gridy,
			int gridwidth, int gridheight,  Insets insets) {
		this(gridx, gridy, gridwidth, gridheight);
		this.insets = insets;
	}
	
	public GBC(int gridx, int gridy, int gridwidth, int gridheight,
			double weightx, double weighty, int anchor, int fill,
			Insets insets, int ipadx, int ipady) {
		this.gridx = gridx;
		this.gridy = gridy;
		this.gridwidth = gridwidth;
		this.gridheight = gridheight;
		this.fill = fill;
		this.ipadx = ipadx;
		this.ipady = ipady;
		this.insets = insets;
		this.anchor  = anchor;
		this.weightx = weightx;
		this.weighty = weighty;
	}
	
	public GBC setGridX(int gridx) {
		this.gridx = gridx;
		return this;
	}
	
	public GBC setGridY(int gridy) {
		this.gridy = gridy;
		return this;
	}
	
	public GBC setGridWidth(int gridwidth) {
		this.gridwidth = gridwidth;
		return this;
	}
	
	public GBC setGridHeight(int gridheight) {
		this.gridheight = gridheight;
		return this;
	}
	
	public GBC setWeightX(double weightx) {
		this.weightx = weightx;
		return this;
	}
	
	public GBC setWeightY(double weighty) {
		this.weighty = weighty;
		return this;
	}
	
	public GBC setFill(int fill) {
		this.fill = fill;
		return this;
	}
	
	public GBC setAnchor(int anchor) {
		this.anchor = anchor;
		return this;
	}
}

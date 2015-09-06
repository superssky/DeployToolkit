package com.wyb.tool.bean;

import java.awt.Point;

public enum Location {

	/**东*/
	EAST(0.75, 0.5),
	/**南*/
	SOUTH(0.5, 0.75),
	/**西*/
	WEST(0.25, 0.5),
	/**北*/
	NORTH(0.5, 0.25),
	/**中*/
	CENTER(0.5, 0.5)
	;
	
	private Location(double xPer, double yPer) {
		this.xPer = xPer;
		this.yPer = yPer;
	}
	
	public Point getLoaction(Point refPoint, double refWidth, double refHeight, double width, double height) {
		
		double x = refWidth*xPer - width*0.5;
		double y = refHeight*yPer - height*0.5;
		return new Point(Double.valueOf(x+refPoint.getX()).intValue(),
				Double.valueOf(y+refPoint.getY()).intValue());
	}

	private double xPer;
	private double yPer;
}

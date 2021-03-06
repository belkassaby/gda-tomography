/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.client.tomo;

/**
 *
 */
public class TiltPlotPointsHolder {

	private String tiltPointsTitle;
	
	private DoublePointList centers1;

	private DoublePointList ellipse1;

	private DoublePointList centers2;

	private DoublePointList ellipse2;

	private DoublePointList line2;

	public DoublePointList getCenters1() {
		return centers1;
	}

	public void setCenters1(DoublePointList centers1) {
		this.centers1 = centers1;
	}

	public DoublePointList getCenters2() {
		return centers2;
	}

	public void setCenters2(DoublePointList centers2) {
		this.centers2 = centers2;
	}

	public DoublePointList getLine2() {
		return line2;
	}

	public void setLine2(DoublePointList line2) {
		this.line2 = line2;
	}

	@Override
	public String toString() {
		return "Centres1:" + centers1 + "  Ellipse1:" + ellipse1 + "  Centers2: " + centers2 + "  Ellipse2: "
				+ ellipse2 + "  Line2:" + line2;
	}

	public DoublePointList getEllipse1() {
		return ellipse1;
	}

	public void setEllipse1(DoublePointList ellipse1) {
		this.ellipse1 = ellipse1;
	}

	public DoublePointList getEllipse2() {
		return ellipse2;
	}

	public void setEllipse2(DoublePointList ellipse2) {
		this.ellipse2 = ellipse2;
	}

	public String getTiltPointsTitle() {
		return tiltPointsTitle;
	}

	public void setTiltPointsTitle(String tiltPointsTitle) {
		this.tiltPointsTitle = tiltPointsTitle;
	}

}

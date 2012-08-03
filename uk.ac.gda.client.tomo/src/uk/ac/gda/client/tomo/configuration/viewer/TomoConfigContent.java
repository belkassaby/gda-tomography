/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.configuration.viewer;

/**
 *
 */
public class TomoConfigContent implements ITomoConfigContent {
	private boolean isSelectedToRun = false;
	private double sampleExposureTime;
	private double flatExposureTime;
	private double sampleDetectorDistance;
	private String sampleDescription;
	private String proposalId;
	private String configId;
	private Integer moduleNumber;
	private double energy;
	private String sampleWeight;
	private String resolution;
	private int framesPerProjection;
	private String scanMode;
	private String runTime;
	private String estEndTime;
	private String timeDivider;
	private boolean shouldDisplay = false;
	private double objectPixelSize;
	private double progress;
	private CONFIG_STATUS status = CONFIG_STATUS.NONE;

	public enum CONFIG_STATUS {
		NONE("None"), RUNNING("Running"), COMPLETE("Complete"), FAIL("Fail");
		private final String value;

		CONFIG_STATUS(String value) {
			this.value = value;
		}

		public static CONFIG_STATUS getConfigStatus(String status) {
			for (CONFIG_STATUS cs : values()) {
				if (cs.value.equals(status)) {
					return cs;
				}
			}
			return null;
		}
	}

	@Override
	public double getSampleExposureTime() {
		return sampleExposureTime;
	}

	public void setSampleExposureTime(double sampleExposureTime) {
		this.sampleExposureTime = sampleExposureTime;
	}

	@Override
	public double getFlatExposureTime() {
		return flatExposureTime;
	}

	public void setFlatExposureTime(double flatExposureTime) {
		this.flatExposureTime = flatExposureTime;
	}

	@Override
	public double getSampleDetectorDistance() {
		return sampleDetectorDistance;
	}

	public void setSampleDetectorDistance(double sampleDetectorDistance) {
		this.sampleDetectorDistance = sampleDetectorDistance;
	}

	@Override
	public String getSampleDescription() {
		return sampleDescription;
	}

	public void setSampleDescription(String sampleDescription) {
		this.sampleDescription = sampleDescription;
	}

	public String getProposalId() {
		return proposalId;
	}

	public void setProposalId(String proposalId) {
		this.proposalId = proposalId;
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public void setModuleNumber(Integer moduleNumber) {
		this.moduleNumber = moduleNumber;
	}

	public Integer getModuleNumber() {
		return moduleNumber;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getEnergy() {
		return energy;
	}

	public String getSampleWeight() {
		return sampleWeight;
	}

	public void setSampleWeight(String sampleWeight) {
		this.sampleWeight = sampleWeight;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public int getFramesPerProjection() {
		return framesPerProjection;
	}

	public void setFramesPerProjection(int framesPerProjection) {
		this.framesPerProjection = framesPerProjection;
	}

	public String getScanMode() {
		return scanMode;
	}

	public void setScanMode(String scanMode) {
		this.scanMode = scanMode;
	}

	public String getRunTime() {
		return runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}

	public String getEstEndTime() {
		return estEndTime;
	}

	public void setEstEndTime(String estEndTime) {
		this.estEndTime = estEndTime;
	}

	public String getTimeDivider() {
		return timeDivider;
	}

	public void setTimeDivider(String timeDivider) {
		this.timeDivider = timeDivider;
	}

	public boolean isShouldDisplay() {
		return shouldDisplay;
	}

	public void setShouldDisplay(boolean shouldDisplay) {
		this.shouldDisplay = shouldDisplay;
	}

	public void setModuleObjectPixelSize(double objectPixelSize) {
		this.objectPixelSize = objectPixelSize;
	}

	public double getObjectPixelSize() {
		return objectPixelSize;
	}

	public boolean isSelectedToRun() {
		return isSelectedToRun;
	}

	public void setSelectedToRun(boolean isSelectedToRun) {
		this.isSelectedToRun = isSelectedToRun;
	}

	public CONFIG_STATUS getStatus() {
		return status;
	}

	public void setStatus(CONFIG_STATUS status) {
		this.status = status;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

}
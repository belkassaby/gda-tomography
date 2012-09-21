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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.composites.CameraControlComposite;
import uk.ac.gda.client.tomo.composites.CameraControlComposite.RESOLUTION;
import uk.ac.gda.client.tomo.composites.MotionControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.Resolution;
import uk.ac.gda.tomography.parameters.SampleWeight;
import uk.ac.gda.tomography.parameters.TomoExperiment;

/**
 *
 */
public class TomoConfigContentProvider implements IStructuredContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];
	private final static Logger logger = LoggerFactory.getLogger(TomoConfigContentProvider.class);

	/**
	 * XXX - Need to move this into a configurable value
	 */
	private static final String T3_M1Z = "t3_m1z";

	public TomoConfigContentProvider() {
		super();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object[] getElements(Object parentElement) {
		if (parentElement instanceof TomoExperiment) {
			TomoExperiment tomoExperiment = (TomoExperiment) parentElement;
			return getTomoConfigContent(tomoExperiment);
		}

		if (parentElement instanceof Object[]) {
			return (Object[]) parentElement;
		}

		if (parentElement instanceof Collection) {
			return ((Collection) parentElement).toArray();
		}
		return EMPTY_ARRAY;
	}

	private Object[] getTomoConfigContent(TomoExperiment tomoExperiment) {
		List<AlignmentConfiguration> configurationSet = tomoExperiment.getParameters().getConfigurationSet();

		ArrayList<ITomoConfigContent> configContents = new ArrayList<ITomoConfigContent>();

		// totalTimeTaken is used so that the estimated end time for a given configuration can be calculated.
		for (AlignmentConfiguration alignmentConfiguration : configurationSet) {
			TomoConfigContent configContent = new TomoConfigContent();
			configContent.setSelectedToRun(alignmentConfiguration.getSelectedToRun());
			configContent.setSampleExposureTime(alignmentConfiguration.getSampleExposureTime());
			configContent.setFlatExposureTime(alignmentConfiguration.getFlatExposureTime());
			configContent.setConfigId(alignmentConfiguration.getId());
			configContent.setSampleDescription(alignmentConfiguration.getDescription());
			configContent.setProposalId(alignmentConfiguration.getProposalId());
			configContent.setModuleNumber(alignmentConfiguration.getDetectorProperties().getModuleParameters()
					.getModuleNumber());
			configContent.setEnergy(alignmentConfiguration.getEnergy());
			configContent.setSampleDetectorDistance(alignmentConfiguration.getMotorPosition(T3_M1Z));

			configContent.setSampleWeight(getSampleWeightString(alignmentConfiguration.getSampleWeight()));
			configContent.setResolution(getResolutionString(alignmentConfiguration.getDetectorProperties()
					.getDesired3DResolution()));
			configContent.setTimeDivider(alignmentConfiguration.getDetectorProperties().getAcquisitionTimeDivider());
			configContent.setFramesPerProjection(alignmentConfiguration.getDetectorProperties()
					.getNumberOfFramerPerProjection());
			configContent.setScanMode(alignmentConfiguration.getScanMode().toString());
			configContent.setModuleObjectPixelSize(alignmentConfiguration.getDetectorProperties().getModuleParameters()
					.getHorizontalFieldOfView());
			double runTime = getRunTime(configContent);

			if (configContent.isSelectedToRun()) {
				double estEndTime = runTime;
				for (ITomoConfigContent iTomoConfigContent : configContents) {
					if (iTomoConfigContent.isSelectedToRun()) {
						estEndTime = estEndTime + iTomoConfigContent.getRunTime();
					}
				}
				configContent.setEstEndTime(estEndTime);
			} else {
				configContent.setEstEndTime(0.0);
			}

			configContent.setRunTime(runTime);
			configContents.add(configContent);
		}
		return configContents.toArray();
	}

	private String getResolutionString(Resolution desired3dResolution) {
		switch (desired3dResolution) {
		case FULL:
			return CameraControlComposite.RESOLUTION.FULL.toString();
		case X2:
			return CameraControlComposite.RESOLUTION.TWO_X.toString();
		case X4:
			return CameraControlComposite.RESOLUTION.FOUR_X.toString();
		case X8:
			return CameraControlComposite.RESOLUTION.EIGHT_X.toString();
		}
		return null;
	}

	private String getSampleWeightString(SampleWeight sampleWeight) {
		switch (sampleWeight) {
		case LESS_THAN_1:
			return SAMPLE_WEIGHT.LESS_THAN_ONE.toString();
		case ONE_TO_TEN:
			return SAMPLE_WEIGHT.ONE_TO_TEN.toString();
		case TEN_TO_TWENTY:
			return SAMPLE_WEIGHT.TEN_TO_TWENTY.toString();
		case TWENTY_TO_FIFTY:
			return SAMPLE_WEIGHT.TWENTY_TO_FIFTY.toString();
		}
		return null;
	}

	private double getRunTime(TomoConfigContent configContent) {
		double exp = configContent.getSampleExposureTime();
		double timeDiv = configContent.getTimeDivider();
		double timeDivExp = exp * timeDiv;
		int res = getResolutionIndex(configContent.getResolution());
		double binning = TomoClientActivator.getResolutionBinning().get(res);
		double projections = TomoClientActivator.getResolutionProjections().get(res);
		double time = (timeDivExp / binning) * projections;
		logger.debug("time:{}", time);
		return time;
	}

	private int getResolutionIndex(String resolution) {
		if (resolution.equals(RESOLUTION.FULL.toString())) {
			return 1;
		} else if (resolution.equals(RESOLUTION.TWO_X.toString())) {
			return 2;
		} else if (resolution.equals(RESOLUTION.TWO_X.toString())) {
			return 4;
		} else {
			return 8;
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}

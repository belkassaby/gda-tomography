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

package uk.ac.gda.client.tomo.configuration.view.handlers.impl;

import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.scan.IScanDataPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TomoClientConstants;
import uk.ac.gda.client.tomo.alignment.view.handlers.IMotorHandler;
import uk.ac.gda.client.tomo.configuration.view.handlers.ITomoScanController;
import uk.ac.gda.client.tomo.configuration.view.handlers.IScanControllerUpdateListener;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.Resolution;

public class TomoScanController implements ITomoScanController {

	private ArrayList<IScanControllerUpdateListener> controllerUpdates = new ArrayList<IScanControllerUpdateListener>();

	private static final Logger logger = LoggerFactory.getLogger(TomoScanController.class);

	private IMotorHandler motorHandler;

	public IMotorHandler getMotorHandler() {
		return motorHandler;
	}

	public void setMotorHandler(IMotorHandler motorHandler) {
		this.motorHandler = motorHandler;
	}

	private IObservable tomoScriptController;

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;

	}

	@Override
	public void addControllerUpdateListener(IScanControllerUpdateListener scanControllerUpdateListener) {
		controllerUpdates.add(scanControllerUpdateListener);
	}

	@Override
	public void removeControllerUpdateListener(IScanControllerUpdateListener scanControllerUpdateListener) {
		controllerUpdates.remove(scanControllerUpdateListener);
	}

	@Override
	public void runScan(List<AlignmentConfiguration> configurationSet) {
		int length = 0;
		ArrayList<Integer> moduleNumbers = new ArrayList<Integer>();
		ArrayList<String> configIds = new ArrayList<String>();
		ArrayList<String> descriptions = new ArrayList<String>();
		ArrayList<HashMap<String, Double>> motorMoveMaps = new ArrayList<HashMap<String, Double>>();
		ArrayList<Double> sampleAcqTimeList = new ArrayList<Double>();
		ArrayList<Double> flatAcqTimesList = new ArrayList<Double>();
		ArrayList<Integer> numFramesPerPrList = new ArrayList<Integer>();
		ArrayList<Integer> numProjList = new ArrayList<Integer>();
		ArrayList<String> isContinuousList = new ArrayList<String>();
		ArrayList<Integer> desiredResolutionList = new ArrayList<Integer>();
		ArrayList<Double> timeDividerList = new ArrayList<Double>();
		ArrayList<Double> positionOfBaseAtFlatList = new ArrayList<Double>();
		ArrayList<Double> positionOfBaseInBeamList = new ArrayList<Double>();

		for (AlignmentConfiguration alignmentConfiguration : configurationSet) {
			if (alignmentConfiguration.getSelectedToRun()) {
				length++;
				configIds.add("'" + alignmentConfiguration.getId() + "'");
				descriptions.add("'" + alignmentConfiguration.getDescription() + "'");
				try {

					int moduleNumber = alignmentConfiguration.getDetectorProperties().getModuleParameters()
							.getModuleNumber().intValue();

					moduleNumbers.add(moduleNumber);

					HashMap<String, Double> motorMoveMap = new HashMap<String, Double>() {
						@Override
						public String toString() {
							String string = super.toString();
							return string.replace("=", ":");
						}
					};

					// t3m1z
					Double t3m1zValue = Double.valueOf(alignmentConfiguration.getDetectorStageParameters().getZ()
							.getValue());
					motorMoveMap.put("'" + motorHandler.getCameraStageZMotorName() + "'", t3m1zValue);

					// ss1_tx
					Double ss1txValue = Double.valueOf(alignmentConfiguration.getSampleStageParameters().getCenterX()
							.getValue());
					motorMoveMap.put("'" + motorHandler.getCentreXMotorName() + "'", ss1txValue);

					// ss1_tz
					Double ss1tzValue = Double.valueOf(alignmentConfiguration.getSampleStageParameters().getCenterZ()
							.getValue());
					motorMoveMap.put("'" + motorHandler.getCentreZMotorName() + "'", ss1tzValue);

					// ss1_rx
					Double ss1rxValue = Double.valueOf(alignmentConfiguration.getSampleStageParameters().getTiltX()
							.getValue());
					motorMoveMap.put("'" + motorHandler.getTiltXMotorName() + "'", ss1rxValue);

					// ss1_rz
					Double ss1rzValue = Double.valueOf(alignmentConfiguration.getSampleStageParameters().getTiltZ()
							.getValue());
					motorMoveMap.put("'" + motorHandler.getTiltZMotorName() + "'", ss1rzValue);

					// ss1_x
					Double baseXValue = Double.valueOf(alignmentConfiguration.getSampleStageParameters().getBaseX()
							.getValue());
					motorMoveMap.put("'" + motorHandler.getSampleBaseMotorName() + "'", baseXValue);

					// ss1_y2
					Double verticalValue = Double.valueOf(alignmentConfiguration.getSampleStageParameters()
							.getVertical().getValue());
					motorMoveMap.put("'" + motorHandler.getVerticalMotorName() + "'", verticalValue);

					motorMoveMaps.add(motorMoveMap);

					boolean isContinuousScan = true;

					// def tomoScani12
					// 1.sampleAcquisitionTime, 2.flatAcquisitionTime, 3.numberOfFramesPerProjection,
					// 4. numberofProjections,
					// 5. isContinuousScan, 6. desiredResolution, 7. timeDivider, 8. positionOfBaseAtFlat,
					// 9. positionOfBaseInBeam, 10. mapOfScannablesTobeAddedAsMetaData):
					double sampleAcq = alignmentConfiguration.getSampleExposureTime();
					double flatAcq = alignmentConfiguration.getFlatExposureTime();
					int numFramesPerPr = alignmentConfiguration.getDetectorProperties()
							.getNumberOfFramerPerProjection();
					int numProj = 10;
					String isContinuous = isContinuousScan ? "True" : "False";
					int desiredResolution = getDesiredResolution(alignmentConfiguration.getDetectorProperties()
							.getDesired3DResolution());
					double timeDivider = 0.8;
					double positionOfBaseAtFlat = 0;
					double positionOfBaseInBeam = alignmentConfiguration.getSampleStageParameters().getBaseX()
							.getValue();

					sampleAcqTimeList.add(sampleAcq);
					flatAcqTimesList.add(flatAcq);
					numFramesPerPrList.add(numFramesPerPr);
					numProjList.add(numProj);
					isContinuousList.add(isContinuous);
					desiredResolutionList.add(desiredResolution);
					timeDividerList.add(timeDivider);
					positionOfBaseAtFlatList.add(positionOfBaseAtFlat);
					positionOfBaseInBeamList.add(positionOfBaseInBeam);

				} catch (Exception e) {
					logger.error("TODO put description of error here", e);
				}
			}
		}
		// self, length, configId, moduleNum, motorMoveMap, sampleAcquisitionTime, flatAcquisitionTime,
		// numberOfFramesPerProjection, numberofProjections,
		// isContinuousScan, desiredResolution, timeDivider, positionOfBaseAtFlat, positionOfBaseInBeam
		String setupTomoScanCmd = String
				.format("tomographyScani13.tomographyConfigurationManager.setupTomoScan(%1$d, %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s, %11$s, %12$s, %13$s, %14$s)",
						length, configIds, descriptions, moduleNumbers, motorMoveMaps, sampleAcqTimeList,
						flatAcqTimesList, numFramesPerPrList, numProjList, isContinuousList, desiredResolutionList,
						timeDividerList, positionOfBaseAtFlatList, positionOfBaseInBeamList);
		logger.debug("Setup:{}", setupTomoScanCmd);
		JythonServerFacade.getInstance().runCommand(setupTomoScanCmd);
	}

	private int getDesiredResolution(Resolution desired3dResolution) {
		int res = 1;
		switch (desired3dResolution) {
		case FULL:
			res = 1;
			break;
		case X2:
			res = 2;
			break;
		case X4:
			res = 4;
			break;
		case X8:
			res = 8;
			break;
		}

		return res;
	}

	private IScanDataPointObserver tomoScriptControllerObserver = new IScanDataPointObserver() {

		@Override
		public void update(Object source, final Object arg) {
			if (arg instanceof IScanDataPoint) {
				final IScanDataPoint scanDataPoint = (IScanDataPoint) arg;
				double currentPoint = scanDataPoint.getCurrentPointNumber();
				double numberOfPoints = scanDataPoint.getNumberOfPoints();
				double progress = currentPoint / numberOfPoints * 100;
				for (IScanControllerUpdateListener lis : controllerUpdates) {
					lis.updateScanProgress(progress);
				}
			} else if (source.equals(tomoScriptController)) {
				if (arg instanceof Exception) {
					Exception exception = (Exception) arg;
					logger.error("Problem with scan {}", exception);
					for (IScanControllerUpdateListener lis : controllerUpdates) {
						lis.updateError(exception);
					}
				} else {
					if (arg instanceof String) {
						for (IScanControllerUpdateListener lis : controllerUpdates) {
							lis.updateMessage((String) arg);
						}
					}
				}
			}
		}
	};

	@Override
	public void dispose() {
		if (tomoScriptController != null) {
			tomoScriptController.deleteIObserver(tomoScriptControllerObserver);
		}
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(tomoScriptControllerObserver);
	}

	@Override
	public void isScanRunning() {
		InterfaceProvider.getCommandRunner().evaluateCommand(TomoClientConstants.TOMOGRAPHY_IS_RUNNING_CONFIG_CMD);
	}

	@Override
	public void initialize() {
		if (this.tomoScriptController != null) {
			this.tomoScriptController.addIObserver(tomoScriptControllerObserver);
		}
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(tomoScriptControllerObserver);
	}

}

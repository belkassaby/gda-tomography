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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import java.awt.Rectangle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.TiffFileInfo;
import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraHandler;
import uk.ac.gda.client.tomo.preferences.TomoAlignmentPreferencePage;
import uk.ac.gda.epics.client.views.controllers.IAdBaseViewController;
import uk.ac.gda.epics.client.views.controllers.INDProcViewController;
import uk.ac.gda.epics.client.views.controllers.INDStatModelViewController;
import uk.ac.gda.epics.client.views.model.AdBaseModel;
import uk.ac.gda.epics.client.views.model.FfMpegModel;
import uk.ac.gda.epics.client.views.model.NdArrayModel;
import uk.ac.gda.epics.client.views.model.NdProcModel;
import uk.ac.gda.epics.client.views.model.NdRoiModel;
import uk.ac.gda.epics.client.views.model.NdStatModel;
import uk.ac.gda.tomography.devices.ITomographyDetector;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.ui.components.ZoomButtonComposite.ZOOM_LEVEL;

/**
 * Invokes command to the server using Spring RMI.
 */
public class TomoDetectorHandler implements ICameraHandler, InitializingBean {

	private int imgWidth = 4008;
	private int imgHeight = 2672;

	private static final String lbl_CAPTURING_IMAGE_AT_EXPOSURE_TIME = "Capturing image at exposure time %.2g(s)";
	private static final String lbl_PROGRESS_TAKING_FLAT = "Taking flat";

	private static final String lbl_PROGRESS_TAKING_DARK = "Taking dark";

	private static final String lbl_PROGRESS_DARK_IMAGE_TAKEN_D = "Dark image taken:%d";

	private ITomographyDetector camera;

	private Integer defaultNumOfFlatImages;
	private Integer defaultNumOfDarkImages;
	private String imageFilePath;
	private String flatImageFileName;
	private String flatFileTemplate;

	//
	private String darkFilePath;
	private String darkImageFileName;
	private String darkFileTemplate;

	private Integer defaultModule;

	// FIXME-make this configurable or obtain this from the object value set

	private boolean changingExposureTime = false;
	/**
	 * Pixel size in mm.
	 */
	private Double detectorPixelSize;
	private String demandRawFileName;
	private AdBaseModel adBaseModel;
	private FfMpegModel ffmjpegModel1;
	private FfMpegModel ffmjpegModel2;
	private NdRoiModel ndRoi1Model;
	private NdProcModel ndProc1Model;
	private NdArrayModel ndArrayModel;

	private NdStatModel ndStatModel;

	private Double defaultAcqPeriod;

	private static final Logger logger = LoggerFactory.getLogger(TomoDetectorHandler.class);

	private TomoAlignmentViewController tomoAlignmentViewController;
	private Integer saturationThreshold;
	private int leftWindowBinValue = 4;

	@Override
	public int getRoi1BinValue() {
		return leftWindowBinValue;
	}

	public void setLeftWindowBinValue(int leftWindowBinValue) {
		this.leftWindowBinValue = leftWindowBinValue;
	}

	private INDStatModelViewController ndStatViewController = new INDStatModelViewController.Stub() {
		@Override
		public void updateMax(double max) {
			tomoAlignmentViewController.updateStatMax(max);
		}

		@Override
		public void updateMin(double min) {
			tomoAlignmentViewController.updateStatMin(min);
		}

		@Override
		public void updateMean(double mean) {
			tomoAlignmentViewController.updateStatMean(mean);
		}

		@Override
		public void updateSigma(double sigma) {
			tomoAlignmentViewController.updateStatSigma(sigma);
		}
	};

	public FfMpegModel getFfmjpegModel1() {
		return ffmjpegModel1;
	}

	public void setFfmjpegModel1(FfMpegModel ffmjpegModel) {
		this.ffmjpegModel1 = ffmjpegModel;
	}

	/**
	 * @return Returns the ffmjpegModel2.
	 */
	public FfMpegModel getFfmjpegModel2() {
		return ffmjpegModel2;
	}

	/**
	 * @param ffmjpegModel2
	 *            The ffmjpegModel2 to set.
	 */
	public void setFfmjpegModel2(FfMpegModel ffmjpegModel2) {
		this.ffmjpegModel2 = ffmjpegModel2;
	}

	@Override
	public void setExposureTime(double exposureTime, int amplifierValue) throws Exception {
		changingExposureTime = true;
		try {
			/**/
			logger.info("setting exposure time:" + exposureTime);
			boolean isCameraBusy = getCamera().isAcquiring();
			if (isCameraBusy) {
				getCamera().abort();
			}

			getCamera().setExposureTime(exposureTime);

			if (isCameraBusy) {
				// to put it back in the state it was.
				getCamera().acquireMJpeg(exposureTime, defaultAcqPeriod, Double.valueOf(amplifierValue),
						leftWindowBinValue, leftWindowBinValue);
			}
		} finally {
			changingExposureTime = false;
		}
	}

	@Override
	public void startAcquiring(double acqTime, int amplifierValue) throws Exception {
		/**/
		logger.info("Starting to acquire");
		getCamera().acquireMJpeg(acqTime, defaultAcqPeriod, Double.valueOf(amplifierValue), leftWindowBinValue,
				leftWindowBinValue);
	}

	@Override
	public void stopAcquiring() throws Exception {
		logger.info("Stop acquiring");
		getCamera().abort();
	}

	@Override
	public void takeFlat(IProgressMonitor monitor, int numOfImages, double expTime) throws Exception {
		final SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask(lbl_PROGRESS_TAKING_FLAT, numOfImages);
		final int arrayCounterInitial = adBaseModel.getArrayCounter_RBV();

		IAdBaseViewController takeFlatController = new IAdBaseViewController.Stub() {

			@Override
			public void updateArrayCounter(int arrayCounter) {
				progress.subTask(String.format("Flat image taken:%d", new Integer(arrayCounter - arrayCounterInitial)));
				progress.worked(1);
			}
		};
		adBaseModel.registerAdBaseViewController(takeFlatController);
		logger.info("Take {} flat", numOfImages);
		try {
			getCamera().takeFlat(expTime, numOfImages, getImageFilePath(), flatImageFileName, flatFileTemplate);
		} finally {
			adBaseModel.removeAdBaseViewController(takeFlatController);
		}
	}

	@Override
	public void dispose() {
		logger.info("Disposing PCO Detector Handler");

		adBaseModel.removeAdBaseViewController(adBaseViewController);
		ndProc1Model.removeProcViewController(proc1ModelListener);
		ndStatModel.removeStatViewController(ndStatViewController);
		// ffmjpegModel1.dispose();
		// ffmjpegModel2.dispose();
		// adBaseModel.dispose();
		// ndRoi1Model.dispose();
		// ndProc1Model.dispose();
	}

	@Override
	public String demandRaw(IProgressMonitor monitor, double acqTime, boolean isFlatFieldRequired) throws Exception {
		// Assuming acquisition has already been stopped.
		logger.info("Stopping acquire");
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.subTask(String.format(lbl_CAPTURING_IMAGE_AT_EXPOSURE_TIME, acqTime));

		String fullFileName = getCamera().demandRaw(acqTime, getImageFilePath(), demandRawFileName, false,
				isFlatFieldRequired, false);
		progress.done();
		logger.info("Demand raw captured full file name");
		return fullFileName;
	}

	@Override
	public String demandRawWithStreamOn(IProgressMonitor monitor, boolean flatCorrectionSelected) throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.subTask(String.format("Exposure time %.3g(s)", getAcqExposureRBV()));

		String fullFileName = getCamera().demandRaw(null, getImageFilePath(), demandRawFileName, false,
				flatCorrectionSelected, true);
		progress.done();
		logger.info("Demand raw captured full file name");
		return fullFileName;
	}

	public void setDemandRawFileName(String demandRawFileName) {
		this.demandRawFileName = demandRawFileName;
	}

	public void setDemandRawFilePath(String demandRawFilePath) {
		this.setImageFilePath(demandRawFilePath);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getImageFilePath() == null) {
			throw new IllegalArgumentException("'demandRawFilePath' should be provided");
		}
		if (detectorPixelSize == null) {
			throw new IllegalArgumentException("'detectorPixelSize' should be provided");
		}
		if (defaultNumOfFlatImages == null) {
			throw new IllegalArgumentException("'defaultNumOfFlatImages' should be set");
		}

		init();
	}

	@Override
	public void setZoomRoiLocation(org.eclipse.swt.graphics.Point roiStart) throws Exception {
		getCamera().setZoomRoiStart(new java.awt.Point(roiStart.x, roiStart.y));
	}

	@Override
	public void setupZoom(ZOOM_LEVEL zoomLevel) throws Exception {
		// FIXME-Ravi - need to evaluate these values.
		// x coordinate of the center of the image - 2004
		// y co-ordinate of the center of the image = 1336

		Dimension rectSize = zoomLevel.getRectSize();
		final int roiX = 0;//(int) (((double) imgWidth / 2) - rectSize.width / 2);
		final int roiY = 0;//(int) (((double) imgHeight / 2) - rectSize.height / 2);
		getCamera().setupZoomMJpeg(new Rectangle(roiX, roiY, zoomLevel.getRoiSize().x, zoomLevel.getRoiSize().y),
				new java.awt.Point(zoomLevel.getBin().x, zoomLevel.getBin().y));
	}

	public AdBaseModel getAdBaseModel() {
		return adBaseModel;
	}

	public void setAdBaseModel(AdBaseModel adBaseModel) {
		this.adBaseModel = adBaseModel;
	}

	private IAdBaseViewController adBaseViewController = new IAdBaseViewController.Stub() {
		@Override
		public void updateAcquireState(short acquisitionState) {
			if (!changingExposureTime) {
				logger.info("Updating acquire state:{}", acquisitionState);
				tomoAlignmentViewController.updateAcquireState(acquisitionState);
			}
		}

		@Override
		public void updateAcqExposure(double acqExposure) {
			tomoAlignmentViewController.updateAcqExposure(acqExposure);
		}
	};

	@Override
	public void setViewController(TomoAlignmentViewController tomoAlignmentViewController) {
		this.tomoAlignmentViewController = tomoAlignmentViewController;

	}

	@Override
	public double getArrayRate_RBV() throws Exception {
		return adBaseModel.getArrayRate_RBV();
	}

	@Override
	public double getAcqExposureRBV() throws Exception {
		return adBaseModel.getAcqExposureRBV();
	}

	@Override
	public double getAcqPeriodRBV() throws Exception {
		return adBaseModel.getAcqPeriodRBV();
	}

	@Override
	public int getAcquireState() throws Exception {
		return adBaseModel.getAcquireState();
	}

	@Override
	public int getArrayCounter_RBV() throws Exception {
		return adBaseModel.getArrayCounter_RBV();
	}

	@Override
	public int getNumExposuresCounter_RBV() throws Exception {
		return adBaseModel.getNumExposuresCounter_RBV();
	}

	@Override
	public int getNumImagesCounter_RBV() throws Exception {
		return adBaseModel.getNumImagesCounter_RBV();
	}

	@Override
	public double getTimeRemaining_RBV() throws Exception {
		return adBaseModel.getTimeRemaining_RBV();
	}

	@Override
	public String getDatatype() throws Exception {
		return adBaseModel.getDatatype();
	}

	@Override
	public String getFullMJpegURL() throws Exception {
		return ffmjpegModel1.getMjpegUrl();
	}

	@Override
	public String getZoomImgMJPegURL() throws Exception {
		return ffmjpegModel2.getMjpegUrl();
	}

	/**
	 * @return Returns the detectorPixelSize.
	 */
	@Override
	public Double getDetectorPixelSize() {
		return detectorPixelSize;
	}

	/**
	 * @param detectorPixelSize
	 *            The detectorPixelSize to set.
	 */
	public void setDetectorPixelSize(Double detectorPixelSize) {
		this.detectorPixelSize = detectorPixelSize;
	}

	@Override
	public Integer getRoi1BinX() throws Exception {
		return getCamera().getRoi1BinX();
	}

	@Override
	public Integer getRoi2BinX() throws Exception {
		return getCamera().getRoi2BinX();
	}

	/**
	 * @return Returns the ndRoi1Model.
	 */
	public NdRoiModel getNdRoi1Model() {
		return ndRoi1Model;
	}

	/**
	 * @param ndRoi1Model
	 *            The ndRoi1Model to set.
	 */
	public void setNdRoi1Model(NdRoiModel ndRoi1Model) {
		this.ndRoi1Model = ndRoi1Model;

	}

	@Override
	public String getTiffFullFileName() throws Exception {
		return getCamera().getTiffImageFileName();
	}

	/**
	 * @return Returns the flatImageFileName.
	 */
	public String getFlatImageFileName() {
		return flatImageFileName;
	}

	/**
	 * @param flatImageFileName
	 *            The flatImageFileName to set.
	 */
	public void setFlatImageFileName(String flatImageFileName) {
		this.flatImageFileName = flatImageFileName;
	}

	/**
	 * @return Returns the flatFileTemplate.
	 */
	public String getFlatFileTemplate() {
		return flatFileTemplate;
	}

	/**
	 * @param flatFileTemplate
	 *            The flatFileTemplate to set.
	 */
	public void setFlatFileTemplate(String flatFileTemplate) {
		this.flatFileTemplate = flatFileTemplate;
	}

	/**
	 * @return Returns the imageFilePath.
	 */
	public String getImageFilePath() {
		return imageFilePath;
	}

	/**
	 * @param imageFilePath
	 *            The imageFilePath to set.
	 */
	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}

	@Override
	public void enableFlatCorrection() throws Exception {
		getCamera().enableFlatField();
	}

	@Override
	public void disableFlatCorrection() throws Exception {
		getCamera().disableFlatField();
	}

	@Override
	public String getFlatImageFullFileName() {
		return String.format(flatFileTemplate, imageFilePath, flatImageFileName);
	}

	@Override
	public void takeDark(IProgressMonitor monitor, double acqTime) throws Exception {
		int numberOfImages = TomoClientActivator.getDefault().getPreferenceStore()
				.getInt(TomoAlignmentPreferencePage.TOMO_CLIENT_DARK_NUM_IMG_PREF);

		// Just so that the recursive average is for a minimum of 2 images. it is better to do it this way rather than
		// limit the user to enter numbers greater than or equal to 2
		if (numberOfImages == 1) {
			numberOfImages = numberOfImages + 1;
		}
		final SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask(lbl_PROGRESS_TAKING_DARK, numberOfImages);
		logger.info("Take {} dark images", numberOfImages);
		final int arrayCounterInitial = adBaseModel.getArrayCounter_RBV();

		IAdBaseViewController takeFlatController = new IAdBaseViewController.Stub() {

			@Override
			public void updateArrayCounter(int arrayCounter) {
				progress.subTask(String.format(lbl_PROGRESS_DARK_IMAGE_TAKEN_D, new Integer(arrayCounter
						- arrayCounterInitial)));
				progress.worked(1);
			}
		};
		adBaseModel.registerAdBaseViewController(takeFlatController);

		try {
			getCamera().takeDark(numberOfImages, acqTime, darkFilePath, darkImageFileName, darkFileTemplate);
		} finally {
			adBaseModel.removeAdBaseViewController(takeFlatController);
		}

	}

	/**
	 * @return Returns the darkFilePath.
	 */
	public String getDarkFilePath() {
		return darkFilePath;
	}

	/**
	 * @param darkFilePath
	 *            The darkFilePath to set.
	 */
	public void setDarkFilePath(String darkFilePath) {
		this.darkFilePath = darkFilePath;
	}

	/**
	 * @return Returns the darkImageFileName.
	 */
	public String getDarkImageFileName() {
		return darkImageFileName;
	}

	/**
	 * @param darkImageFileName
	 *            The darkImageFileName to set.
	 */
	public void setDarkImageFileName(String darkImageFileName) {
		this.darkImageFileName = darkImageFileName;
	}

	/**
	 * @return Returns the darkFileTemplate.
	 */
	public String getDarkFileTemplate() {
		return darkFileTemplate;
	}

	/**
	 * @param darkFileTemplate
	 *            The darkFileTemplate to set.
	 */
	public void setDarkFileTemplate(String darkFileTemplate) {
		this.darkFileTemplate = darkFileTemplate;
	}

	/**
	 * @return Returns the ndProc1Model.
	 */
	public NdProcModel getNdProc1Model() {
		return ndProc1Model;
	}

	private INDProcViewController proc1ModelListener = new INDProcViewController.Stub() {
		@Override
		public void updateEnableFlatField(short flatFieldEnabled) {
			if (flatFieldEnabled == 0) {
				tomoAlignmentViewController.updateEnableFlatField(false);
			} else {
				tomoAlignmentViewController.updateEnableFlatField(true);
			}
		}
	};

	/**
	 * @param ndProc1Model
	 *            The ndProc1Model to set.
	 */
	public void setNdProc1Model(NdProcModel ndProc1Model) {
		this.ndProc1Model = ndProc1Model;
	}

	@Override
	public Boolean getProc1FlatFieldCorrection() throws Exception {
		short enableFlatField = ndProc1Model.getEnableFlatField();
		if (enableFlatField == 0) {
			return false;
		}
		return true;
	}

	/**
	 * @return Returns the defaultNumOfFlatImages.
	 */
	public Integer getDefaultNumOfFlatImages() {
		return defaultNumOfFlatImages;
	}

	/**
	 * @param defaultNumOfFlatImages
	 *            The defaultNumOfFlatImages to set.
	 */
	public void setDefaultNumOfFlatImages(Integer defaultNumOfFlatImages) {
		this.defaultNumOfFlatImages = defaultNumOfFlatImages;
		TomoClientActivator.getDefault().getPreferenceStore()
				.setDefault(TomoAlignmentPreferencePage.TOMO_CLIENT_FLAT_NUM_IMG_PREF, defaultNumOfFlatImages);
	}

	/**
	 * @return Returns the defaultNumOfDarkImages.
	 */
	public Integer getDefaultNumOfDarkImages() {
		return defaultNumOfDarkImages;
	}

	/**
	 * @param defaultNumOfDarkImages
	 *            The defaultNumOfDarkImages to set.
	 */
	public void setDefaultNumOfDarkImages(Integer defaultNumOfDarkImages) {
		this.defaultNumOfDarkImages = defaultNumOfDarkImages;
		TomoClientActivator.getDefault().getPreferenceStore()
				.setDefault(TomoAlignmentPreferencePage.TOMO_CLIENT_DARK_NUM_IMG_PREF, defaultNumOfDarkImages);
	}

	@Override
	public int getTakeFlatNumImages() {
		return TomoClientActivator.getDefault().getPreferenceStore()
				.getInt(TomoAlignmentPreferencePage.TOMO_CLIENT_FLAT_NUM_IMG_PREF);
	}

	@Override
	public double getPreferredFlatExposureTime() {
		return TomoClientActivator.getDefault().getPreferenceStore()
				.getDouble(TomoAlignmentPreferencePage.TOMO_CLIENT_FLAT_EXPOSURE_TIME);
	}

	@Override
	public void setPreferredFlatExposureTime(double exposureTime) {
		TomoClientActivator.getDefault().getPreferenceStore()
				.setValue(TomoAlignmentPreferencePage.TOMO_CLIENT_FLAT_EXPOSURE_TIME, exposureTime);
	}

	@Override
	public double getPreferredSampleExposureTime() {
		return TomoClientActivator.getDefault().getPreferenceStore()
				.getDouble(TomoAlignmentPreferencePage.TOMO_CLIENT_SAMPLE_EXPOSURE_TIME);
	}

	/**
	 * @param defaultSampleExposureTime
	 *            The defaultExposureTime to set.
	 */
	public void setDefaultSampleExposureTime(double defaultSampleExposureTime) {
		TomoClientActivator.getDefault().getPreferenceStore()
				.setDefault(TomoAlignmentPreferencePage.TOMO_CLIENT_SAMPLE_EXPOSURE_TIME, defaultSampleExposureTime);

	}

	public void setDefaultFlatExposureTime(double defaultFlatExposureTime) {
		TomoClientActivator.getDefault().getPreferenceStore()
				.setDefault(TomoAlignmentPreferencePage.TOMO_CLIENT_FLAT_EXPOSURE_TIME, defaultFlatExposureTime);
	}

	@Override
	public void setPreferredSampleExposureTime(double exposureTime) {
		TomoClientActivator.getDefault().getPreferenceStore()
				.setValue(TomoAlignmentPreferencePage.TOMO_CLIENT_SAMPLE_EXPOSURE_TIME, exposureTime);
	}

	/**
	 * @return Returns the defaultInitialModule.
	 */
	public Integer getDefaultModule() {
		return defaultModule;
	}

	/**
	 * @param defaultModule
	 *            The defaultInitialModule to set.
	 */
	public void setDefaultModule(Integer defaultModule) {
		this.defaultModule = defaultModule;
	}

	/**
	 * @return Returns the defaultAcqPeriod.
	 */
	public Double getDefaultAcqPeriod() {
		return defaultAcqPeriod;
	}

	/**
	 * @param defaultAcqPeriod
	 *            The defaultAcqPeriod to set.
	 */
	public void setDefaultAcqPeriod(Double defaultAcqPeriod) {
		this.defaultAcqPeriod = defaultAcqPeriod;
	}

	/**
	 * @return Returns the saturationThreshold.
	 */
	@Override
	public Integer getSaturationThreshold() {
		return saturationThreshold;
	}

	/**
	 * @param saturationThreshold
	 *            The saturationThreshold to set.
	 */
	public void setSaturationThreshold(Integer saturationThreshold) {
		this.saturationThreshold = saturationThreshold;
	}

	/**
	 * @return Returns the ndStat.
	 */
	public NdStatModel getNdStatModel() {
		return ndStatModel;
	}

	/**
	 * @param ndStat
	 *            The ndStat to set.
	 */
	public void setNdStatModel(NdStatModel ndStat) {
		this.ndStatModel = ndStat;

	}

	@Override
	public double getStatMin() throws Exception {
		return ndStatModel.getMin();
	}

	@Override
	public double getStatMax() throws Exception {
		return ndStatModel.getMax();
	}

	@Override
	public double getStatMean() throws Exception {
		return ndStatModel.getMean();
	}

	@Override
	public double getStatSigma() throws Exception {
		return ndStatModel.getSigma();
	}

	@Override
	public void init() {
		if (this.ndStatModel != null) {
			this.ndStatModel.registerStatViewController(ndStatViewController);
		}
		if (this.adBaseModel != null) {
			this.adBaseModel.registerAdBaseViewController(adBaseViewController);
		}
		if (this.ndProc1Model != null) {
			this.ndProc1Model.registerProcViewController(proc1ModelListener);
		}
	}

	@Override
	public void setAmplifiedValue(double newExpTime, int factor) throws Exception {
		setExposureTime(newExpTime / factor, factor);
		getCamera().setProcScale(factor);
	}

	/**
	 * @return {@link CAMERA_MODULE} depending on the position of the motor compared with what they are expected to be
	 *         at any given module.
	 */
	public CAMERA_MODULE getCameraModulePosition() {
		//
		return CAMERA_MODULE.FOUR;
	}

	@Override
	public void stopDemandRaw() throws Exception {
		getCamera().abort();
	}

	@Override
	public String getCameraName() {
		return getCamera().getDetectorName();
	}

	@Override
	public short[] getArrayData() throws Exception {
		return ndArrayModel.getShortArray(imgWidth * imgHeight);
	}

	public void setNdArrayModel(NdArrayModel ndArrayModel) {
		this.ndArrayModel = ndArrayModel;
	}

	@Override
	public void resetFileFormat() throws Exception {
		getCamera().resetFileFormat();
	}

	@Override
	public TiffFileInfo getTiffFileInfo() throws Exception {
		String filePath = getCamera().getTiffFilePath();
		String fileName = getCamera().getTiffFileName();
		String fileFormat = getCamera().getTiffFileTemplate();

		TiffFileInfo tiffFileInfo = new TiffFileInfo(filePath, fileName, fileFormat);
		return tiffFileInfo;
	}

	@Override
	public void setTiffFileNumber(int fileNumber) throws Exception {
		getCamera().setTiffFileNumber(0);
	}

	@Override
	public void reset() throws Exception {
		// Need to set the hdfFormat to what is was before resetAll. Using resetAll because there are a whole lot of
		// settings that would need to be copied otherwise.
		boolean hdfFormat = getCamera().isHdfFormat();
		getCamera().resetAll();
		getCamera().setExternalTriggered(Boolean.FALSE);
		getCamera().initDetector();
		getCamera().setHdfFormat(hdfFormat);
	}

	public void setImgHeight(int imgHeight) {
		this.imgHeight = imgHeight;
	}

	public void setImgWidth(int imgWidth) {
		this.imgWidth = imgWidth;
	}

	@Override
	public void setUpForTilt(int minY, int maxY, int minX, int maxX) throws Exception {
		getCamera().abort();
		getCamera().setupForTilt(minY, maxY, minX, maxX);
	}

	@Override
	public String getDarkImageFullFileName() {
		return String.format(darkFileTemplate, darkFilePath, darkImageFileName);
	}

	@Override
	public void resetAfterTilt() throws Exception {
		getCamera().resetAfterTiltToInitialValues();
	}

	/**
	 * @return camera
	 */
	public ITomographyDetector getCamera() {
		return camera;
	}

	/**
	 * @param camera
	 */
	public void setCamera(ITomographyDetector camera) {
		this.camera = camera;
	}

	@Override
	public void disableDarkSubtraction() throws Exception {
		getCamera().disableDarkSubtraction();
	}

	@Override
	public void enableDarkSubtraction() throws Exception {
		getCamera().enableDarkSubtraction();
	}

	@Override
	public String getDarkFieldImageFullFileName() {
		return String.format(darkFileTemplate, darkFilePath, darkImageFileName);
	}

	@Override
	public int getFullImageWidth() {
		return imgWidth;
	}

	@Override
	public int getFullImageHeight() {
		return imgHeight;
	}

	@Override
	public void setProc1ScaleValue(double newScale) throws Exception {
		camera.setProc1Scale(newScale);
	}

	@Override
	public double getProc1Scale() throws Exception {
		return camera.getProc1Scale();
	}

	@Override
	public void applyScalingAndContrast(double offset, double scale) throws Exception {
		camera.setOffsetAndScale(offset, scale);
	}
}

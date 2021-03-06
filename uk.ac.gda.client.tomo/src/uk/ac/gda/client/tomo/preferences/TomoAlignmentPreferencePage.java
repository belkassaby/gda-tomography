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

package uk.ac.gda.client.tomo.preferences;

import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TomoClientActivator;

/**
 * Preference page for tomography alignment.
 */
public class TomoAlignmentPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DOUBLE_REGEX = "\\-?[0-9]*\\.?[0-9]*";

	private Pattern doubleRegexPattern = Pattern.compile(DOUBLE_REGEX);
	public static final String ID = "uk.ac.gda.client.tomo.tomoalignment.prefpage";
	private static final String INVALID_VALUE_ERRMSG = "Invalid value - %1$s";
	private static final String DARK = "Dark";
	private static final String FLAT = "Flat";
	private static final String FAST_PREVIEW = "Fast Preview";
	private static final String PREFERENCES_FOR_TOMOGRAPHY_ALIGNMENT = "Preferences for tomography alignment";
	private static final String SAMPLE_MOVE_DISTANCE = "Sample move Distance";
	private static final String DISTANCE_TO_MOVE_SAMPLE = "Distance to move sample";
	private static final String NUMBER_OF_IMAGES = "Number of Images";
	private static final String FAST_PREVIEW_EXP_TIME = "Fast Preview exposure time threshold";
	private static final String ERR_MSG_ID = "ERR_MSG_ID";
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentPreferencePage.class);
	private IPreferenceStore preferenceStore;
	private Text txtNumFlatImages;
	private Text txtSampleMoveDist;
	private Text txtNumDarkImages;

	private Text txtFastPreviewExpTime;
	public static final String TOMO_CLIENT_FLAT_NUM_IMG_PREF = "TOMO_CLIENT_FLAT_NUM_IMG_PREF";
	public static final String TOMO_CLIENT_DARK_NUM_IMG_PREF = "TOMO_CLIENT_DARK_NUM_IMG_PREF";
	public static final String TOMO_CLIENT_FLAT_DIST_MOVE_PREF = "TOMO_CLIENT_FLAT_DIST_MOVE_PREF";
	public static final String TOMO_CLIENT_SAMPLE_EXPOSURE_TIME = "TOMO_CLIENT_SAMPLE_EXPOSURE_TIME";
	public static final String TOMO_CLIENT_FLAT_EXPOSURE_TIME = "TOMO_CLIENT_FLAT_EXPOSURE_TIME";

	public static final String TOMO_CLIENT_FAST_PREVIEW_EXP_TIME = "TOMO_CLIENT_FAST_PREVIEW_EXP_TIME";
	public static final String TOMO_CLIENT_VERTICAL_STAGE_USE_Y1 = "TOMO_CLIENT_VERTICAL_STAGE_USE_Y1";
	public static final String TOMO_CLIENT_VERTICAL_STAGE_Y2_BEFORE_Y3 = "TOMO_CLIENT_VERTICAL_STAGE_Y2_BEFORE_Y3";

	private Button chkShouldUseY1;

	private Button chkUseY2BeforeY3;

	/**
	 * 
	 */
	public TomoAlignmentPreferencePage() {
	}

	/**
	 * @param title
	 */
	public TomoAlignmentPreferencePage(String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public TomoAlignmentPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		logger.info("Pref page initialized");
		setDescription(PREFERENCES_FOR_TOMOGRAPHY_ALIGNMENT);
		preferenceStore = TomoClientActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		logger.info("Pref page controls created");

		Composite root = new Composite(parent, SWT.None);
		root.setLayout(new GridLayout());
		// Flat group
		Group flatGroup = new Group(root, SWT.None);
		flatGroup.setText(FLAT);
		GridLayout gl = new GridLayout(2, true);

		flatGroup.setLayout(gl);
		flatGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label lblFlatImages = new Label(flatGroup, SWT.None);
		lblFlatImages.setText(NUMBER_OF_IMAGES);
		lblFlatImages.setLayoutData(new GridData());

		int intNumImgs = preferenceStore.getInt(TOMO_CLIENT_FLAT_NUM_IMG_PREF);

		txtNumFlatImages = new Text(flatGroup, SWT.BORDER);
		txtNumFlatImages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtNumFlatImages.setText(Integer.toString(intNumImgs));
		txtNumFlatImages.addModifyListener(intVerifyListener);
		txtNumFlatImages.setData(ERR_MSG_ID, NUMBER_OF_IMAGES);

		Label lblDistanceToMove = new Label(flatGroup, SWT.None);
		lblDistanceToMove.setText(DISTANCE_TO_MOVE_SAMPLE);
		lblDistanceToMove.setLayoutData(new GridData());

		txtSampleMoveDist = new Text(flatGroup, SWT.BORDER);
		txtSampleMoveDist.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSampleMoveDist.setText(Double.toString(preferenceStore.getDouble(TOMO_CLIENT_FLAT_DIST_MOVE_PREF)));
		txtSampleMoveDist.setData(ERR_MSG_ID, SAMPLE_MOVE_DISTANCE);
		txtSampleMoveDist.addModifyListener(doubleVerifyListener);

		// Dark group
		Group darkGroup = new Group(root, SWT.None);
		darkGroup.setText(DARK);
		gl = new GridLayout(2, true);

		darkGroup.setLayout(gl);
		darkGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblNumDarkImages = new Label(darkGroup, SWT.None);
		lblNumDarkImages.setText(NUMBER_OF_IMAGES);
		lblNumDarkImages.setLayoutData(new GridData());

		int intNumDarkImgs = preferenceStore.getInt(TOMO_CLIENT_DARK_NUM_IMG_PREF);

		txtNumDarkImages = new Text(darkGroup, SWT.BORDER);
		txtNumDarkImages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtNumDarkImages.setText(Integer.toString(intNumDarkImgs));
		txtNumDarkImages.addModifyListener(intVerifyListener);
		txtNumDarkImages.setData(ERR_MSG_ID, NUMBER_OF_IMAGES);

		// Fast Preview group
		Group fastPreview = new Group(root, SWT.None);
		fastPreview.setText(FAST_PREVIEW);
		gl = new GridLayout(2, true);

		fastPreview.setLayout(gl);
		fastPreview.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblFastPreviewExpTime = new Label(fastPreview, SWT.None);
		lblFastPreviewExpTime.setText("Exposure Time threshold (s)");
		lblFastPreviewExpTime.setLayoutData(new GridData());

		double fastPreviewExpTime = preferenceStore.getDouble(TOMO_CLIENT_FAST_PREVIEW_EXP_TIME);

		txtFastPreviewExpTime = new Text(fastPreview, SWT.BORDER);
		txtFastPreviewExpTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFastPreviewExpTime.setText(Double.toString(fastPreviewExpTime));
		txtFastPreviewExpTime.addModifyListener(doubleVerifyListener);
		txtFastPreviewExpTime.setData(ERR_MSG_ID, FAST_PREVIEW_EXP_TIME);

		// Dark group
		Group verticalStageGroup = new Group(root, SWT.None);
		verticalStageGroup.setText("Vertical Stage");
		gl = new GridLayout();

		verticalStageGroup.setLayout(gl);
		verticalStageGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		chkShouldUseY1 = new Button(verticalStageGroup, SWT.CHECK | SWT.WRAP);
		chkShouldUseY1.setText("Use Y1 motor while aligning vertical stage");
		chkShouldUseY1.setLayoutData(new GridData());
		chkShouldUseY1.setSelection(preferenceStore.getBoolean(TOMO_CLIENT_VERTICAL_STAGE_USE_Y1));

		chkUseY2BeforeY3 = new Button(verticalStageGroup, SWT.CHECK | SWT.WRAP);
		chkUseY2BeforeY3
				.setText("Move Y2 motor to limits before moving \nY3 motors while moving up \n(the motor usage will be inverted while moving down)");
		chkUseY2BeforeY3.setLayoutData(new GridData());
		chkUseY2BeforeY3.setSelection(preferenceStore.getBoolean(TOMO_CLIENT_VERTICAL_STAGE_Y2_BEFORE_Y3));

		return root;
	}

	private ModifyListener intVerifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			if (source.equals(txtNumFlatImages)) {
				validateIntegerText(txtNumFlatImages);
			}
			if (source.equals(txtNumDarkImages)) {
				validateIntegerText(txtNumDarkImages);
			}
		}

	};
	private ModifyListener doubleVerifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			if (source.equals(txtSampleMoveDist)) {
				validateDoubleValue(txtSampleMoveDist);
			} else if (source.equals(txtFastPreviewExpTime)) {
				validateDoubleValue(txtFastPreviewExpTime);
			}

		}
	};

	@Override
	public boolean performOk() {
		if (validateDoubleValue(txtSampleMoveDist)) {
			preferenceStore.setValue(TOMO_CLIENT_FLAT_DIST_MOVE_PREF, Double.parseDouble(txtSampleMoveDist.getText()));
		}
		if (validateIntegerText(txtNumFlatImages)) {
			preferenceStore.setValue(TOMO_CLIENT_FLAT_NUM_IMG_PREF, Integer.parseInt(txtNumFlatImages.getText()));
		}
		if (validateIntegerText(txtNumDarkImages)) {
			preferenceStore.setValue(TOMO_CLIENT_DARK_NUM_IMG_PREF, Integer.parseInt(txtNumDarkImages.getText()));
		}
		if (validateDoubleValue(txtFastPreviewExpTime)) {
			preferenceStore.setValue(TOMO_CLIENT_FAST_PREVIEW_EXP_TIME,
					Double.parseDouble(txtFastPreviewExpTime.getText()));
		}

		preferenceStore.setValue(TOMO_CLIENT_VERTICAL_STAGE_USE_Y1, chkShouldUseY1.getSelection());
		preferenceStore.setValue(TOMO_CLIENT_VERTICAL_STAGE_Y2_BEFORE_Y3, chkUseY2BeforeY3.getSelection());

		return true;
	}

	@Override
	protected void performDefaults() {
		txtSampleMoveDist.setText(Double.toString(preferenceStore.getDefaultDouble(TOMO_CLIENT_FLAT_DIST_MOVE_PREF)));
		txtNumFlatImages.setText(Integer.toString(preferenceStore.getDefaultInt(TOMO_CLIENT_FLAT_NUM_IMG_PREF)));
		txtNumDarkImages.setText(Integer.toString(preferenceStore.getDefaultInt(TOMO_CLIENT_DARK_NUM_IMG_PREF)));
		txtFastPreviewExpTime
				.setText(Double.toString(preferenceStore.getDefaultInt(TOMO_CLIENT_FAST_PREVIEW_EXP_TIME)));
	}

	private boolean validateDoubleValue(Text txtCtrl) {
		String txt = txtCtrl.getText();
		Object errMsgId = txtCtrl.getData(ERR_MSG_ID);
		boolean result = true;

		if (txt == null || txt.length() < 1) {
			setErrorMessage(String.format(INVALID_VALUE_ERRMSG, errMsgId));
			result = false;
		} else if (!doubleRegexPattern.matcher(txt).matches()) {
			setErrorMessage(String.format(INVALID_VALUE_ERRMSG, errMsgId));
			result = false;
		} else {
			setErrorMessage(null);
		}
		return result;
	}

	private boolean validateIntegerText(Text txtCtrl) {
		Object errMsgId = txtCtrl.getData(ERR_MSG_ID);
		String txt = txtCtrl.getText();
		boolean result = true;
		if (txt == null || txt.length() < 1) {
			setErrorMessage(String.format(INVALID_VALUE_ERRMSG, errMsgId));
			result = false;
		} else if (!txt.matches("(\\d)*")) {
			setErrorMessage(String.format(INVALID_VALUE_ERRMSG, errMsgId));
			result = false;
		} else {
			int intVal = Integer.parseInt(txt);
			if (intVal < 1) {
				setErrorMessage(String.format("Must be greater than 0 - %1$s", errMsgId));
				result = false;
			} else {
				setErrorMessage(null);
			}
		}
		return result;
	}
}

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

import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;

/**
 * Enum defining the states of the left window view in the tomo alignment view.
 */
public enum ViewerDisplayMode {
	SAMPLE_STREAM_LIVE(TomoAlignmentView.SAMPLE_LIVE_STREAM) {

		@Override
		public String getFileName(TomoAlignmentViewController tomoAlignmentViewController) throws Exception {
			return null;
		}
	},
	FLAT_STREAM_LIVE(TomoAlignmentView.FLAT_LIVE_STREAM) {

		@Override
		public String getFileName(TomoAlignmentViewController tomoAlignmentViewController) throws Exception {
			return null;
		}
	},
	SAMPLE_SINGLE(TomoAlignmentView.SAMPLE_SINGLE) {
		@Override
		public String getFileName(TomoAlignmentViewController tomoAlignmentViewController) throws Exception {
			return tomoAlignmentViewController.getDemandRawTiffFullFileName();
		}

	},
	FLAT_SINGLE(TomoAlignmentView.FLAT_SINGLE) {
		@Override
		public String getFileName(TomoAlignmentViewController tomoAlignmentViewController) {
			return tomoAlignmentViewController.getFlatImageFullFileName();
		}

	},
	DARK_SINGLE(TomoAlignmentView.STATIC_DARK) {
		@Override
		public String getFileName(TomoAlignmentViewController tomoAlignmentViewController) throws Exception {
			return tomoAlignmentViewController.getDarkFieldImageFullFileName();
		}
	},
	STREAM_STOPPED(TomoAlignmentView.STREAM_STOPPED) {
		@Override
		public String getFileName(TomoAlignmentViewController tomoAlignmentViewController) throws Exception {
			return null;
		}
	};

	private final String val;

	ViewerDisplayMode(String val) {
		this.val = val;
	}

	public String getVal() {
		return val;
	}

	// public static ViewerDisplayMode getViewDisplayMode(boolean isStreaming, String txt) {
	// if (isStreaming) {
	// return ViewerDisplayMode.SAMPLE_STREAM_LIVE;
	// }
	// for (ViewerDisplayMode img : values()) {
	// if (img.getVal().equals(txt)) {
	// return img;
	// }
	// }
	// return null;
	// }

	public abstract String getFileName(TomoAlignmentViewController tomoAlignmentViewController) throws Exception;

	public static ViewerDisplayMode getDisplayMode(String info) {
		for (ViewerDisplayMode mode : values()) {
			if (info.equals(mode.getVal())) {
				return mode;
			}
		}
		throw new IllegalArgumentException("Display Mode doesn't exist");
	}
}

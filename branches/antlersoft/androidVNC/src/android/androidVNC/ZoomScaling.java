/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import android.graphics.Matrix;
import android.widget.ImageView.ScaleType;

/**
 * @author Michael A. MacDonald
 */
class ZoomScaling extends AbstractScaling {

	private Matrix matrix;
	int canvasXOffset;
	int canvasYOffset;
	float scaling;
	
	/**
	 * @param id
	 * @param scaleType
	 */
	public ZoomScaling() {
		super(R.id.itemZoomable, ScaleType.MATRIX);
		matrix = new Matrix();
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractScaling#getDefaultHandlerId()
	 */
	@Override
	int getDefaultHandlerId() {
		return R.id.itemInputTouchPanZoomMouse;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractScaling#isAbleToPan()
	 */
	@Override
	boolean isAbleToPan() {
		return true;
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractScaling#isValidInputMode(int)
	 */
	@Override
	boolean isValidInputMode(int mode) {
		return mode == R.id.itemInputTouchPanZoomMouse;
	}
	
	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractScaling#zoomIn(android.androidVNC.VncCanvasActivity)
	 */
	@Override
	void zoomIn(VncCanvasActivity activity) {
		resetMatrix();
		scaling += 0.25;
		if (scaling > 4.0)
		{
			scaling = (float)4.0;
			activity.zoomer.setIsZoomInEnabled(false);
		}
		activity.zoomer.setIsZoomOutEnabled(true);
		matrix.postScale(scaling, scaling);
		activity.vncCanvas.setImageMatrix(matrix);
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractScaling#zoomOut(android.androidVNC.VncCanvasActivity)
	 */
	@Override
	void zoomOut(VncCanvasActivity activity) {
		resetMatrix();
		scaling -= 0.25;
		if (scaling < 1.0)
		{
			scaling = (float)1.0;
			activity.zoomer.setIsZoomOutEnabled(false);
		}
		activity.zoomer.setIsZoomInEnabled(true);
		matrix.postScale(scaling, scaling);
		activity.vncCanvas.setImageMatrix(matrix);
	}

	private void resetMatrix()
	{
		matrix.reset();
		matrix.preTranslate(canvasXOffset, canvasYOffset);
	}

	/* (non-Javadoc)
	 * @see android.androidVNC.AbstractScaling#setScaleTypeForActivity(android.androidVNC.VncCanvasActivity)
	 */
	@Override
	void setScaleTypeForActivity(VncCanvasActivity activity) {
		// TODO Auto-generated method stub
		super.setScaleTypeForActivity(activity);
		canvasXOffset = -activity.vncCanvas.getCenteredXOffset();
		canvasYOffset = -activity.vncCanvas.getCenteredYOffset();
		scaling = (float)1.0;
		resetMatrix();
		activity.vncCanvas.setImageMatrix(matrix);
		// Reset the pan position to (0,0)
		activity.vncCanvas.scrollTo(-activity.vncCanvas.getCenteredXOffset(),-activity.vncCanvas.getCenteredYOffset());
	}

}

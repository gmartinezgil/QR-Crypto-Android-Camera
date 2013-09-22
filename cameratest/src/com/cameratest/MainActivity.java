package com.cameratest;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.android.PlanarYUVLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

/**
 * 
 * @author jerry
 *
 */
public class MainActivity extends Activity {
	/*
	 * Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType,
	 * Object>(3); Vector<BarcodeFormat> qrFormat = new
	 * Vector<BarcodeFormat>(1); qrFormat.add(BarcodeFormat.QR_CODE);
	 * hints.put(DecodeHintType.POSSIBLE_FORMATS, qrFormat);
	 * hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
	 * hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
	 */

	private static Hashtable<DecodeHintType, Object> hints;
	static {
		hints = new Hashtable<DecodeHintType, Object>(1);
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
	}
	private static final Pattern ALPHANUMERIC_PATTERN = Pattern
			.compile("[A-Za-z0-9]+");
	//camera
	private SurfaceView surfaceView;
	private SurfaceHolderCallback surfaceHolderCallback;
	private Camera.PreviewCallback cameraPreviewCallback;
	//views
	private ImageView pictureTakenView;
	private View centerView;
	private TextView txtScanResult;
	private Button btnTakePictureAgain;
	private Button btnExit;
	//timers...
	private Timer cameraTimer;
	private CameraTimerTask cameraTimerTask;
	
	private MediaPlayer mediaPlayer;// used only for sounds...
	
	// HVGA
	final static int width = 480;
	final static int height = 320;
	int dstLeft, dstTop, dstWidth, dstHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.setTitle("PagoMovi");
		pictureTakenView = (ImageView) this.findViewById(R.id.ImageView01);
		centerView = (View) this.findViewById(R.id.centerView);
		surfaceView = (SurfaceView) this.findViewById(R.id.sfvCamera);
		surfaceHolderCallback = new SurfaceHolderCallback(
				surfaceView.getHolder());
		txtScanResult = (TextView) this.findViewById(R.id.txtScanResult);
		btnTakePictureAgain = (Button) this.findViewById(R.id.button1);
		btnTakePictureAgain.setClickable(false);
		btnExit = (Button) this.findViewById(R.id.button2);
		btnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		// Initialize the timer
		cameraTimer = new Timer();
		cameraTimerTask = new CameraTimerTask();
		cameraTimer.schedule(cameraTimerTask, 0, 80);
		cameraPreviewCallback = new Camera.PreviewCallback() {
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				// access to the specified range of frames of data
				final PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
						data, width, height, dstLeft, dstTop, dstWidth,
						dstHeight);
				// output a preview image of the picture taken by the camera
				final Bitmap previewImage = source
						.renderCroppedGreyscaleBitmap();
				pictureTakenView.setImageBitmap(previewImage);
				// set this one as the source to decode
				final BinaryBitmap bitmap = new BinaryBitmap(
						new HybridBinarizer(source));
				new DecodeImageTask().execute(bitmap);
			}
		};
		
		// Debug.startMethodTracing("PagoMovi");
	}

	/**
	 * 
	 * @author jerry
	 * 
	 */
	class CameraTimerTask extends TimerTask {
		@Override
		public void run() {
			if (dstLeft == 0) {
				dstLeft = centerView.getLeft() * width
						/ getWindowManager().getDefaultDisplay().getWidth();
				dstTop = centerView.getTop() * height
						/ getWindowManager().getDefaultDisplay().getHeight();
				dstWidth = (centerView.getRight() - centerView.getLeft())
						* width
						/ getWindowManager().getDefaultDisplay().getWidth();
				dstHeight = (centerView.getBottom() - centerView.getTop())
						* height
						/ getWindowManager().getDefaultDisplay().getHeight();
			}
			surfaceHolderCallback.autoFocusAndPreviewCallback();
		}
	}

	/**
	 * Class to decode the image captured on the camera
	 * 
	 * @author jerry
	 * 
	 */
	private class DecodeImageTask extends
			AsyncTask<BinaryBitmap, String, String> {

		@Override
		protected String doInBackground(BinaryBitmap... bitmap) {
			String decodedText = null;
			final Reader reader = new QRCodeReader();
			try {
				final Result result = reader.decode(bitmap[0], hints);
				decodedText = result.getText();
				cameraTimer.cancel();
				/*
				 * String strResult = "BarcodeFormat:" +
				 * result.getBarcodeFormat().toString() + "  text:" +
				 * result.getText(); txtScanResult.setText(strResult);
				 * //Toast.makeText(MainActivity.this, R.string.msgGotIt,
				 * Toast.LENGTH_SHORT).show(); try { //
				 * http://www.soundjay.com/beep-sounds-1.html lots of free
				 * mMediaPlayer = MediaPlayer.create(MainActivity.this,
				 * R.raw.beep1); mMediaPlayer.setLooping(false); Log.e("beep",
				 * "started0"); mMediaPlayer.start(); //
				 * Log.e("beep","started1"); mMediaPlayer
				 * .setOnCompletionListener(new OnCompletionListener() { public
				 * void onCompletion(MediaPlayer m) { finish(); Intent intent =
				 * new Intent(getBaseContext(), DecodedActivity.class);
				 * intent.putExtra("DECODED_TEXT", result.getText());
				 * startActivity(intent); } }); } catch (Exception e) {
				 * Log.e("beep", "error: " + e.getMessage(), e); }
				 */
			} catch (Exception e) {
				decodedText = e.toString();
				// txtScanResult.setText(e.toString());
			}
			return decodedText;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT)
					.show();
			txtScanResult.setText(result);
			if (ALPHANUMERIC_PATTERN.matcher(result).matches()) {
				try {
					mediaPlayer = MediaPlayer.create(MainActivity.this,
							R.raw.beep1);
					mediaPlayer.setLooping(false);
					mediaPlayer.start();
				} catch (Exception e) {
					Log.e("beep", "error: " + e.getMessage(), e);
				}
			}
			// doesn't work...
			btnTakePictureAgain.setClickable(true);
			btnTakePictureAgain.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					cameraTimer.schedule(cameraTimerTask, 0, 80);
					btnTakePictureAgain.setClickable(false);
				}
			});
		}

	}

	/**
	 * 
	 * @author jerry
	 * 
	 */
	private class SurfaceHolderCallback implements SurfaceHolder.Callback {
		private SurfaceHolder holder = null;
		private Camera camera;

		public SurfaceHolderCallback(SurfaceHolder holder) {
			this.holder = holder;
			this.holder.addCallback(this);
			this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(width, height);
			parameters.setPictureFormat(PixelFormat.JPEG);
			camera.setParameters(parameters);
			camera.startPreview();
			Log.e("Camera", "surfaceChanged");
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			camera = Camera.open();
			try {
				camera.setPreviewDisplay(holder);
				Log.e("Camera", "surfaceCreated");
			} catch (IOException e) {
				camera.release();
				camera = null;
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera = null;
			Log.e("Camera", "surfaceDestroyed");
		}

		/**
		 * Auto focus and callback Camera.PreviewCallback
		 */
		public void autoFocusAndPreviewCallback() {
			if (camera != null)
				camera.autoFocus(new Camera.AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						if (success) {
							camera.setOneShotPreviewCallback(cameraPreviewCallback);
						}
					}
				});
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cameraTimer != null) {
			cameraTimer.cancel();
		}
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		// Debug.stopMethodTracing();
	}

}
package com.cameratest;

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DecodedActivity extends Activity {
	private final static String TAG = "DecodedActivity";
	private TextView result;
	private Button ok;
	
	private static final Pattern ALPHA = Pattern.compile("[A-Za-z0-9]+");

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decoded);
		result = (TextView) this.findViewById(R.id.result);
		ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startActivity(new Intent("com.cameratest.MainActivity"));
				finish();
			}

		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			final String decodedText = getIntent().getStringExtra(
					"DECODED_TEXT");
			Log.d(TAG, decodedText);
			if(ALPHA.matcher(decodedText).matches()) {
				final String cryptedText = SimpleCrypto.decrypt(Constant.KEY,
						decodedText);
				Log.d(TAG, cryptedText);
				result.setText(cryptedText);
				
			} else {
				result.setText(decodedText);
			}
		} catch (Exception e) {
			Log.d(TAG, e.toString(), e);
			e.printStackTrace();
		}
	}

}
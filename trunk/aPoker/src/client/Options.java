package client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.zorrozua.asier.R;

public class Options extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.optionslayout);

		final Button aboutButton = (Button) findViewById(R.id.aboutButton);
		aboutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Coming soon...", 2).show();
			}
		});

		final Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("Options","Back button clicked");
				finish();
			}
		});
	}

}

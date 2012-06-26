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
				accederPantallaAbout();
			}
		});

		final Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				volverPantallaAnterior();
			}
		});
	}

	private void accederPantallaAbout()
	{
		showMssg();
	}

	private void volverPantallaAnterior()
	{
		finish();
	}
	
	private void showMssg()
	{
		Toast.makeText(getApplicationContext(), "Coming soon...", 2).show();
	}

}

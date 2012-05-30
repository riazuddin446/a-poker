package client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.zorrozua.asier.R;

public class JoinOrCreate extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.joinorcreatelayout);

		//final Intent joinTable = new Intent(this, JoinTable.class);
		final Intent createTable = new Intent(this, CreateTable.class);

		Button joinButton = (Button) findViewById(R.id.joinButton);
		joinButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Coming soon...", 2).show();
				//startActivity(joinTable);
			}
		});

		Button createButton = (Button) findViewById(R.id.createButton);
		createButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(createTable);
			}
		});

		Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

	}

}

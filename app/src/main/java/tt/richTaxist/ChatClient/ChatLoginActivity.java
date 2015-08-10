package tt.richTaxist.ChatClient;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import tt.richTaxist.R;
import tt.richTaxist.Storage;


public class ChatLoginActivity extends Activity{
	private EditText ipAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_home);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		ipAddress = (EditText) findViewById(R.id.et_IP);
		ipAddress.setText("109.195.91.45");

		Button connect = (Button)findViewById(R.id.chat_login);
		connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Client.SERVER_IP = ipAddress.getText().toString();
				startActivity(new Intent(getBaseContext(), ChatActivity.class));
				//Log.e("ServerIP", Client.SERVER_IP);
			}
		});
	}
}

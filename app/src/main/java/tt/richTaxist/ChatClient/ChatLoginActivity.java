package tt.richTaxist.ChatClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import tt.richTaxist.R;
import tt.richTaxist.Storage;


public class ChatLoginActivity extends Activity{
	static Context context;
	private EditText ipAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_home);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		context = getApplicationContext();
		Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_chat_home));

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

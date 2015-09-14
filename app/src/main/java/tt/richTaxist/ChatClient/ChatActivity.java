package tt.richTaxist.ChatClient;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import tt.richTaxist.R;
import tt.richTaxist.Storage;


public class ChatActivity extends Activity {
	static Context context;
	private ArrayList<String> arrayList;
	private ChatAdapter mAdapter;
	private Client mClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_client);
		context = getApplicationContext();
		Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_chat_client));
		arrayList = new ArrayList<>();

		final EditText editText = (EditText) findViewById(R.id.editText);
		Button send = (Button)findViewById(R.id.send_button);

		//relate the listView from java to the one created in xml
		ListView mList = (ListView)findViewById(R.id.list);
		mAdapter = new ChatAdapter(this, arrayList);
		mList.setAdapter(mAdapter);

		// connect to the server
		new connectTask().execute("");

		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String message = editText.getText().toString();
				//arrayList.add("c: " + message);//add the text in the arrayList
				if (mClient != null) mClient.sendMessage(message);//sends the message to the server
				//refresh the list
				mAdapter.notifyDataSetChanged();
				editText.setText("");
			}
		});
	}

	public class connectTask extends AsyncTask<String,String,Client> {
		@Override
		protected Client doInBackground(String... message) {
			//we create a Client object
			mClient = new Client(new Client.OnMessageReceived() {
				@Override
				//here the messageReceived method is implemented
				public void messageReceived(String message) {
					//this method calls the onProgressUpdate
					publishProgress(message);
				}
			});
			mClient.run();
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			//add in the arrayList the messaged received from server
			arrayList.add(values[0]);
			// notify the adapter. This means that new message received from server was added to the list
			mAdapter.notifyDataSetChanged();
		}
	}
}

package pradeepms.dictionay;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Dictionary extends Activity implements TextToSpeech.OnInitListener {
	String pradeep = "pradeep";
	String collected;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button push = (Button) findViewById(R.id.button);
		push.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText et = (EditText) findViewById(R.id.editText);
				String entered = et.getText().toString();
				if (isNetworkAvailable()) {
					if ((entered.matches(""))) {

						Toast.makeText(getBaseContext(),
								"Empty strings can't have meaning!!",
								Toast.LENGTH_LONG).show();
						return;
					}

					new AccessWebServiceTask().execute(entered);
				} else {
					AlertDialog.Builder noInternetConnection=new AlertDialog.Builder(Dictionary.this);
					noInternetConnection.setMessage("Enable Internet Connection");
					noInternetConnection.setNeutralButton("OK",new DialogInterface.OnClickListener() {
						
						  public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					
						  }
						  });
					noInternetConnection.show();
				}
			}
		});
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	private InputStream OpenHttpConnection(String urlString) throws IOException {

		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP Connection");
		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		} catch (Exception ex) {

		}

		return in;
	}

	private String WordDefinition(String word) {

		InputStream in = null;
		String strDefinition = "";

		try {
			in = OpenHttpConnection("http://services.aonaware.com/dictservice/dictservice.asmx/Define?word="
					+ word);
			Document doc = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;

			try {
				db = dbf.newDocumentBuilder();
				doc = db.parse(in);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			doc.getDocumentElement().normalize();

			NodeList definitionElements = doc
					.getElementsByTagName("Definition");

			for (int i = 0; i < definitionElements.getLength(); i++) {
				Node itemNode = definitionElements.item(i);

				if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
					Element definitionElement = (Element) itemNode;
					NodeList wordDefinitionElements = (definitionElement)
							.getElementsByTagName("WordDefinition");
					strDefinition = "";

					for (int j = 0; j < wordDefinitionElements.getLength(); j++) {

						Element wordDefinitionElement = (Element) wordDefinitionElements
								.item(j);

						NodeList textNodes = ((Node) wordDefinitionElement)
								.getChildNodes();
						strDefinition += ((Node) textNodes.item(0))
								.getNodeValue() + ". \n";

					}

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return strDefinition;
	}

	private class AccessWebServiceTask extends AsyncTask<String, Void, String> {
		AlertDialog.Builder builder1=new AlertDialog.Builder(Dictionary.this);
		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			collected = WordDefinition(urls[0]);
			return collected;
		}

		protected void onPostExecute(String result) {
			/*
			 * Toast.makeText(getBaseContext(), result,
			 * Toast.LENGTH_LONG).show();
			 */
			TextView definition = (TextView) findViewById(R.id.definition);
			Log.d(pradeep, result);
			if (result == "") {
				definition
						.setText("");
				builder1.setMessage("Couldn't find your query!!\n Please enter again..");
				builder1.setNeutralButton("OK",new DialogInterface.OnClickListener() {
					
					  public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				
					  }
					  });
					builder1.show();

			} else {

				definition.setMovementMethod(new ScrollingMovementMethod());
				definition.setTextSize(15);
				definition.setText(result);
			}
		}

	}

	public void onInit(int status) {
		// TODO Auto-generated method stub

	}

}

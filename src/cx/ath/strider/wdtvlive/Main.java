package cx.ath.strider.wdtvlive;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class Main extends Activity implements OnSharedPreferenceChangeListener {
	private Remote remote;
	private final Handler handler = new Handler();
	private ProgressDialog workinDialog;
	private static SharedPreferences preferences;
	private boolean reconnect;

	private OnClickListener clickHandler = new OnClickListener(){
		public void onClick(View v) {
			if(remote == null) {
				initRemote();
			} else {			
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				remote.Press(((ImageButton)v).getTag().toString().charAt(0));
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);        
        
        if(preferences.getBoolean("wdtvlive_auto", false))
        	initRemote();        
        initClickListeners();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {  
    	if(reconnect && requestCode == 0xBEEF) {
    		initRemote();
    		reconnect = false;
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	reconnect = true;
	}
	
    private void initRemote() {
        workinDialog = ProgressDialog.show(Main.this, "", "Connecting to WDTV Live...");
        Thread t = new Thread() {
        	public void run() {        		        		        		
        		String host = preferences.getString("wdtvlive_ip", getResources().getString(R.string.default_wdtvlive_ip));
        		String user = preferences.getString("wdtvlive_user", getResources().getString(R.string.default_wdtvlive_user));
        		String pass = preferences.getString("wdtvlive_password", getResources().getString(R.string.default_wdtvlive_password));
        		
        		remote = new Remote(host, user, pass);
        		handler.post(new Runnable() {
					public void run() {
						workinDialog.dismiss();
						if(remote.isConnected()) {
							Toast.makeText(Main.this, "Connection Established.", Toast.LENGTH_SHORT).show();
						} else {
							showMsgDialog("Failed to connect to WDTV Live!\nVerify the information in Menu -> Options.", "");
						}
					}        			
        		});
        	}
        };
        t.start();
    }
    private void initClickListeners() {
    	((ImageButton)findViewById(R.id.btnPower)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnHome)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnUp)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnRight)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnDown)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnLeft)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnEnter)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnBack)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnStop)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnOption)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnRewind)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnPausePlay)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnForward)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnPrev)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnNext)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnSearch)).setOnClickListener(clickHandler);
    	((ImageButton)findViewById(R.id.btnEject)).setOnClickListener(clickHandler);
    }
    
    private void showMsgDialog(String Message, String Title) {
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle(Title)
		 .setMessage(Message)
		 .setNeutralButton("Ok", null)
		 .setIcon(R.drawable.icon)
		 .show();
    }        
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {    	
    	switch(item.getItemId()) {
    	case R.id.mnuOptions:
    		Intent i = new Intent(Main.this, Preferences.class);
    		startActivityForResult(i, 0xBEEF);
    		break;
    	case R.id.mnuConnect:
    		if(remote != null && remote.isConnected())
    			remote.Close();
    		initRemote();
    		break;
    	}
    	return true;
    }
    
    @Override
    public void onDestroy() {    	
    	if(isFinishing() && remote != null)
    		remote.Close();
    	
    	preferences.unregisterOnSharedPreferenceChangeListener(this);
    	super.onDestroy();
    }
}
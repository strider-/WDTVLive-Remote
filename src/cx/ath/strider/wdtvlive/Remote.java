package cx.ath.strider.wdtvlive;

import java.io.IOException;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import android.util.Log;

public class Remote {
	private Connection conn;
	private boolean isAuthed;
	
	public Remote(String host, String user, String pass) {		
        try {
        	Log.i("Remote", "Opening Connection...");
        	conn = new Connection(host);
        	conn.connect();
        	
        	Log.i("Remote", "Authenticating...");        	
			isAuthed = conn.authenticateWithPassword(user, pass);
			if(!isAuthed)
				throw new IOException("Credentials Invalid.");
						
			Log.i("Remote", "Connection Established.");
		} catch (IOException e) {
			Log.e("Error", "Failed to Authenticate", e);
		}
	}
	public void Press(char command) {
		sendCmd(String.format("echo %c > /tmp/ir_injection", command));
	}
	
	public void Close() {
		Log.i("Remote", "Closing Connection");
		conn.close();
	}
	public boolean isConnected() {
		return isAuthed;
	}
	
	private void sendCmd(String command) {
		if(!isAuthed) {
			Log.i("Remote", "Not connected to WDTV Live.");
			return;
		}
		
		try {
			Log.i("Remote", "Opening Session...");
			Session session = conn.openSession();
			Log.i("Remote", "Sending Command '" + command + "'");
			session.execCommand(command);
			Log.i("Remote", "Command Sent.");
			session.close();
			Log.i("Remote", "Session Closed.");
		} catch(IOException e){
			Log.e("Error", "Failed to send command.", e);
		}
	}
}

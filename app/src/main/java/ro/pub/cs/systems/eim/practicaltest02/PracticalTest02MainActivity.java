package ro.pub.cs.systems.eim.practicaltest02;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PracticalTest02MainActivity extends AppCompatActivity {
    EditText serverPortEditText = null;
    EditText clientAddressEditText = null;
    EditText clientPortEditText = null;
    EditText hourEditText = null;
    EditText minuteEditText = null;
    TextView resultEditText = null;

    ServerThread serverThread = null;

    private final ConnectButtonClickListener connectButtonClickListener = new ConnectButtonClickListener();
    private final QueryButtonClickListener queryButtonClickListener = new QueryButtonClickListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverPortEditText = (EditText)findViewById(R.id.server_port_edit_text);
        clientPortEditText = (EditText)findViewById(R.id.client_port_edit_text);
        clientAddressEditText = (EditText)findViewById(R.id.client_address_edit_text);
        hourEditText = (EditText)findViewById(R.id.hour);
        minuteEditText = (EditText)findViewById(R.id.minute);

        Button connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(connectButtonClickListener);

        Button setButton = findViewById(R.id.set_button);
        setButton.setOnClickListener(queryButtonClickListener);

        Button resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(queryButtonClickListener);

        Button pollButton = findViewById(R.id.poll_button);
        pollButton.setOnClickListener(queryButtonClickListener);
    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }

    private class ConnectButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String serverPort = serverPortEditText.getText().toString();
            if (serverPort.isEmpty()) {
                System.out.println("[MAIN ACTIVITY] Server port should be filled!");
                return;
            }
            serverThread = new ServerThread(Integer.parseInt(serverPort));
            if (serverThread.getServerSocket() == null) {
                System.out.println("[MAIN ACTIVITY] Could not create server thread!");
                return;
            }
            serverThread.start();
        }
    }

    private class QueryButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            // Retrieves the client address and port. Checks if they are empty or not
            //  Checks if the server thread is alive. Then creates a new client thread with the address, port, city and information type
            //  and starts it
            String clientAddress = clientAddressEditText.getText().toString();
            String clientPort = clientPortEditText.getText().toString();
            if (clientAddress.isEmpty() || clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hour = hourEditText.getText().toString();
            String minute = minuteEditText.getText().toString();
            if (hour.isEmpty() || minute.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Parameters from client (hour / minute) should be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            String queryType = view.getId() == R.id.set_button ? Constants.SET_COMMAND : view.getId() == R.id.reset_button ? Constants.RESET_COMMAND : Constants.POLL_COMMAND;

            Log.i(Constants.TAG, "[MAIN ACTIVITY] Query parameters: " + clientAddress + " " + clientPort + " " + hour + " " + minute + " " + queryType);
//            resultEditText.setText(Constants.EMPTY_STRING);

            ClientThread clientThread = new ClientThread(
                    clientAddress,
                    Integer.parseInt(clientPort),
                    hour,
                    minute,
                    queryType,
                    resultEditText
            );
            clientThread.start();
        }
    }
}
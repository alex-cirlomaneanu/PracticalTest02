package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private final String address;
    private final int port;
    private final String hour;
    private final String minute;
    private final String query;
    private final TextView resultTextView;

    public ClientThread(String address, int port, String hour, String minute, String query, TextView resultTextView) {
        this.address = address;
        this.port = port;
        this.hour = hour;
        this.minute = minute;
        this.query = query;
        this.resultTextView = resultTextView;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            // tries to establish a socket connection to the server
            socket = new Socket(address, port);

            // gets the reader and writer for the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            // sends the city and information type to the server
            printWriter.println(hour);
            printWriter.flush();
            printWriter.println(minute);
            printWriter.flush();
            printWriter.println(query);
            printWriter.flush();
            String alarmInformation;

            // reads the weather information from the server
            while ((alarmInformation = bufferedReader.readLine()) != null) {
                final String finalizedAlarmInformation = alarmInformation;

                // updates the UI with the weather information. This is done using postt() method to ensure it is executed on UI thread
                resultTextView.post(() -> resultTextView.setText(finalizedAlarmInformation));
            }
        } // if an exception occurs, it is logged
        catch (Exception e) {
            System.out.println("[CLIENT THREAD] An exception has occurred: " + e.getMessage());
            if (Constants.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    // closes the socket regardless of errors or not
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}

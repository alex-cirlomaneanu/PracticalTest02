package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class CommunicationThread extends Thread {
    private final ServerThread serverThread;
    private final Socket socket;

    // Constructor of the thread, which takes a ServerThread and a Socket as parameters
    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            System.out.println("[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            System.out.println("[COMMUNICATION THREAD] Waiting for parameters from client!");

            String hour = bufferedReader.readLine();
            String minute = bufferedReader.readLine();
            String key = hour + ":" + minute;
            String query = bufferedReader.readLine();

            if (hour == null || hour.isEmpty() || minute == null || minute.isEmpty()) {
                System.out.println("[COMMUNICATION THREAD] Error receiving parameters from client!");
                return;
            }
            HashMap<String, AlarmInformation> data = serverThread.getData();

            AlarmInformation alarmInformation = null;

            switch (query) {
                case Constants.SET_COMMAND:
                    if (data.containsKey(key)) {
                        System.out.println("[COMMUNICATION THREAD] The alarm is already set!");
                        printWriter.println("The alarm is already set!");
                        printWriter.flush();
                    } else {
                        System.out.println("[COMMUNICATION THREAD] The alarm has been set for " + hour + ":" + minute + "!");
                        printWriter.println("The alarm has been set!");
                        printWriter.flush();
                        alarmInformation = new AlarmInformation("Alarm set for " + hour + ":" + minute);
                        serverThread.setData(key, alarmInformation);
                    }
                    break;
                case Constants.RESET_COMMAND:
                    if (data.containsKey(key)) {
                        System.out.println("[COMMUNICATION THREAD] The alarm has been reset!");
                        printWriter.println("The alarm has been reset!");
                        printWriter.flush();
                        serverThread.removeData(key);
                    } else {
                        System.out.println("[COMMUNICATION THREAD] The alarm is not set!");
                        printWriter.println("The alarm is not set!");
                        printWriter.flush();
                    }
                    break;
                case Constants.POLL_COMMAND:
                    if (data.containsKey(key)) {
                        System.out.println("[COMMUNICATION THREAD] The alarm is set!");
                        Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");

                        HttpClient httpClient = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS+ ":" + Constants.PORT);
                        HttpResponse httpGetResponse = httpClient.execute(httpGet);
                        HttpEntity httpGetEntity = httpGetResponse.getEntity();
                        String pageSourceCode = "";


                        if (httpGetEntity != null) {
                            pageSourceCode = EntityUtils.toString(httpGetEntity);
                        }
                        if (pageSourceCode == null) {
                            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                            return;
                        } else
                            Log.i(Constants.TAG, pageSourceCode);
                    } else {
                        System.out.println("[COMMUNICATION THREAD] The alarm is not set!");
                    }
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            if (Constants.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}

package com.pepin_prestini.elim.myapplication.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pepin_prestini.elim.myapplication.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Adrien on 19/02/2018.
 */

public class SocketServerService extends Service {
    public static final String REQUEST_STRING = "myRequest";
    public static final String RESPONSE_STRING = "myResponse";
    public static final String RESPONSE_MESSAGE = "myResponseMessage";
    /** Socket port */
    private static final int PORT = 6666;

    /** The Socket service */
    private ServerSocket server = null;
    /** The Socket connection pool */
    private ExecutorService mExecutorService = null; // thread pool



    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }



    @Override
    public void onCreate() {
        System.out.println("DEBUT DU SERVER...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                creatSocket();
            }
        }).start();

    }



    /**

     * To create a Socket service

     */

    public void creatSocket() {
        try {
            server = new ServerSocket(PORT);
            mExecutorService = Executors.newCachedThreadPool();
            while (true) {
                Socket client = server.accept();
                mExecutorService.execute(new SockectService(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SockectService implements Runnable {
        Socket socket = null;
        private BufferedReader reader = null;
        private PrintWriter writer = null;

        /** The client sends a message */
        private String msg = null;


        public SockectService(Socket socket) {
            this.socket = socket;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override

        public void run() {
            while (!socket.isClosed() && socket.isConnected()) {
                try {
                    if ((msg = reader.readLine()) != null && msg.toString().length() > 0) {
                        Log.i("----", "The received data is: " + msg);
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(MainActivity.SocketServerServiceReceiver.PROCESS_RESPONSE);
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra(RESPONSE_STRING, msg.toString());
                        sendBroadcast(broadcastIntent);

                        //this.sendMsgToClient();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }



        /**

         * Send message to clients

         */

        public void sendMsgToClient() {
            try {
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                Log.i("-----------", "The transmitted data:" + msg);
                writer.println(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

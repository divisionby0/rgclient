package dev.div0.rgyandexmaps.socket;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import dev.div0.rgyandexmaps.Settings;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;

public class AppSocket implements ISocketSender{
    //private String serverUrl = "https://roboguideserver.divisionby0.ru";
    private Socket socket;
    private ISocketListenerCallback socketListenerCallback;

    private String tag = "AppSocket";
    private String userId;

    private static AppSocket instance = null;

    private AppSocket() {
    }

    public static AppSocket getInstance() {
        if (instance == null)
            instance = new AppSocket();

        return instance;
    }

    public void setUserId(String _userId){
        userId = _userId;
    }

    public void setSocketListenerCallback(ISocketListenerCallback _socketListenerCallback){
        socketListenerCallback = _socketListenerCallback;
    }
    public boolean isConnected(){
        if(socket == null){
            return false;
        }
        else{
            return socket.connected();
        }
    }

    public void init(){
        AppSocket that = this;

        try {
            Log.d(tag, "connecting to "+ Settings.socketServerUrl);

            IO.Options options = IO.Options.builder()
                    // IO factory options
                    .setForceNew(false)
                    .setMultiplex(true)

                    // low-level engine options
                    .setTransports(new String[] { WebSocket.NAME, Polling.NAME})
                    .setUpgrade(true)
                    .setRememberUpgrade(false)
                    .setPath("/socket.io/")
                    .setQuery("userId="+userId)
                    .setExtraHeaders(null)

                    // Manager options
                    .setReconnection(true)
                    .setReconnectionAttempts(Integer.MAX_VALUE)
                    .setReconnectionDelay(1_000)
                    .setReconnectionDelayMax(5_000)
                    .setRandomizationFactor(0.5)
                    .setTimeout(20_000)

                    // Socket options
                    .setAuth(null)
                    .build();

            socket = IO.socket(Settings.socketServerUrl, options);

            socket.on(Socket.EVENT_CONNECT,onConnect);
            socket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on("hello", onHello);
            socket.on("yandexApiKey", onYandexApiKey);
            socket.connect();

            Log.d(tag, "socket="+socket);
        }
        catch (URISyntaxException e) {
            socketListenerCallback.onSocketConnectError(e.getMessage());
        }
    }

    /*
    public AppSocket(String _userId, ISocketListenerCallback _socketListenerCallback){
        Log.d(tag, "AppSocket()");

        AppSocket that = this;

        userId = _userId;
        socketListenerCallback = _socketListenerCallback;

        try {
            Log.d(tag, "connecting to "+serverUrl);

            IO.Options options = IO.Options.builder()
                    // IO factory options
                    .setForceNew(false)
                    .setMultiplex(true)

                    // low-level engine options
                    .setTransports(new String[] { WebSocket.NAME, Polling.NAME})
                    .setUpgrade(true)
                    .setRememberUpgrade(false)
                    .setPath("/socket.io/")
                    .setQuery("userId="+userId)
                    .setExtraHeaders(null)

                    // Manager options
                    .setReconnection(true)
                    .setReconnectionAttempts(Integer.MAX_VALUE)
                    .setReconnectionDelay(1_000)
                    .setReconnectionDelayMax(5_000)
                    .setRandomizationFactor(0.5)
                    .setTimeout(20_000)

                    // Socket options
                    .setAuth(null)
                    .build();

            socket = IO.socket(serverUrl, options);

            socket.on(Socket.EVENT_CONNECT,onConnect);
            socket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on("hello", onHello);
            socket.on("yandexApiKey", onYandexApiKey);
            socket.connect();

            Log.d(tag, "socket="+socket);
        }
        catch (URISyntaxException e) {
            socketListenerCallback.onSocketConnectError(e.getMessage());
        }
    }
     */

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketListenerCallback.onSocketConnected();
        }
    };
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketListenerCallback.onSocketDisconnected();
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Object exception = args[0];

            if((EngineIOException)exception!=null){
                String error = ((EngineIOException) exception).getMessage();
                socketListenerCallback.onSocketConnectError(error);
            }
            else{
                socketListenerCallback.onSocketConnectError("");
            }
        }
    };

    private Emitter.Listener onHello = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(tag, "server said hello");
        }
    };

    private Emitter.Listener onYandexApiKey = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String key = args[0].toString();
            Log.d(tag, "server send yandex api key "+key);
            socketListenerCallback.onYandexAPIKey(key);
        }
    };

    @Override
    public void sendPosition(Double lat, Double lng){

        JSONObject json = new JSONObject();

        Log.d(tag,"sendPosition socket="+socket);

        try {
            json.put("id", userId);
            json.put("lat", lat);
            json.put("lng", lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit("onCoordinates", json.toString());
    }

    public void sendLog(String message){
        JSONObject json = new JSONObject();

        try {
            json.put("id", userId);
            json.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit("clientLog", json.toString());
    }
}

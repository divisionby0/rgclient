package dev.div0.rgyandexmaps.socket;

public interface ISocketListenerCallback {
    void onSocketConnected();
    void onSocketDisconnected();
    void onSocketConnectError(String error);
    void onYandexAPIKey(String key);
}

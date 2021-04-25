package dev.div0.rgyandexmaps.socket;

import org.json.JSONException;

import io.socket.client.Socket;

public interface ISocketSender {
    void sendPosition(Double lat, Double lng) throws JSONException;
}

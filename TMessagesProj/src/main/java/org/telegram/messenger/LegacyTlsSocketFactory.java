package org.telegram.messenger;

import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Litegram: Android 4.4 supports TLS 1.1/1.2 but does not enable them for client sockets
 * (that only happens on Android 5.0+). Most HTTPS servers (e.g. the DNS-over-HTTPS endpoints
 * Telegram uses to find working servers) require TLS 1.2 nowadays, so enable every supported
 * TLS version on sockets created through {@link HttpsURLConnection}.
 */
public final class LegacyTlsSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegate;

    private LegacyTlsSocketFactory(SSLSocketFactory delegate) {
        this.delegate = delegate;
    }

    public static void installIfNeeded() {
        if (Build.VERSION.SDK_INT >= 21) {
            return;
        }
        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, null, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(new LegacyTlsSocketFactory(context.getSocketFactory()));
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private Socket enableTls(Socket socket) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            ArrayList<String> protocols = new ArrayList<>();
            for (String protocol : sslSocket.getSupportedProtocols()) {
                if (protocol.startsWith("TLS")) {
                    protocols.add(protocol);
                }
            }
            if (!protocols.isEmpty()) {
                sslSocket.setEnabledProtocols(protocols.toArray(new String[0]));
            }
        }
        return socket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTls(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTls(delegate.createSocket());
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableTls(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableTls(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTls(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTls(delegate.createSocket(address, port, localAddress, localPort));
    }
}

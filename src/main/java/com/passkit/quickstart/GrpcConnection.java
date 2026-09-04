package com.passkit.quickstart;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GrpcConnection {
    private static final Logger logger = Logger.getLogger(GrpcConnection.class.getName());

    private final ManagedChannel channel;
    private final GrpcConnectionPool pool;

    private static String mHost;
    private static int mPort;

    private static SslContext buildSslContext(String host, int port, String trustFile, String clientCertFile, String clientKeyFile, String keyPassword) throws SSLException {
        mHost = host;
        mPort = port;
        return GrpcSslContexts.forClient()
                .trustManager(new File(trustFile))
                .keyManager(new File(clientCertFile), new File(clientKeyFile), keyPassword)
                .build();
    }

    public GrpcConnection() throws IOException {
        AppConfig config = AppConfig.load();
        pool = new GrpcConnectionPool(config);
        channel = null;
    }

    public GrpcConnection(String host, int port, String trustFile, String clientCertFile, String clientKeyFile, String keyPassword) throws SSLException {
        this(buildSslContext(host, port, trustFile, clientCertFile, clientKeyFile, keyPassword));
    }

    public GrpcConnection(SslContext sslContext) {
        this(NettyChannelBuilder.forAddress(mHost, mPort)
                .negotiationType(NegotiationType.TLS)
                .sslContext(sslContext)
                .build());
    }

    public GrpcConnection(ManagedChannel channel) {
        this.channel = channel;
        this.pool = null;
    }

    public final ManagedChannel getChannel() throws Exception {
        if (pool != null) return pool.getChannel();
        if (channel == null) {
            throw new Exception("GrpcConnection has not been initialised. Call GrpcConnection() with defaults or GrpcConnection(String host, int port, String trustFile, String clientCertFile, String clientKeyFile, String keyPassword)");
        }
        return channel;
    }

    public final void closeChannel() {
        if (pool != null) {
            pool.shutdown();
            return;
        }
        if (channel == null || channel.isShutdown()) {
            return;
        }
        channel.shutdown();
        try {
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) channel.shutdownNow();
        } catch (InterruptedException e) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

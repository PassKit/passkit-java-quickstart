package com.passkit.quickstart;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcConnectionPool {
    private final List<ManagedChannel> channelPool = new ArrayList<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final int poolSize;

    // Build SslContext similar to GrpcConnection class
    private static SslContext buildSslContext(String host, int port, String trustFile, String clientCertFile,
            String clientKeyFile, String keyPassword) throws SSLException {
        return GrpcSslContexts.forClient()
                .trustManager(new File(trustFile))
                .keyManager(new File(clientCertFile), new File(clientKeyFile), keyPassword)
                .build();
    }

    public GrpcConnectionPool(int poolSize) throws IOException {
        this(AppConfig.load(), poolSize);
    }

    public GrpcConnectionPool(AppConfig config) throws IOException {
        this(config, config.poolSize());
    }

    private GrpcConnectionPool(AppConfig config, int poolSize) throws IOException {
        if (poolSize < 1) throw new IllegalArgumentException("poolSize must be at least 1");
        this.poolSize = poolSize;
        String host = config.host();
        int port = config.port();

        for (int i = 0; i < poolSize; i++) {
            try {
                SslContext ctx = buildSslContext(host, port,
                        config.caChain(), config.certificate(), config.key(), config.keyPassword());

                ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                        .sslContext(ctx)
                        .build();

                channelPool.add(channel);
            } catch (SSLException e) {
                e.printStackTrace();
                throw new SSLException("Couldn't build SSL context", e);
            }
        }
    }

    // Round-robin selection of channels from the pool
    public ManagedChannel getChannel() {
        int index = currentIndex.getAndUpdate(i -> (i + 1) % poolSize);
        return channelPool.get(index);
    }

    public void shutdown() {
        for (ManagedChannel channel : channelPool) {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) channel.shutdownNow();
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

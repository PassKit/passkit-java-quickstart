package com.passkit.quickstart;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GrpcConnection {
    private static final Logger logger = Logger.getLogger(GrpcConnection.class.getName());

    private final ManagedChannel channel;

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
        Properties properties = new Properties();
        try {
            properties.load(GrpcConnection.class.getResourceAsStream("/passkit.properties"));
            mHost = properties.getProperty("grpc.host", "grpc.pub1.passkit.io");
            mPort = Integer.parseInt(properties.getProperty("grpc.port", "443"));
            try {
                SslContext ctx = buildSslContext(mHost, mPort,
                        properties.getProperty("credentials.chain", "src/main/resources/credentials/ca-chain.pem"),
                        properties.getProperty("credentials.certificate", "src/main/resources/credentials/certificate.pem"),
                        properties.getProperty("credentials.key", "src/main/resources/credentials/key-java.pem"),
                        properties.getProperty("credentials.password", "password"));
                channel = NettyChannelBuilder.forAddress(mHost, mPort)
                        .negotiationType(NegotiationType.TLS)
                        .sslContext(ctx)
                        .build();
            } catch (SSLException e) {
                logger.log(Level.SEVERE, "couldn't build SSL context from passkit.properties values or program defaults");
                e.printStackTrace();
                throw new SSLException("couldn't build SSL context from passkit.properties values or program defaults", e);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "couldn't initiate connection from passkit.properties or program defaults");
            e.printStackTrace();
            throw new IOException("couldn't initiate connection from passkit.properties or program defaults", e);
        }
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
    }

    public final ManagedChannel getChannel() throws Exception {
        if (channel == null) {
            throw new Exception("GrpcConnection has not been initialised. Call GrpcConnection() with defaults or GrpcConnection(String host, int port, String trustFile, String clientCertFile, String clientKeyFile, String keyPassword)");
        }
        return channel;
    }
}

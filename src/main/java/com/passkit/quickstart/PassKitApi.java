package com.passkit.quickstart;

import com.passkit.grpc.Analytics.AnalyticsGrpc;
import com.passkit.grpc.CertificatesGrpc;
import com.passkit.grpc.DistributionGrpc;
import com.passkit.grpc.EventTickets.EventTicketsGrpc;
import com.passkit.grpc.Flights.FlightsGrpc;
import com.passkit.grpc.ImagesGrpc;
import com.passkit.grpc.IntegrationsGrpc;
import com.passkit.grpc.Members.MembersGrpc;
import com.passkit.grpc.MessagesGrpc;
import com.passkit.grpc.Raw.RawGrpc;
import com.passkit.grpc.Scheduler.SchedulerGrpc;
import com.passkit.grpc.SingleUseCoupons.SingleUseCouponsGrpc;
import com.passkit.grpc.TemplatesGrpc;
import com.passkit.grpc.UsersGrpc;

/**
 * Shared access to every service exposed by the PassKit Java SDK.
 * Each stub is assigned a channel from the configured round-robin pool.
 */
public final class PassKitApi implements AutoCloseable {
    private final GrpcConnection connection;
    private final boolean allowDestructive;

    public PassKitApi() throws Exception { this(false); }
    public PassKitApi(boolean allowDestructive) throws Exception {
        connection = new GrpcConnection();
        this.allowDestructive = allowDestructive;
    }

    public AnalyticsGrpc.AnalyticsBlockingStub analytics() throws Exception { return AnalyticsGrpc.newBlockingStub(connection.getChannel()); }
    public CertificatesGrpc.CertificatesBlockingStub certificates() throws Exception { return CertificatesGrpc.newBlockingStub(connection.getChannel()); }
    public DistributionGrpc.DistributionBlockingStub distribution() throws Exception { return DistributionGrpc.newBlockingStub(connection.getChannel()); }
    public EventTicketsGrpc.EventTicketsBlockingStub eventTickets() throws Exception { return EventTicketsGrpc.newBlockingStub(connection.getChannel()); }
    public FlightsGrpc.FlightsBlockingStub flights() throws Exception { return FlightsGrpc.newBlockingStub(connection.getChannel()); }
    public ImagesGrpc.ImagesBlockingStub images() throws Exception { return ImagesGrpc.newBlockingStub(connection.getChannel()); }
    public IntegrationsGrpc.IntegrationsBlockingStub integrations() throws Exception { return IntegrationsGrpc.newBlockingStub(connection.getChannel()); }
    public MembersGrpc.MembersBlockingStub members() throws Exception { return MembersGrpc.newBlockingStub(connection.getChannel()); }
    public MembersGrpc.MembersBlockingStub loyalty() throws Exception { return members(); }
    public MessagesGrpc.MessagesBlockingStub messages() throws Exception { return MessagesGrpc.newBlockingStub(connection.getChannel()); }
    public RawApi raw() throws Exception { return new RawApi(connection.getChannel()); }
    public RawGrpc.RawBlockingStub rawStub() throws Exception { return RawGrpc.newBlockingStub(connection.getChannel()); }
    public SchedulerGrpc.SchedulerBlockingStub scheduler() throws Exception { return SchedulerGrpc.newBlockingStub(connection.getChannel()); }
    public CouponsApi coupons() throws Exception { return new CouponsApi(connection.getChannel()); }
    public SingleUseCouponsGrpc.SingleUseCouponsBlockingStub couponsStub() throws Exception { return SingleUseCouponsGrpc.newBlockingStub(connection.getChannel()); }
    public TemplatesGrpc.TemplatesBlockingStub templates() throws Exception { return TemplatesGrpc.newBlockingStub(connection.getChannel()); }
    public UsersGrpc.UsersBlockingStub users() throws Exception { return UsersGrpc.newBlockingStub(connection.getChannel()); }
    public UsersGrpc.UsersBlockingStub scanners() throws Exception { return users(); }

    public Advanced advanced() {
        if (!allowDestructive) {
            throw new IllegalStateException(
                    "Advanced bulk operations are disabled. Construct PassKitApi(true) to enable them.");
        }
        return new Advanced();
    }

    public final class Advanced {
        private Advanced() {}
        public MembersGrpc.MembersBlockingStub loyalty() throws Exception { return members(); }
        public SingleUseCouponsGrpc.SingleUseCouponsBlockingStub coupons() throws Exception { return couponsStub(); }
        public EventTicketsGrpc.EventTicketsBlockingStub eventTickets() throws Exception { return PassKitApi.this.eventTickets(); }
    }

    @Override public void close() { connection.closeChannel(); }
}

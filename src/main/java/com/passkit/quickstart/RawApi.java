package com.passkit.quickstart;

import com.google.protobuf.Empty;
import com.passkit.grpc.CommonObjects.Id;
import com.passkit.grpc.Raw.PassOuterClass.Pass;
import com.passkit.grpc.Raw.PassOuterClass.ListPassesByPassProjectRequest;
import com.passkit.grpc.Raw.PassOuterClass.ListPassesByPassTemplateRequest;
import com.passkit.grpc.Raw.PassOuterClass.PassRecordByExternalIdRequest;
import com.passkit.grpc.Raw.Project.PassProject;
import com.passkit.grpc.Raw.Project.PassProjectCopyRequest;
import com.passkit.grpc.Raw.RawGrpc;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;

/** Combines the Java SDK's blocking raw API with its bidirectional update stream. */
public final class RawApi {
    private final RawGrpc.RawBlockingStub blocking;
    private final RawGrpc.RawStub async;

    RawApi(Channel channel) {
        blocking = RawGrpc.newBlockingStub(channel);
        async = RawGrpc.newStub(channel);
    }

    public Id createPassProject(PassProject request) { return blocking.createPassProject(request); }
    public PassProject getPassProject(Id request) { return blocking.getPassProject(request); }
    public PassProject updatePassProject(PassProject request) { return blocking.updatePassProject(request); }
    public Id copyPassProject(PassProjectCopyRequest request) { return blocking.copyPassProject(request); }
    public Empty deletePassProject(Id request) { return blocking.deletePassProject(request); }
    public Id createPass(Pass request) { return blocking.createPass(request); }
    public Pass getPassById(Id request) { return blocking.getPassById(request); }
    public Pass getPassByExternalId(PassRecordByExternalIdRequest request) { return blocking.getPassByExternalId(request); }
    public Id updatePass(Pass request) { return blocking.updatePass(request); }
    public Empty deletePass(Pass request) { return blocking.deletePass(request); }
    public Iterator<Pass> listPassesByPassProject(ListPassesByPassProjectRequest request) {
        return blocking.listPassesByPassProject(request);
    }
    public Iterator<Pass> listPassesByPassTemplate(ListPassesByPassTemplateRequest request) {
        return blocking.listPassesByPassTemplate(request);
    }
    public StreamObserver<Pass> streamPassUpdates(StreamObserver<Id> responses) {
        return async.streamPassUpdates(responses);
    }
}

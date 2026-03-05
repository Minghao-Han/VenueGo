package com.happy.UserService.grpc;

import com.happy.grpc.UserProfileProto.InitProfileRequest;
import com.happy.grpc.UserProfileProto.InitProfileResponse;
import com.happy.grpc.UserProfileServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import com.happy.UserService.service.UserProfileService;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class UserProfileGrpcService
        extends UserProfileServiceGrpc.UserProfileServiceImplBase {

    private final UserProfileService userProfileService;

    @Override
    public void initProfile(InitProfileRequest request,
                            StreamObserver<InitProfileResponse> responseObserver) {
        try {
            userProfileService.createProfile(
                    UUID.fromString(request.getUserId()),
                    request.getUsername(),
                    request.getEmail()
            );

            responseObserver.onNext(InitProfileResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Profile created")
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }
}
package com.example.grpc;

import java.io.IOException;
import java.util.logging.Logger;

import com.example.StandardUserMessage;
import com.example.exceptions.MessageValidationException;
import com.example.storage.LocalStorage;
import com.example.user.GenericResponse;
import com.example.user.User;
import com.example.user.UserServiceGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcServer {
	private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

	private static Server server;

	public static void startup() throws InterruptedException, IOException {
		if (server == null) {
			start();
			blockUntilShutdown();
		}
	}

	private static void start() throws IOException {
		/* The port on which the server should run */
		int port = 50051;
		server = ServerBuilder.forPort(port).addService(new UserServiceImpl()).build().start();
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown
				// hook.
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				GrpcServer.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	private static void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	private static void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * GRPC stub implementation. This is where we will interact with our persistence
	 * layer.
	 * 
	 * @author tbox
	 *
	 */
	static class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
		@Override
		public void upsert(com.example.user.User request,
				io.grpc.stub.StreamObserver<com.example.user.GenericResponse> responseObserver) {
			try {
				LocalStorage.write(new StandardUserMessage(request));
				responseObserver.onNext(
						GenericResponse.newBuilder().setDescription("Upsert Successful").setStatus(200).build());
			} catch (MessageValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				responseObserver
						.onNext(GenericResponse.newBuilder().setDescription("Upsert Failed!").setStatus(500).build());
			}
			responseObserver.onCompleted();
		}

		@Override
		public void get(com.example.user.GetUserRequest request,
				io.grpc.stub.StreamObserver<com.example.user.User> responseObserver) {
			StandardUserMessage msg = LocalStorage.read(StandardUserMessage.class, User.newBuilder(), request.getId(),
					null);
			responseObserver.onNext(msg == null ? User.getDefaultInstance() : msg.getMessage());
			responseObserver.onCompleted();
		}
	}
}

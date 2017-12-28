package xyz.luan.test;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class SpeechAPI {

	private Consumer<String> consumer;
	private final ApiStreamObserver<StreamingRecognizeRequest> requestObserver;

	public SpeechAPI(int sampleRate, String credentialsFile) throws IOException {
		InputStream credentialFile = SpeechAPI.class.getResourceAsStream(credentialsFile);
		CredentialsProvider provider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(credentialFile));
		SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(provider).build();
		SpeechClient speech = SpeechClient.create(settings);

		RecognitionConfig recConfig = RecognitionConfig.newBuilder()
				.setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
				.setLanguageCode("en-US")
				.setSampleRateHertz(sampleRate)
				.build();
		StreamingRecognitionConfig config = StreamingRecognitionConfig.newBuilder()
				.setConfig(recConfig)
				.build();
		BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse> callable = speech.streamingRecognizeCallable();

		requestObserver = callable.bidiStreamingCall(new ApiStreamObserver<StreamingRecognizeResponse>() {

			@Override
			public void onNext(StreamingRecognizeResponse streamingRecognizeResponse) {
				String spokenText = streamingRecognizeResponse.getResultsList().get(0).getAlternativesList().get(0).getTranscript();
				if (consumer != null) {
					consumer.accept(spokenText);
				}
			}

			@Override
			public void onError(Throwable throwable) {
				throw new RuntimeException(throwable);
			}

			@Override
			public void onCompleted() {
				close();
			}
		});

		// The first request must **only** contain the audio configuration:
		requestObserver.onNext(StreamingRecognizeRequest.newBuilder().setStreamingConfig(config).build());
	}

	public void listen(Consumer<String> consumer) {
		this.consumer = consumer;
	}

	public void speak(byte[] data) {
		// Subsequent requests must **only** contain the audio data.
		requestObserver.onNext(StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(data)).build());
	}

	public void close() {
		requestObserver.onCompleted();
	}
}

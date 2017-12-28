package xyz.luan.test;

public class Main {

	private static final int SAMPLE_RATE = 16000;

	public static void main(String[] args) throws Exception {
		Microphone mic = new Microphone(SAMPLE_RATE);
		SpeechAPI api = new SpeechAPI(SAMPLE_RATE, "/speech-test-key.json");

		mic.listen(api::speak);

		api.listen(text -> {
			System.out.println("> " + text);
			if (text.contains("exit")) {
				System.out.println("I heard 'exit'. It's time to say goodbye o/");
				mic.close();
				api.close();
			}
		});

		mic.start();
	}
}

package com.clovagoogleapitest.Google;


import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@RestController
@Data
@Slf4j
public class SpeechToText {

    /**
     * PCM 오디오에서 블로킹되지 않는 음성 인식을 수행하고 전사를 출력합니다. 전사는 60초 오디오로 제한됩니다.
     *
     * fileName: 전사할 PCM 오디오 파일의 경로입니다.
     */
    @GetMapping(value = "asyncRecognizeFile")
    public static void asyncRecognizeFile(HttpServletRequest httpServletRequest) throws Exception {
        String fileName = httpServletRequest.getParameter("fileName");
        try (SpeechClient speech = SpeechClient.create()) {
            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            ByteString audioBytes = ByteString.copyFrom(data);

            // 로컬 PCM 오디오를 사용하여 요청을 구성합니다
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.MP3)
                            .setLanguageCode("en-US")
                            .setLanguageCode("ko-KR")
                            .setSampleRateHertz(16000)
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // 파일 전사에 대한 블로킹되지 않는 호출을 사용합니다
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speech.longRunningRecognizeAsync(config, audio);

            while (!response.isDone()) {
                System.out.println("응답을 기다리는 중...");
                Thread.sleep(10000);
            }

            List<SpeechRecognitionResult> results = response.get().getResultsList();

            for (SpeechRecognitionResult result : results) {
                // 주어진 음성의 한 부분에 대해 여러 대안적인 전사가 있을 수 있습니다. 여기서는 첫 번째(가장 가능성이 높은) 것만 사용합니다.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());
            }
        }
    }


}

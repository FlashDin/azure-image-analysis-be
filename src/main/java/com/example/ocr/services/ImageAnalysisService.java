package com.example.ocr.services;

import com.azure.ai.vision.common.ImageSourceBuffer;
import com.azure.ai.vision.common.VisionServiceOptions;
import com.azure.ai.vision.common.VisionSource;
import com.azure.ai.vision.imageanalysis.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageAnalysisService {
    @Autowired
    private Environment env;

    public Map<String, Object> analyze(MultipartFile multipartFile) {
        Map<String, Object> map = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        ImageSourceBuffer imageSourceBuffer = new ImageSourceBuffer();
        try (
                VisionServiceOptions serviceOptions = new VisionServiceOptions(new URL(env.getProperty("vision-endpoint")), env.getProperty("vision-key"));
                ImageAnalysisOptions analysisOptions = new ImageAnalysisOptions()
        ) {
            byte[] imgBytes = multipartFile.getBytes();
            imageSourceBuffer.getWriter().write(ByteBuffer.wrap(imgBytes));
            VisionSource imageSource = VisionSource.fromImageSourceBuffer(imageSourceBuffer);
            analysisOptions.setFeatures(EnumSet.of(ImageAnalysisFeature.CAPTION, ImageAnalysisFeature.TEXT));
            analysisOptions.setModelVersion("latest");
            analysisOptions.setLanguage("en");
            analysisOptions.setGenderNeutralCaption(false);
            try (
                    ImageAnalyzer analyzer = new ImageAnalyzer(serviceOptions, imageSource, analysisOptions);
                    ImageAnalysisResult result = analyzer.analyze()) {
                if (result.getReason() == ImageAnalysisResultReason.ANALYZED) {
                    map = objectMapper.convertValue(result, new TypeReference<Map<String, Object>>() {});
                } else {
                    ImageAnalysisErrorDetails errorDetails = ImageAnalysisErrorDetails.fromResult(result);
                    map = objectMapper.convertValue(errorDetails, new TypeReference<Map<String, Object>>() {});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            imageSourceBuffer.closeWriter();
            imageSourceBuffer.close();
        }
        return map;
    }

    @SneakyThrows
    public ResponseEntity<Object> analyzeUseRest(MultipartFile multipartFile) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Ocp-Apim-Subscription-Key", env.getProperty("vision-key"));
//        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
//        Resource file = multipartFile.getResource();
//        multipartBodyBuilder.part("file", multipartFile.getBytes(), MediaType.APPLICATION_OCTET_STREAM);
//        MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();
//        HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(multipartBody, headers);
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(multipartFile.getBytes(), headers);
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(env.getProperty("vision-endpoint") + "?features=read&model-version=latest&language=en&gender-neutral-caption=false&api-version=2023-10-01", httpEntity, Object.class);
        return responseEntity;
    }
}

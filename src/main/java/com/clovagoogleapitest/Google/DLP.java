package com.clovagoogleapitest.Google;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.*;
import com.google.privacy.dlp.v2.InfoTypeTransformations.InfoTypeTransformation;
import com.google.protobuf.ByteString;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@Data
public class DLP {
    @GetMapping(value = "/inspectString")
    // 제공된 텍스트를 검사합니다.
    public void inspectString(HttpServletRequest httpServletRequest) throws IOException {

        String projectId = httpServletRequest.getParameter("projectId");
        String textToInspect = httpServletRequest.getParameter("textToInspect");
        String getInfoType = httpServletRequest.getParameter("infoType");

        // 요청을 보내는 데 사용될 클라이언트를 초기화합니다. 이 클라이언트는 한 번만 생성되며,
        // 여러 요청에 대해 재사용될 수 있습니다. 모든 요청을 완료한 후에는
        // 클라이언트의 "close" 메소드를 호출하여 남아 있는 백그라운드 리소스를 안전하게 정리하세요.
        try (DlpServiceClient dlp = DlpServiceClient.create()) {
            // 검사할 유형과 콘텐츠를 지정합니다.
            ContentItem item = ContentItem.newBuilder().setValue(textToInspect).build();

            // 검사할 정보의 유형을 지정합니다.
            // https://cloud.google.com/dlp/docs/infotypes-reference를 참조하여 전체 정보 유형 목록을 확인하세요.
            InfoType infoType = InfoType.newBuilder().setName(getInfoType).build();

            // Inspect 요청을 위한 구성을 구축합니다.
            InspectConfig config =
                    InspectConfig.newBuilder()
                            .setIncludeQuote(true)
                            .setMinLikelihood(Likelihood.POSSIBLE)
                            .addInfoTypes(infoType)
                            .build();

            // 클라이언트가 보낼 Inspect 요청을 구성합니다.
            InspectContentRequest request =
                    InspectContentRequest.newBuilder()
                            .setParent(LocationName.of(projectId, "global").toString())
                            .setItem(item)
                            .setInspectConfig(config)
                            .build();

            // 클라이언트를 사용하여 API 요청을 보냅니다.
            InspectContentResponse response = dlp.inspectContent(request);

            // 응답을 파싱하고 결과를 처리합니다.
            System.out.println("Findings: " + response.getResult().getFindingsCount());
            for (Finding f : response.getResult().getFindingsList()) {
                System.out.println("\tQuote: " + f.getQuote());
                System.out.println("\tInfo type: " + f.getInfoType().getName());
                System.out.println("\tLikelihood: " + f.getLikelihood());
            }
        }
    }

    @GetMapping(value = "/inspectTextFile")
    public static void inspectTextFile(HttpServletRequest httpServletRequest) throws IOException {
        // 요청을 보낼 클라이언트 초기화. 이 클라이언트는 한 번만 생성되어야 하며,
        // 여러 요청에 대해 재사용될 수 있습니다. 모든 요청을 완료한 후,
        // 클라이언트의 "close" 메서드를 호출하여 남은 배경 자원을 안전하게 정리하세요.

        String projectId = httpServletRequest.getParameter("projectId");
        String filePath = httpServletRequest.getParameter("filePath");

        try (DlpServiceClient dlp = DlpServiceClient.create()) {
            // 검사할 타입과 콘텐츠를 지정합니다.
            ByteString fileBytes = ByteString.readFrom(new FileInputStream(filePath));
            ByteContentItem byteItem =
                    ByteContentItem.newBuilder().setType(ByteContentItem.BytesType.TEXT_UTF8).setData(fileBytes).build();
            ContentItem item = ContentItem.newBuilder().setByteItem(byteItem).build();

            // 검사가 찾을 정보의 타입을 지정합니다.
            List<InfoType> infoTypes = new ArrayList<>();

            String[] typeNameList = httpServletRequest.getParameter("typeName").split(",");

            // 모든 정보 타입의 완전한 목록은 https://cloud.google.com/dlp/docs/infotypes-reference 에서 확인하세요.
            //for (String typeName : new String[] {"PHONE_NUMBER", "EMAIL_ADDRESS", "IP_ADDRESS", "CREDIT_CARD_NUMBER"}) {
            for (String typeName : typeNameList) {
                infoTypes.add(InfoType.newBuilder().setName(typeName.trim()).build());
            }

            // Inspect 요청에 대한 구성을 만듭니다.
            InspectConfig config =
                    InspectConfig.newBuilder().setIncludeQuote(true).build();

            // 클라이언트가 보낼 Inspect 요청을 구성합니다.
            InspectContentRequest request =
                    InspectContentRequest.newBuilder()
                            .setParent(LocationName.of(projectId, "global").toString())
                            .setItem(item)
                            .setInspectConfig(config)
                            .build();

            // 클라이언트를 사용하여 API 요청을 보냅니다.
            InspectContentResponse response = dlp.inspectContent(request);

            // 응답을 해석하고 결과를 처리합니다.
            System.out.println("Findings: " + response.getResult().getFindingsCount());
            for (Finding f : response.getResult().getFindingsList()) {
                System.out.println("\tQuote: " + f.getQuote());
                System.out.println("\tInfo type: " + f.getInfoType().getName());
                System.out.println("\tLikelihood: " + f.getLikelihood());
            }
        }
    }

    @GetMapping(value = "deIdentifyWithReplacement")
    public static void deIdentifyWithReplacement(HttpServletRequest httpServletRequest) {
        // 요청을 보내기 위해 사용될 클라이언트를 초기화합니다. 이 클라이언트는 한 번만 생성되며,
        // 여러 요청에 대해 재사용될 수 있습니다. 모든 요청을 완료한 후, 클라이언트에 대해 "close" 메소드를 호출하여
        // 남아 있는 백그라운드 리소스를 안전하게 정리합니다.

        String projectId = httpServletRequest.getParameter("projectId");
        String textToRedact = httpServletRequest.getParameter("textToRedact");

        try (DlpServiceClient dlp = DlpServiceClient.create()) {
            // 검사할 내용을 지정합니다.
            ContentItem item = ContentItem.newBuilder().setValue(textToRedact).build();

            // 검사가 찾을 정보의 유형을 지정합니다.
            // 전체 정보 유형 목록은 https://cloud.google.com/dlp/docs/infotypes-reference 에서 확인하세요
            InfoType infoType = InfoType.newBuilder().setName("EMAIL_ADDRESS").build();
            InspectConfig inspectConfig = InspectConfig
                    .newBuilder()
                    .addInfoTypes(infoType)
                    .addInfoTypes(infoType)
                    .build();
            // 찾은 정보에 사용될 대체 문자열을 지정합니다.
            ReplaceValueConfig replaceValueConfig =
                    ReplaceValueConfig.newBuilder()
                            .setNewValue(Value.newBuilder().setStringValue("[email-address]").build())
                            .build();
            // 비식별화의 유형을 대체로 정의합니다.
            PrimitiveTransformation primitiveTransformation =
                    PrimitiveTransformation.newBuilder().setReplaceConfig(replaceValueConfig).build();
            // 정보 유형과 비식별화 유형을 연결합니다.
            InfoTypeTransformation transformation =
                    InfoTypeTransformation.newBuilder()
                            .addInfoTypes(infoType)
                            .setPrimitiveTransformation(primitiveTransformation)
                            .build();
            // Redact 요청을 위한 구성을 구성하고 원하는 모든 변환을 나열합니다.
            DeidentifyConfig redactConfig =
                    DeidentifyConfig.newBuilder()
                            .setInfoTypeTransformations(
                                    InfoTypeTransformations.newBuilder().addTransformations(transformation))
                            .build();

            // 클라이언트가 보낼 Redact 요청을 구성합니다.
            DeidentifyContentRequest request =
                    DeidentifyContentRequest.newBuilder()
                            .setParent(LocationName.of(projectId, "global").toString())
                            .setItem(item)
                            .setDeidentifyConfig(redactConfig)
                            .setInspectConfig(inspectConfig)
                            .build();

            // 클라이언트를 사용하여 API 요청을 보냅니다.
            DeidentifyContentResponse response = dlp.deidentifyContent(request);

            // 응답을 파싱하고 결과를 처리합니다.
            System.out.println("Text after redaction: " + response.getItem().getValue());
        } catch (Exception e) {
            System.out.println("Error during inspectString: \n" + e.toString());
        }
    }

    // 지정된 이미지 파일을 검사합니다.
    @GetMapping("inspectImageFile")
    public static void inspectImageFile(HttpServletRequest httpServletRequest) throws IOException {
        // 요청을 보내기 위해 사용될 클라이언트를 초기화합니다. 이 클라이언트는 한 번만 생성되며,
        // 여러 요청에 대해 재사용될 수 있습니다. 모든 요청을 완료한 후, 클라이언트에 대해 "close" 메소드를 호출하여
        // 남아 있는 백그라운드 리소스를 안전하게 정리합니다.

        String projectId = httpServletRequest.getParameter("projectId");
        String filePath = httpServletRequest.getParameter("filePath");

        try (DlpServiceClient dlp = DlpServiceClient.create()) {
            // 검사할 유형과 내용을 지정합니다.
            ByteString fileBytes = ByteString.readFrom(new FileInputStream(filePath));
            ByteContentItem byteItem =
                    ByteContentItem.newBuilder().setType(ByteContentItem.BytesType.IMAGE).setData(fileBytes).build();
            ContentItem item = ContentItem.newBuilder().setByteItem(byteItem).build();

            // 검사가 찾을 정보의 타입을 지정합니다.
            List<InfoType> infoTypes = new ArrayList<>();

            String[] typeNameList = httpServletRequest.getParameter("typeName").split(",");

            // 모든 정보 타입의 완전한 목록은 https://cloud.google.com/dlp/docs/infotypes-reference 에서 확인하세요.
            //for (String typeName : new String[] {"PHONE_NUMBER", "EMAIL_ADDRESS", "IP_ADDRESS", "CREDIT_CARD_NUMBER"}) {
            for (String typeName : typeNameList) {
                infoTypes.add(InfoType.newBuilder().setName(typeName.trim()).build());
            }

            // Inspect 요청에 대한 구성을 만듭니다.
            InspectConfig config =
                    InspectConfig.newBuilder().addAllInfoTypes(infoTypes).setIncludeQuote(true).build();

            // 클라이언트가 보낼 Inspect 요청을 구성합니다.
            InspectContentRequest request =
                    InspectContentRequest.newBuilder()
                            .setParent(LocationName.of(projectId, "global").toString())
                            .setItem(item)
                            .setInspectConfig(config)
                            .build();

            // 클라이언트를 사용하여 API 요청을 보냅니다.
            InspectContentResponse response = dlp.inspectContent(request);

            // 응답을 파싱하고 결과를 처리합니다.
            System.out.println("Findings: " + response.getResult().getFindingsCount());
            for (Finding f : response.getResult().getFindingsList()) {
                System.out.println("\tQuote: " + f.getQuote());
                System.out.println("\tInfo type: " + f.getInfoType().getName());
                System.out.println("\tLikelihood: " + f.getLikelihood());
            }
        }
    }

    @GetMapping(value = "redactImageFile")
    public static void redactImageFile(HttpServletRequest httpServletRequest)
            throws IOException {
        // 요청을 보내기 위해 사용될 클라이언트를 초기화합니다. 이 클라이언트는 한 번만 생성되며,
        // 여러 요청에 대해 재사용될 수 있습니다. 모든 요청을 완료한 후, 클라이언트에 대해 "close" 메소드를 호출하여
        // 남아 있는 백그라운드 리소스를 안전하게 정리합니다.

        String projectId = httpServletRequest.getParameter("projectId");
        String inputPath = httpServletRequest.getParameter("inputPath");
        String outputPath = httpServletRequest.getParameter("outputPath");

        try (DlpServiceClient dlp = DlpServiceClient.create()) {
            // 검사할 내용을 지정합니다.
            ByteString fileBytes = ByteString.readFrom(new FileInputStream(inputPath));
            ByteContentItem byteItem =
                    ByteContentItem.newBuilder().setType(ByteContentItem.BytesType.IMAGE).setData(fileBytes).build();

            // 검게 처리할 정보의 유형과 확률을 지정합니다.
/*            List<InfoType> infoTypes = new ArrayList<>();
            // 전체 정보 유형 목록은 https://cloud.google.com/dlp/docs/infotypes-reference 에서 확인하세요
            for (String typeName : new String[] {"PHONE_NUMBER", "EMAIL_ADDRESS", "CREDIT_CARD_NUMBER"}) {
                infoTypes.add(InfoType.newBuilder().setName(typeName).build());
            }*/
            InspectConfig config =
                    InspectConfig.newBuilder()
                            //.addAllInfoTypes(infoTypes)
                            .setMinLikelihood(Likelihood.LIKELY)
                            .build();

            // 클라이언트가 보낼 Redact 요청을 구성합니다.
            RedactImageRequest request =
                    RedactImageRequest.newBuilder()
                            .setParent(LocationName.of(projectId, "global").toString())
                            .setByteItem(byteItem)
                            .setInspectConfig(config)
                            .build();

            // 클라이언트를 사용하여 API 요청을 보냅니다.
            RedactImageResponse response = dlp.redactImage(request);

            // 응답을 파싱하고 결과를 처리합니다.
            FileOutputStream redacted = new FileOutputStream(outputPath);
            redacted.write(response.getRedactedImage().toByteArray());
            redacted.close();
            System.out.println("Redacted image written to " + outputPath);
        }
    }

    // Creates a custom stored info type that contains GitHub usernames used in commits.
    public static void createStoredInfoType(String projectId, String outputPath)
            throws IOException {
        try (DlpServiceClient dlp = DlpServiceClient.create()) {

            // Optionally set a display name and a description.
            String displayName = "GitHub usernames";
            String description = "Dictionary of GitHub usernames used in commits";

            // The output path where the custom dictionary containing the GitHub usernames will be stored.
            CloudStoragePath cloudStoragePath =
                    CloudStoragePath.newBuilder()
                            .setPath(outputPath)
                            .build();

            // The reference to the table containing the GitHub usernames.
            BigQueryTable table = BigQueryTable.newBuilder()
                    .setProjectId("bigquery-public-data")
                    .setDatasetId("samples")
                    .setTableId("github_nested")
                    .build();

            // The reference to the BigQuery field that contains the GitHub usernames.
            BigQueryField bigQueryField = BigQueryField.newBuilder()
                    .setTable(table)
                    .setField(FieldId.newBuilder().setName("actor").build())
                    .build();

            LargeCustomDictionaryConfig largeCustomDictionaryConfig =
                    LargeCustomDictionaryConfig.newBuilder()
                            .setOutputPath(cloudStoragePath)
                            .setBigQueryField(bigQueryField)
                            .build();

            StoredInfoTypeConfig storedInfoTypeConfig = StoredInfoTypeConfig.newBuilder()
                    .setDisplayName(displayName)
                    .setDescription(description)
                    .setLargeCustomDictionary(largeCustomDictionaryConfig)
                    .build();

            // Combine configurations into a request for the service.
            CreateStoredInfoTypeRequest createStoredInfoType = CreateStoredInfoTypeRequest.newBuilder()
                    .setParent(LocationName.of(projectId, "global").toString())
                    .setConfig(storedInfoTypeConfig)
                    .setStoredInfoTypeId("github-usernames")
                    .build();

            // Send the request and receive response from the service.
            StoredInfoType response = dlp.createStoredInfoType(createStoredInfoType);

            // Print the results.
            System.out.println("Created Stored InfoType: " + response.getName());
        }
    }

}

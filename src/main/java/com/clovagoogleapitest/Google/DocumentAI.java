package com.clovagoogleapitest.Google;

import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DocumentAI {
    @GetMapping(value = "Cloud_Document_AI_API")
    public static void Cloud_Document_AI_API(HttpServletRequest httpServletRequest) throws IOException{

        String projectId = httpServletRequest.getParameter("projectId");
        String location = "us"; // Format is "us" or "eu".
        String processorId = httpServletRequest.getParameter("processorId");
        String filePath = httpServletRequest.getParameter("filePath");
        String fileType = httpServletRequest.getParameter("fileType");

        String endpoint = String.format("%s-documentai.googleapis.com:443", location);
        DocumentProcessorServiceSettings settings =
                DocumentProcessorServiceSettings.newBuilder().setEndpoint(endpoint).build();
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            // 프로세서의 전체 리소스 이름, 예:
            // projects/project-id/locations/location/processor/processor-id
            // 우선 Cloud Console에서 새 프로세서를 생성해야 합니다.
            String name =
                    String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            // 파일을 읽습니다.
            byte[] imageFileData = Files.readAllBytes(Paths.get(filePath));

            // 이미지 데이터를 버퍼로 변환하고 base64로 인코딩합니다.
            ByteString content = ByteString.copyFrom(imageFileData);
            RawDocument document = RawDocument
                    .newBuilder()
                    .setContent(content)
                    .setMimeType("application/pdf")
                    .build();

            if(fileType.equals("jpeg")){
                document = RawDocument
                        .newBuilder()
                        .setContent(content)
                        .setMimeType("image/jpeg")
                        .build();
            }

            // 처리 요청을 구성합니다.
            ProcessRequest request =
                    ProcessRequest.newBuilder().setName(name).setRawDocument(document).build();

            // PDF 문서에서 텍스트 엔티티를 인식합니다.
            ProcessResponse result = client.processDocument(request);
            Document documentResponse = result.getDocument();

            // 문서의 모든 텍스트를 하나의 큰 문자열로 가져옵니다.
            String text = documentResponse.getText();


            for(Document.Page page : documentResponse.getPagesList()){
                System.out.println(page.getPageNumber()+"페이지의 문서에는 다음과 같은 단락이 포함되어 있습니다.");

                List<Document.Page.Paragraph> paragraphs = page.getParagraphsList();

                for (Document.Page.Paragraph paragraph : paragraphs) {
                    String paragraphText = getText(paragraph.getLayout().getTextAnchor(), text);
                    System.out.printf("단락 텍스트:\n%s\n", paragraphText);
                }

                // 양식 파싱은
                // 양식 형식의 PDF에 대한 추가적인 출력을 제공합니다. 전체 필드 세부 정보를 보려면
                // Cloud Console에서 양식 프로세서를 생성해야 합니다.
                System.out.println("다음 양식 키/값 쌍이 감지되었습니다:");

                for (Document.Page.FormField field : page.getFormFieldsList()) {
                    String fieldName = getText(field.getFieldName().getTextAnchor(), text);
                    String fieldValue = getText(field.getFieldValue().getTextAnchor(), text);

                    System.out.println("추출된 양식 필드 쌍:");
                    System.out.printf("\t(%s, %s))\n", fieldName, fieldValue);
                }
            }
        }
    }


    @PostMapping(value = "Expense_Parser")
    public static void Expense_Parser(
            HttpServletRequest httpServletRequest,
            @RequestParam("file") MultipartFile file
    ) throws IOException{

        String filePath = null;
        try {
            FileUploadController fileUploadController = new FileUploadController();
            filePath = fileUploadController.uploadFile(file);
        }catch (Exception e){
            filePath = httpServletRequest.getParameter("filePath");
        }

        String projectId = httpServletRequest.getParameter("projectId");
        String location = "us"; // Format is "us" or "eu".
        String processorId = httpServletRequest.getParameter("processorId");
        String fileType = httpServletRequest.getParameter("fileType"); // jpeg
        String endpoint = String.format("%s-documentai.googleapis.com:443", location);

        DocumentProcessorServiceSettings settings =
                DocumentProcessorServiceSettings.newBuilder().setEndpoint(endpoint).build();
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            // 프로세서의 전체 리소스 이름, 예:
            // projects/project-id/locations/location/processor/processor-id
            // 우선 Cloud Console에서 새 프로세서를 생성해야 합니다.
            String name =
                    String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            // 파일을 읽습니다.
            byte[] imageFileData = Files.readAllBytes(Paths.get(filePath));

            // 이미지 데이터를 버퍼로 변환하고 base64로 인코딩합니다.
            ByteString content = ByteString.copyFrom(imageFileData);
            RawDocument document = RawDocument
                    .newBuilder()
                    .setContent(content)
                    .setMimeType("application/pdf")
                    .build();

            if(fileType.equals("jpeg")){
                document = RawDocument
                        .newBuilder()
                        .setContent(content)
                        .setMimeType("image/jpeg")
                        .build();
            }

            // 처리 요청을 구성합니다.
            ProcessRequest request =
                    ProcessRequest.newBuilder().setName(name).setRawDocument(document).build();

            // PDF 문서에서 텍스트 엔티티를 인식합니다.
            ProcessResponse result = client.processDocument(request);
            Document documentResponse = result.getDocument();

            // 문서의 모든 텍스트를 하나의 큰 문자열로 가져옵니다.
            String text = documentResponse.getText();
            System.out.println("text: " + text);

            for(Document.Page page : documentResponse.getPagesList()){
                for (Document.Page.FormField field : page.getFormFieldsList()) {
                    String fieldName = getText(field.getFieldName().getTextAnchor(), text);
                    String fieldValue = getText(field.getFieldValue().getTextAnchor(), text);

                    System.out.println("fieldName-> " + fieldName);
                    System.out.println("fieldValue-> " + fieldValue);

                    switch (fieldName) {
                        case "receipt_date":
                            System.out.println("영수증 날짜: " + fieldValue);
                            break;
                        case "purchase_time":
                            System.out.println("구매 시간: " + fieldValue);
                            break;
                        case "line_item/description":
                            System.out.println("항목 설명: " + fieldValue);
                            break;
                        case "line_item/amount":
                            System.out.println("항목 금액: " + fieldValue);
                            break;
                        case "total_amount":
                            System.out.println("총액: " + fieldValue);
                            break;
                    }

                }
            }
            client.close();
        }
    }

    // 텍스트 필드에서 조각을 추출합니다
    private static String getText(Document.TextAnchor textAnchor, String text) {
        if (textAnchor.getTextSegmentsList().size() > 0) {
            int startIdx = (int) textAnchor.getTextSegments(0).getStartIndex();
            int endIdx = (int) textAnchor.getTextSegments(0).getEndIndex();
            return text.substring(startIdx, endIdx);
        }
        return "[NO TEXT]";
    }
}

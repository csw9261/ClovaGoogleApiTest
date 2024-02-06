package com.clovagoogleapitest.Clova;

import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@RestController
public class ClovaOcr {

    @GetMapping(value = "GeneralOcrRequest")
    public void GeneralOcrRequest(HttpServletRequest httpServletRequest) {
        String apiURL = httpServletRequest.getParameter("apiURL"); // OCR API URL
        String secretKey = httpServletRequest.getParameter("secretKey"); // API 접근을 위한 시크릿 키
        String imageFile = httpServletRequest.getParameter("imageFile"); // OCR 처리할 이미지 파일 경로

        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // HTTP 연결 설정
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setConnectTimeout(0);
            con.setReadTimeout(0); // 읽기 타임아웃 설정
            con.setRequestMethod("POST"); // HTTP POST 메소드 설정
            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-OCR-SECRET", secretKey);

            // JSON 객체 생성 및 초기화
            JSONObject json = new JSONObject();
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());
            JSONObject image = new JSONObject();
            image.put("format", "jpg");
            image.put("name", "demo");
            JSONArray images = new JSONArray();
            images.put(image);
            json.put("images", images);
            String postParams = json.toString();

            con.connect();
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            File file = new File(imageFile);
            writeMultiPart(wr, postParams, file, boundary); // 멀티파트 데이터 작성
            wr.close();

            // 응답 처리
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 성공 응답
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else { // 에러 응답
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            System.out.println(response);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @GetMapping(value = "BillOcrRequest")
    public void BillOcrRequest(HttpServletRequest httpServletRequest) {
        String apiURL = httpServletRequest.getParameter("apiURL"); // OCR API URL
        String secretKey = httpServletRequest.getParameter("secretKey"); // API 접근을 위한 시크릿 키
        String imageFile = httpServletRequest.getParameter("imageFile"); // OCR 처리할 이미지 파일 경로

        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // HTTP 연결 설정
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setConnectTimeout(0);
            con.setReadTimeout(0); // 읽기 타임아웃 설정
            con.setRequestMethod("POST"); // HTTP POST 메소드 설정
            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-OCR-SECRET", secretKey);

            // JSON 객체 생성 및 초기화
            JSONObject json = new JSONObject();
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());
            JSONObject image = new JSONObject();
            image.put("format", "jpg");
            image.put("name", "demo");
            JSONArray images = new JSONArray();
            images.put(image);
            json.put("images", images);
            String postParams = json.toString();

            con.connect();
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            File file = new File(imageFile);
            writeMultiPart(wr, postParams, file, boundary); // 멀티파트 데이터 작성
            wr.close();

            // 응답 처리
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 성공 응답
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else { // 에러 응답
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            System.out.println(response);
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    private static void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws IOException {
        // 멀티파트 데이터 작성을 위한 메소드
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage);
        sb.append("\r\n");

        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();

        // 파일이 있을 경우 파일 데이터를 스트림에 작성
        if (file != null && file.isFile()) {
            out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
            StringBuilder fileString = new StringBuilder();
            fileString
                    .append("Content-Disposition:form-data; name=\"file\"; filename=");
            fileString.append("\"" + file.getName() + "\"\r\n");
            fileString.append("Content-Type: application/octet-stream\r\n\r\n");
            out.write(fileString.toString().getBytes("UTF-8"));
            out.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
            }

            out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        out.flush();
    }
}

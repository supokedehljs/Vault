package com.example.eaglereader;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class MainActivity extends AppCompatActivity {
    private EditText nasUrlInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loadButton;
    private Spinner folderSpinner;
    private RecyclerView mediaRecyclerView;
    private TextView resultView;

    private String nasBaseUrl = "";
    private String username = "";
    private String password = "";
    private NtlmPasswordAuthentication auth;
    private List<FolderInfo> folders = new ArrayList<>();
    private List<MediaItem> mediaItems = new ArrayList<>();
    private MediaAdapter mediaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        nasUrlInput = findViewById(R.id.nasUrlInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loadButton = findViewById(R.id.loadButton);
        folderSpinner = findViewById(R.id.folderSpinner);
        mediaRecyclerView = findViewById(R.id.mediaRecyclerView);
        resultView = findViewById(R.id.resultView);
    }

    private void setupRecyclerView() {
        mediaAdapter = new MediaAdapter(this, mediaItems);
        mediaRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mediaRecyclerView.setAdapter(mediaAdapter);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        folderSpinner.setAdapter(adapter);

        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    loadMediaForFolder(folders.get(position - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        loadButton.setOnClickListener(v -> {
            String nasUrl = nasUrlInput.getText().toString().trim();
            username = usernameInput.getText().toString().trim();
            password = passwordInput.getText().toString();

            if (nasUrl.isEmpty()) {
                Toast.makeText(this, "请输入NAS地址", Toast.LENGTH_SHORT).show();
                return;
            }

            nasBaseUrl = nasUrl;
            loadLibrary();
        });
    }

    private void loadLibrary() {
        resultView.setText("正在连接NAS...");
        new Thread(this::loadLibraryFromNAS).start();
    }

    private void loadLibraryFromNAS() {
        try {
            Config.setProperty("jcifs.smb.client.responseTimeout", "30000");
            Config.setProperty("jcifs.smb.client.soTimeout", "30000");
            auth = new NtlmPasswordAuthentication(null, username, password);

            String libraryUrl = nasBaseUrl + "/李杰.library/metadata.json";
            String metadataJson = fetchJsonFromUrl(libraryUrl);

            if (metadataJson != null) {
                JSONObject metadata = new JSONObject(metadataJson);
                JSONArray foldersArray = metadata.getJSONArray("folders");

                folders.clear();
                Map<String, FolderInfo> folderMap = new HashMap<>();

                for (int i = 0; i < foldersArray.length(); i++) {
                    JSONObject folderObj = foldersArray.getJSONObject(i);
                    FolderInfo folder = new FolderInfo();
                    folder.id = folderObj.getString("id");
                    folder.name = folderObj.getString("name");
                    folder.icon = folderObj.optString("icon", "folder");
                    folders.add(folder);
                    folderMap.put(folder.id, folder);
                }

                loadAllImagesMetadata();

                runOnUiThread(() -> {
                    updateFolderSpinner();
                    resultView.setText("加载完成！共 " + mediaItems.size() + " 个文件，" + folders.size() + " 个文件夹");
                });
            } else {
                runOnUiThread(() -> resultView.setText("无法获取素材库信息"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> resultView.setText("错误: " + e.getMessage()));
        }
    }

    private void loadAllImagesMetadata() {
        try {
            String imagesUrl = nasBaseUrl + "/李杰.library/images";
            SmbFile imagesDir = new SmbFile(imagesUrl, auth);
            if (imagesDir.exists() && imagesDir.isDirectory()) {
                SmbFile[] items = imagesDir.listFiles();
                for (SmbFile item : items) {
                    if (item.isDirectory() && item.getName().endsWith(".info")) {
                        String metadataUrl = item.getURL() + "/metadata.json";
                        String metadataJson = fetchJsonFromUrl(metadataUrl);
                        if (metadataJson != null) {
                            JSONObject meta = new JSONObject(metadataJson);
                            processImageMetadata(meta);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fetchJsonFromUrl(String urlString) {
        try {
            URL url = new URL(urlString.replace("smb:/", "smb://"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processImageMetadata(JSONObject metadata) {
        MediaItem item = new MediaItem();
        item.id = metadata.optString("id", "");
        item.name = metadata.optString("name", "");
        item.star = metadata.optInt("star", 0);
        item.ext = metadata.optString("ext", "");
        item.width = metadata.optInt("width", 0);
        item.height = metadata.optInt("height", 0);
        item.size = metadata.optLong("size", 0);

        JSONArray foldersArray = metadata.optJSONArray("folders");
        if (foldersArray != null) {
            for (int i = 0; i < foldersArray.length(); i++) {
                item.folderIds.add(foldersArray.getString(i));
            }
        }

        JSONArray tagsArray = metadata.optJSONArray("tags");
        if (tagsArray != null) {
            for (int i = 0; i < tagsArray.length(); i++) {
                item.tags.add(tagsArray.getString(i));
            }
        }

        mediaItems.add(item);
    }

    private void updateFolderSpinner() {
        List<String> folderNames = new ArrayList<>();
        folderNames.add("全部文件");
        for (FolderInfo folder : folders) {
            folderNames.add(folder.icon + " " + folder.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, folderNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        folderSpinner.setAdapter(adapter);
    }

    private void loadMediaForFolder(FolderInfo folder) {
        List<MediaItem> filtered = new ArrayList<>();
        for (MediaItem item : mediaItems) {
            if (item.folderIds.contains(folder.id)) {
                filtered.add(item);
            }
        }
        mediaAdapter.updateData(filtered);
        resultView.setText("文件夹 '" + folder.name + "' 中有 " + filtered.size() + " 个文件");
    }

    static class FolderInfo {
        String id;
        String name;
        String icon;
    }

    static class MediaItem {
        String id;
        String name;
        String ext;
        int star;
        int width;
        int height;
        long size;
        List<String> folderIds = new ArrayList<>();
        List<String> tags = new ArrayList<>();
    }
}
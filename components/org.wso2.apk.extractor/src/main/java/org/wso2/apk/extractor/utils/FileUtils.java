package org.wso2.apk.extractor.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.wso2.apk.extractor.datatypes.APIDataType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

public class FileUtils {
    public static void saveExtractedData(LinkedHashMap<String, Object> extractedData) throws IOException {
        LinkedHashMap<String, Object> objects = (LinkedHashMap<String, Object>) extractedData.get("getAPIs");
        ObjectMapper mapper = new ObjectMapper();
        List<APIDataType> apiList = mapper.convertValue(objects.get("list"), new TypeReference<List<APIDataType>>() {
        });
        Gson gson = new Gson();
        Path extracted = Paths.get("Extracted");
        if (!Files.exists(extracted)) {
            Files.createDirectory(extracted);
        }
        for (APIDataType api : apiList) {
            String tenantDirectory = "Extracted/" + api.getOrganization();
            Path directory = Paths.get(tenantDirectory);
            if (!Files.exists(directory)) {
                Files.createDirectory(directory);
            }
            String fileName = String.format("%s/%s_%s.json", tenantDirectory, api.getName(), api.getVersion());
            String jsonString = gson.toJson(api);
            Files.write(Paths.get(fileName), jsonString.getBytes());

        }
    }
}

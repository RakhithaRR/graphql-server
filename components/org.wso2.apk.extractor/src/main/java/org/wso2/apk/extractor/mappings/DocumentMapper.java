package org.wso2.apk.extractor.mappings;

import org.apache.commons.io.IOUtils;
import org.wso2.apk.extractor.models.DocumentDTO;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class DocumentMapper {
    private final String organization;
    private final String adminUsername;
    private final APIProvider apiProvider;

    public DocumentMapper(APIProvider apiProvider, String adminUsername, String organization) {
        this.apiProvider = apiProvider;
        this.organization = organization;
        this.adminUsername = adminUsername;
    }

    public List<DocumentDTO> getDocumentationDetails(API api) {
        List<DocumentDTO> documentDTOList = new ArrayList<>();
        try {
            List<Documentation> documentationList = apiProvider.getAllDocumentation(api.getId(), organization);

            for (Documentation documentation : documentationList) {
                DocumentDTO documentDTO = new DocumentDTO();
                documentDTO.setDocumentId(documentation.getId());
                documentDTO.setName(documentation.getName());
                documentDTO.setSummary(documentation.getSummary());
                documentDTO.setType(documentation.getType().toString());
                documentDTO.setSourceType(documentation.getSourceType().toString());
                documentDTO.setOtherTypeName(documentation.getOtherTypeName());
                documentDTO.setSourceUrl(documentation.getSourceUrl());
                documentDTO.setVisibility(documentation.getVisibility().toString());

//                DocumentationContent documentationContent = apiProvider
//                        .getDocumentationContent(apiId, documentation.getId(), organization);
                String content = apiProvider.getDocumentationContent(api.getId(), documentation.getName());
                if (Documentation.DocumentSourceType.INLINE.equals(documentation.getSourceType())
                        || Documentation.DocumentSourceType.MARKDOWN.equals(documentation.getSourceType())) {
                    if (content != null) {
                        documentDTO.setInlineContent(content);
                    }
                }
                Map<String, Object> docResourceMap = APIUtil.getDocument(adminUsername, documentation.getFilePath(),
                        organization);
                if (Documentation.DocumentSourceType.FILE.equals(documentation.getSourceType())
                        && !docResourceMap.isEmpty()) {
                    InputStream contentStream = (InputStream) docResourceMap.get("Data");
                    documentDTO.setFileName(getBase64EncodedDocument(contentStream));
                }
                documentDTOList.add(documentDTO);
            }
        } catch (APIManagementException e) {
            return documentDTOList;
            // todo: handle exception
        }
        return documentDTOList;
    }

    private String getBase64EncodedDocument(InputStream contentStream) {
//        InputStream contentStream = documentationContent.getResourceFile().getContent();
        String base64EncodedDocument = "";
        if (contentStream != null) {
            try {
                byte[] bytes = IOUtils.toByteArray(contentStream);
                base64EncodedDocument = Base64.getEncoder().encodeToString(bytes);
                return base64EncodedDocument;
            } catch (IOException e) {
                return "";
            }
        }
        return base64EncodedDocument;
    }
}

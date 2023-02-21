package org.wso2.apk.graphql.api.mappings;

import org.apache.commons.io.IOUtils;
import org.wso2.apk.graphql.api.models.DocumentDTO;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DocumentMapper {
    private final String organization;
    private final APIProvider apiProvider;

    public DocumentMapper(APIProvider apiProvider, String organization) {
        this.apiProvider = apiProvider;
        this.organization = organization;
    }

    public List<DocumentDTO> getDocumentationDetails(String apiId) {
        List<DocumentDTO> documentDTOList = new ArrayList<>();
        try {
            List<Documentation> documentationList = apiProvider.getAllDocumentation(apiId, organization);

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

                DocumentationContent documentationContent = apiProvider
                        .getDocumentationContent(apiId, documentation.getId(), organization);
                if (Documentation.DocumentSourceType.INLINE.equals(documentation.getSourceType())
                        || Documentation.DocumentSourceType.MARKDOWN.equals(documentation.getSourceType())) {
                    if (documentationContent != null) {
                        documentDTO.setInlineContent(documentationContent.getTextContent());
                    }
                }
                if (Documentation.DocumentSourceType.FILE.equals(documentation.getSourceType())
                        && documentationContent != null) {
                    documentDTO.setFileName(getBase64EncodedDocument(documentationContent));
                }
                documentDTOList.add(documentDTO);
            }
        } catch (APIManagementException e) {
            return documentDTOList;
            // todo: handle exception
        }
        return documentDTOList;
    }

    private String getBase64EncodedDocument(DocumentationContent documentationContent) {
        InputStream contentStream = documentationContent.getResourceFile().getContent();
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

package org.wso2.apk.extractor.utils;

import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonUtils {

    public static List<Tenant> loadTenants() {
        List<Tenant> tenantsArray = new ArrayList<>();
        TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();

        try {
            tenantsArray = new ArrayList<>(Arrays.asList(tenantManager.getAllTenants()));
            Tenant superTenant = new Tenant();
            superTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            superTenant.setId(MultitenantConstants.SUPER_TENANT_ID);
            tenantsArray.add(superTenant);

            for (int i = 0; i < tenantsArray.size(); ++i) {
                Tenant tenant = tenantsArray.get(i);
                if (tenant.getId() == MultitenantConstants.SUPER_TENANT_ID) {
                    String tenantDomain = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                            getSuperTenantDomain();
                    String adminName = getTenantAdminUserName(tenantDomain);
                    tenant.setAdminName(adminName);
                } else {
                    tenantsArray.set(i, tenantManager.getTenant(tenant.getId()));
                    tenantsArray.get(i).setAdminName(getTenantAdminUserName(tenant.getDomain()));
                }
            }
        } catch (UserStoreException e) {
            return tenantsArray;
        }

        return tenantsArray;
    }

    private static String getTenantAdminUserName(String tenantDomain) throws UserStoreException {
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            String adminUserName = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminUserName();
            if (!tenantDomain.contentEquals(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                return adminUserName.concat("@").concat(tenantDomain);
            }
            return adminUserName;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}

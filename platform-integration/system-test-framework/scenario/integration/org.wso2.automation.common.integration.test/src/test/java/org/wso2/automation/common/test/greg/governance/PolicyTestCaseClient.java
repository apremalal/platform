/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.automation.common.test.greg.governance;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyFilter;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class PolicyTestCaseClient {

    private Registry governance;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registryWS, userId);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddPolicy() throws Exception {
        PolicyManager policyManager = new PolicyManager(governance);

        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/trunk/graphite/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml");
        policy.addAttribute("creator", "it is me");
        policy.addAttribute("version", "0.01");
        policyManager.addPolicy(policy);

        Policy newPolicy = policyManager.getPolicy(policy.getId());
        Assert.assertEquals(newPolicy.getPolicyContent(), policy.getPolicyContent());
        Assert.assertEquals(newPolicy.getAttribute("creator"), "it is me");
        Assert.assertEquals(newPolicy.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldPolicyPath = newPolicy.getPath();
        Assert.assertEquals(oldPolicyPath, "/trunk/policies/policy.xml");
        Assert.assertTrue(governance.resourceExists("/trunk/policies/policy.xml"));

        newPolicy.setName("my-policy.xml");
        policyManager.updatePolicy(newPolicy);

        Assert.assertEquals(newPolicy.getPath(), "/trunk/policies/my-policy.xml");
        Assert.assertFalse(governance.resourceExists("/trunk/policies/policy.xml"));

        // doing an update without changing anything.
        policyManager.updatePolicy(newPolicy);

        Assert.assertEquals(newPolicy.getPath(), "/trunk/policies/my-policy.xml");
        Assert.assertEquals(newPolicy.getAttribute("version"), "0.01");

        newPolicy = policyManager.getPolicy(policy.getId());
        Assert.assertEquals(newPolicy.getAttribute("creator"), "it is me");
        Assert.assertEquals(newPolicy.getAttribute("version"), "0.01");

        Policy[] policies = policyManager.findPolicies(new PolicyFilter() {
            public boolean matches(Policy policy) throws GovernanceException {
                if (policy.getAttribute("version").equals("0.01")) {
                    return true;
                }
                return false;
            }
        });
        Assert.assertEquals(policies.length, 1);
        Assert.assertEquals(newPolicy.getId(), policies[0].getId());

        // deleting the policy
        policyManager.removePolicy(newPolicy.getId());
        Policy deletedPolicy = policyManager.getPolicy(newPolicy.getId());
        Assert.assertNull(deletedPolicy);
    }

    @Test(groups = {"wso2.greg"}, description = "Add 1000 policies")
    public void testAddLargeNumberOfPolicies() throws GovernanceException {
        int policyCount = 20;
        PolicyManager policyManager = new PolicyManager(governance);

        Policy policy;
        int policyCountBeforeTest = policyManager.getAllPolicies().length;

        for (int i = 1; i <= policyCount; i++) {
            policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/" +
                                             "trunk/platform-integration/system-test-framework" +
                                             "/core/org.wso2.automation.platform.core/src/main/" +
                                             "resources/artifacts/GREG/policy/UTPolicy.xml");
            policy.setName("WSO2AutomationUTPolicy" + i + ".xml");
            policyManager.addPolicy(policy);
        }
        Policy[] policies = policyManager.getAllPolicies();
        int policyCountAfterTest = policies.length;
        assertTrue(((policyCountAfterTest - policyCountBeforeTest) == policyCount),
                   "All " + policyCount + "policies were not added");

        //delete policies
        for (Policy policyEntry : policies) {
            if (policyEntry.getQName().toString().contains("WSO2AutomationUTPolicy")) {
                policyManager.removePolicy(policyEntry.getId());
                assertNull(policyManager.getPolicy(policyEntry.getId()));
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Adding duplicate policy")
    public void testAddDuplicatePolicy() throws GovernanceException {
        PolicyManager policyManager = new PolicyManager(governance);

        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/" +
                                                "trunk/platform-integration/system-test-framework" +
                                                "/core/org.wso2.automation.platform.core/src/main/" +
                                                "resources/artifacts/GREG/policy/UTPolicy.xml");
        policy.setName("WSO2AutomationUTPolicyDuplicate.xml");
        policyManager.addPolicy(policy);
        assertTrue(policy.getQName().toString().contains("WSO2AutomationUTPolicyDuplicate.xml"));

        Policy policyDuplicate = policyManager.newPolicy("http://svn.wso2" +
                                                         ".org/repos/wso2/carbon/platform/" +
                                                         "trunk/platform-integration/system-test-framework" +
                                                         "/core/org.wso2.automation.platform.core/src/main/" +
                                                         "resources/artifacts/GREG/policy/UTPolicy.xml");
        policyDuplicate.setName("WSO2AutomationUTPolicyDuplicate.xml");
        policyManager.addPolicy(policyDuplicate);

        assertTrue(policyDuplicate.getQName().toString().contains("WSO2AutomationUTPolicyDuplicate.xml"));
        policyManager.removePolicy(policy.getId());
        assertNull(policyManager.getPolicy(policy.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "Adding invalid policy")
    public void testAddInvalidPolicy() throws GovernanceException {
        PolicyManager policyManager = new PolicyManager(governance);

        Policy policy = policyManager.newPolicy("https://svn.wso2.org/repos/wso2/carbon/platform/" +
                                                "trunk/platform-integration/system-test-framework/" +
                                                "core/org.wso2.automation.platform.core/src/main/" +
                                                "resources/artifacts/GREG/policy/invlidPolicySyntax.xml");

        policy.setName("invalidPolicy.xml");
        policyManager.addPolicy(policy);
        assertTrue(policy.getQName().toString().contains("invalidPolicy.xml"));
        policyManager.removePolicy(policy.getId());
        assertNull(policyManager.getPolicy(policy.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "Update policy content")
    public void testUpdatePolicy() throws GovernanceException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String updatedPolicyPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                                   + "artifacts" + File.separator + "GREG" + File.separator +
                                   "policy" + File.separator + "UTPolicy-updated.xml";

        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/" +
                                                "trunk/platform-integration/system-test-framework" +
                                                "/core/org.wso2.automation.platform.core/src/main/" +
                                                "resources/artifacts/GREG/policy/UTPolicy.xml");

        policy.setName("NewPolicyAutomation.xml");
        policyManager.addPolicy(policy);
        assertTrue(policy.getQName().toString().contains("NewPolicyAutomation.xml"));


        File newPolicyFile = new File(updatedPolicyPath);
        StringBuilder strContent = new StringBuilder("");

        if (newPolicyFile.exists()) {
            FileInputStream inputStream = new FileInputStream(newPolicyFile);

            int character;
            while ((character = inputStream.read()) != -1) {
                strContent.append((char) character);
            }
            inputStream.close();
        }
        policy.setPolicyContent(strContent.toString());
        policyManager.updatePolicy(policy);

        assertTrue(policy.getQName().toString().contains("NewPolicyAutomation.xml"));
        assertTrue(policyManager.getPolicy(policy.getId()).getPolicyContent().contains("PolicyUpdatedForAutomationTest"),
                   "Updated content not found");

        policyManager.removePolicy(policy.getId());
        assertNull(policyManager.getPolicy(policy.getId()));
    }
}

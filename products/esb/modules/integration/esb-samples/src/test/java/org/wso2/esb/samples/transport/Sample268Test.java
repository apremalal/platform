package org.wso2.esb.samples.transport;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertTrue;

public class Sample268Test extends ESBIntegrationTestCase {

    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }


    @Test(groups = {"wso2.esb"}, description = "Sample 268: Proxy services with the Local transport")
    public void testLocalTransportProxyService() throws Exception {
        loadSampleESBConfiguration(268);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("LocalTransportProxy", false),
                null, "WSO2");

        assertTrue(response.toString().contains("WSO2"));
        log.info("Response : " + response.toString());
    }
}

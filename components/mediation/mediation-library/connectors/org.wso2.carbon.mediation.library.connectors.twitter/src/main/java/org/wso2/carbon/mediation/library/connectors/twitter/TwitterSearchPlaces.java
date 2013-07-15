/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediation.library.connectors.twitter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import twitter4j.GeoLocation;
import twitter4j.GeoQuery;
import twitter4j.Place;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class TwitterSearchPlaces extends AbstractTwitterConnector{

	public static final String SEARCH_BY_LATITUDE = "latitude";
	
	public static final String SEARCH_LONGITUDE = "longitude";
	
	public static final String SEARCH_IP = "ip";
	
	
	@Override
	public void connect() throws ConnectException {
		// TODO Auto-generated method stub
		if(log.isDebugEnabled()){
			log.info("executing twitter search places");
		}
		MessageContext messageContext = getMessageContext();
		try {
			String latitude = TwitterMediatorUtils.lookupFunctionParam(messageContext, SEARCH_BY_LATITUDE);
			String longitude = TwitterMediatorUtils.lookupFunctionParam(messageContext, SEARCH_LONGITUDE);
			String ip = TwitterMediatorUtils.lookupFunctionParam(messageContext, SEARCH_IP);
			GeoQuery query = new GeoQuery(new GeoLocation(Double.parseDouble(latitude), Double.parseDouble(longitude)));
			Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
			OMElement element = this.performSearch(twitter, query);
			SOAPBody soapBody = messageContext.getEnvelope().getBody();
			if(log.isDebugEnabled()){
				log.info("executing prparing soap envolope"+element.toString());
			}
			for (Iterator itr = soapBody.getChildElements(); itr.hasNext();) {
				OMElement child = (OMElement) itr.next();
				child.detach();
			}
			for (Iterator itr = element.getChildElements(); itr.hasNext();) {
				OMElement child = (OMElement) itr.next();
				soapBody.addChild(child);
			}
		} catch (TwitterException te) {
			log.error("Failed to search twitter : " + te.getMessage(), te);
			TwitterMediatorUtils.storeErrorResponseStatus(messageContext, te);
		} catch (Exception te) {
			log.error("Failed to search generic: " + te.getMessage(), te);
			TwitterMediatorUtils.storeErrorResponseStatus(messageContext, te);
		}
	}

	/**
	 * Performing the searching operation for the given Geo Query criteria.
	 * 
	 * @param twitter
	 * @param query
	 * @return
	 * @throws XMLStreamException
	 * @throws TwitterException
	 * @throws JSONException
	 * @throws IOException
	 */
	private OMElement performSearch(Twitter twitter, GeoQuery query) throws XMLStreamException, TwitterException, JSONException, IOException {
		OMElement resultElement = AXIOMUtil.stringToOM("<XMLPayload/>");
		List<Place> results = twitter.searchPlaces(query);
		if(log.isDebugEnabled()){
			log.info("executing executing search"+query);
		}
		for (Place place : results) {
		    StringBuilder stringBuilder = new StringBuilder();
		    stringBuilder.append("{ \"Place\" : ");
			String json = DataObjectFactory.getRawJSON(place);
			stringBuilder.append(json);
			stringBuilder.append("} ");
			//System.out.println(stringBuilder.toString());
			OMElement element =super.parseJsonToXml(stringBuilder.toString());
			resultElement.addChild(element);
		}
		return resultElement;

	}
	
		
}

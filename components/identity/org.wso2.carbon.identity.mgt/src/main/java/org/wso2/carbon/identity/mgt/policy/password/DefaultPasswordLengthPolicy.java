/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.mgt.policy.password;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.policy.AbstractPasswordPolicyEnforcer;

/**
 * This class is used to enforce the password length policy. This checks minimum and
 * maximum lengths of the password.
 * 
 */
public class DefaultPasswordLengthPolicy extends AbstractPasswordPolicyEnforcer{

	private static final Log log = LogFactory.getLog(DefaultPasswordLengthPolicy.class);
	
	private int MIN_LENGTH = 6;
	private int MAX_LENGTH = 10;

	/**
	 * Required initializations to get the configuration values from file.
	 */
	@Override
	public void init(){
		
		// Initialize the configuration from file.
		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		MIN_LENGTH = config.getPasswordLengthMin();
		MAX_LENGTH = config.getPasswordLengthMax();
	}
	
	/**
	 * Policy enforcing method.
	 * 
	 * @param - the first parameter assumed to be the password. The order of the parameters 
	 * are implementation dependent.
	 */
	@Override
	public boolean enforce(Object... args) {

		// If null input pass through.
		if (args != null) {
			
			String password = args[0].toString();
			if (password.length() < MIN_LENGTH) {
				errorMessage = "Password at least should be have " + MIN_LENGTH + " characters";
				return false;
			} else if (password.length() > MAX_LENGTH) {
				errorMessage = "Password cannot have more than " + MAX_LENGTH + " characters";
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	


}

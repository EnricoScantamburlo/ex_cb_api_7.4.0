/*
 * Copyright by Intland Software
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Intland Software. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Intland.
 */
package com.intland.codebeamer.remoting.sample;

import com.intland.codebeamer.remoting.RemoteApi;
import com.intland.codebeamer.remoting.RemoteApiFactory;


/**
 * Utility methods for remoting.
 *
 * @author <a href="mailto:aron.gombas@intland.com">Aron Gombas</a>
 * @version $Id$
 * @deprecated since 4.3 use {@link RemoteApiFactory} instead
 */
public class RemotingUtils {
	/**
	 * Connects to the passed web service URL.
	 * Supports both plain HTTP and HTTPS protocols. In case of HTTPS,
	 * it completely bypasses the certificate verification.
	 * @return the CodeBeamer Remote API associated with the URL or <code>null</code> if couldn't connect.
	 */
	public static RemoteApi connect(String serviceUrl) {
		RemoteApi api = null;
		try {
			api = RemoteApiFactory.getInstance().connect(serviceUrl);
		} catch(Exception ex) {
			System.err.println(ex);
		}
		return api;
	}

	/**
	 * This method is now obsolete and simply verifies that the api is not null
	 * @param api to check
	 * @return the api
	 */
	public static RemoteApi createExceptionHandler(RemoteApi api) {
		if (api == null) {
			throw new IllegalArgumentException("API object cannot be null");
		}
		return api;
	}
}

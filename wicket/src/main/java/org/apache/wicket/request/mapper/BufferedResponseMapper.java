/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.request.mapper;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.BufferedResponseRequestHandler;

/**
 * Encoder that intercepts requests for which there is already stored buffer with rendered data.
 * 
 * @author Matej Knopp
 */
public class BufferedResponseMapper implements IRequestMapper
{
	/**
	 * Construct.
	 */
	public BufferedResponseMapper()
	{
	}

	/**
	 * 
	 * @return session id
	 */
	protected String getSessionId()
	{
		return Session.get().getId();
	}


	protected boolean hasBufferedResponse(Url url)
	{
		String sessionId = getSessionId();
		if (sessionId != null)
		{
			return WebApplication.get().hasBufferedResponse(sessionId, url);
		}
		else
		{
			return false;
		}
	}

	protected BufferedWebResponse getAndRemoveBufferedResponse(Url url)
	{
		return WebApplication.get().getAndRemoveBufferedResponse(getSessionId(), url);
	}

	private Request getRequest(Request original)
	{
		// The buffers are stored under "real" URL which can be different
		// than the URL handlers get due to global URL pre/postprocessing
		// (i.e. prepending URL with language segment).
		// Because of that we need find out the real URL from request cycle

		if (RequestCycle.get() != null)
		{
			return RequestCycle.get().getRequest();
		}
		else
		{
			return original;
		}
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	public IRequestHandler mapRequest(Request request)
	{
		request = getRequest(request);

		BufferedWebResponse response = getAndRemoveBufferedResponse(request.getUrl());
		if (response != null)
		{
			return new BufferedResponseRequestHandler(response);
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.org.apache.wicket.request.IRequestHandler)
	 */
	public Url mapHandler(IRequestHandler requestHandler)
	{
		return null;
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	public int getCompatibilityScore(Request request)
	{
		request = getRequest(request);

		if (hasBufferedResponse(request.getUrl()))
		{
			return Integer.MAX_VALUE;
		}
		else
		{
			return 0;
		}
	}
}

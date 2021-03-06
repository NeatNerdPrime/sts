/*
 * eID Security Token Service Project.
 * Copyright (C) 2015 e-Contract.be BVBA.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */
package be.e_contract.sts.client.cxf;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.ws.security.saml.ext.SAMLCallback;
import org.w3c.dom.Element;

public class SAMLCallbackHandler implements CallbackHandler {

	private final Element assertionElement;

	public SAMLCallbackHandler(Element assertionElement) {
		this.assertionElement = assertionElement;
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (Callback callback : callbacks) {
			if (callback instanceof SAMLCallback) {
				SAMLCallback samlCallback = (SAMLCallback) callback;
				samlCallback.setAssertionElement(this.assertionElement);
			}
		}
	}
}

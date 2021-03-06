/*
 * eID Security Token Service Project.
 * Copyright (C) 2014 e-Contract.be BVBA.
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

package be.e_contract.sts.client;

import javax.xml.ws.BindingProvider;

import be.e_contract.sts.ws.SecurityTokenServiceFactory;
import be.e_contract.sts.ws.jaxws.SecurityTokenService;
import be.e_contract.sts.ws.jaxws.SecurityTokenServicePort;

public class STSClient {

	private final SecurityTokenServicePort stsPort;

	public STSClient(String address) {
		SecurityTokenService securityTokenService = SecurityTokenServiceFactory
				.newInstance();
		this.stsPort = securityTokenService.getSecurityTokenServicePort();

		BindingProvider bindingProvider = (BindingProvider) this.stsPort;
		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
		// TODO: implement me
	}
}

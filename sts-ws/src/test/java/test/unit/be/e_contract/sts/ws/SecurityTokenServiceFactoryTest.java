/*
 * eID Security Token Service Project.
 * Copyright (C) 2014-2020 e-Contract.be BV.
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

package test.unit.be.e_contract.sts.ws;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import be.e_contract.sts.ws.SecurityTokenServiceFactory;
import be.e_contract.sts.ws.jaxws.SecurityTokenService;

public class SecurityTokenServiceFactoryTest {

	@Test
	public void testNewInstance() throws Exception {
		SecurityTokenService securityTokenService = SecurityTokenServiceFactory.newInstance();
		assertNotNull(securityTokenService);
	}
}

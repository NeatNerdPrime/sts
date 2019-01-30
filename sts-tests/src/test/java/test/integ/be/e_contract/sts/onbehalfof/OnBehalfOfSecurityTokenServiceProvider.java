/*
 * eID Security Token Service Project.
 * Copyright (C) 2014-2019 e-Contract.be BVBA.
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
package test.integ.be.e_contract.sts.onbehalfof;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.sts.STSPropertiesMBean;
import org.apache.cxf.sts.SignatureProperties;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.claims.ClaimsAttributeStatementProvider;
import org.apache.cxf.sts.operation.TokenIssueOperation;
import org.apache.cxf.sts.service.ServiceMBean;
import org.apache.cxf.sts.token.delegation.SAMLDelegationHandler;
import org.apache.cxf.sts.token.provider.AttributeStatementProvider;
import org.apache.cxf.sts.token.provider.DefaultConditionsProvider;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.provider.TokenProvider;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.apache.ws.security.WSConstants;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.joda.time.DateTime;

import test.integ.be.e_contract.sts.ServerCallbackHandler;
import test.integ.be.e_contract.sts.saml.SAMLServiceMBean;
import test.integ.be.e_contract.sts.saml.SAMLSubjectProvider;

@WebService(targetNamespace = "http://docs.oasis-open.org/ws-sx/ws-trust/200512", serviceName = "SecurityTokenService", wsdlLocation = "ws-trust-1.4.wsdl", portName = "SecurityTokenServicePort")
@HandlerChain(file = "/example-ws-handlers.xml")
@EndpointProperties({ @EndpointProperty(key = SecurityConstants.SIGNATURE_PROPERTIES, value = "signature.properties") })
public class OnBehalfOfSecurityTokenServiceProvider extends SecurityTokenServiceProvider {

	private static final X509Certificate SAML_SIGNER_CERTIFICATE;

	private static final PrivateKey SAML_SIGNER_PRIVATE_KEY;

	static {
		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		SAML_SIGNER_PRIVATE_KEY = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();
		try {
			SAML_SIGNER_CERTIFICATE = getCertificate(SAML_SIGNER_PRIVATE_KEY, publicKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public OnBehalfOfSecurityTokenServiceProvider() throws Exception {
		super();
		TokenIssueOperation issueOperation = new TokenIssueOperation();

		STSPropertiesMBean stsProperties = new StaticSTSProperties();
		stsProperties.setCallbackHandler(new ServerCallbackHandler()); // SAMLTokenProvider

		List<X509Certificate> certificates = new LinkedList<>();
		certificates.add(SAML_SIGNER_CERTIFICATE);
		stsProperties.setSignatureCrypto(new OnBehalfOfCrypto(SAML_SIGNER_PRIVATE_KEY, certificates));
		issueOperation.setStsProperties(stsProperties);
		stsProperties.setIssuer("https://issuer");
		SignatureProperties signatureProperties = stsProperties.getSignatureProperties();
		signatureProperties.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
		signatureProperties.setDigestAlgorithm(WSConstants.SHA256);

		List<ServiceMBean> services = new LinkedList<>();
		SAMLServiceMBean service = new SAMLServiceMBean();
		services.add(service);
		issueOperation.setServices(services);

		List<TokenProvider> tokenProviders = new LinkedList<>();
		SAMLTokenProvider samlTokenProvider = new SAMLTokenProvider();
		samlTokenProvider.setSubjectProvider(new SAMLSubjectProvider());

		List<AttributeStatementProvider> attributeStatementProviders = new LinkedList<>();
		attributeStatementProviders.add(new ClaimsAttributeStatementProvider());
		samlTokenProvider.setAttributeStatementProviders(attributeStatementProviders);

		DefaultConditionsProvider defaultConditionsProvider = new DefaultConditionsProvider();
		defaultConditionsProvider.setLifetime(60 * 60 * 3);
		samlTokenProvider.setConditionsProvider(defaultConditionsProvider);

		tokenProviders.add(samlTokenProvider);
		issueOperation.setTokenProviders(tokenProviders);

		issueOperation.getTokenValidators().add(new SAMLTokenValidator());
		issueOperation.getDelegationHandlers().add(new SAMLDelegationHandler());

		setIssueOperation(issueOperation);
	}

	private static X509Certificate getCertificate(PrivateKey privateKey, PublicKey publicKey) throws Exception {
		X500Name subjectName = new X500Name("CN=SAML STS Signer");
		X500Name issuerName = subjectName; // self-signed
		BigInteger serial = new BigInteger(128, new SecureRandom());
		SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusMonths(1);
		X509v3CertificateBuilder x509v3CertificateBuilder = new X509v3CertificateBuilder(issuerName, serial,
				notBefore.toDate(), notAfter.toDate(), subjectName, publicKeyInfo);
		AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
		AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
		AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(privateKey.getEncoded());

		ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(asymmetricKeyParameter);
		X509CertificateHolder x509CertificateHolder = x509v3CertificateBuilder.build(contentSigner);

		byte[] encodedCertificate = x509CertificateHolder.getEncoded();

		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) certificateFactory
				.generateCertificate(new ByteArrayInputStream(encodedCertificate));
		return certificate;
	}

	public static X509Certificate getSAMLSignerCertificate() {
		return SAML_SIGNER_CERTIFICATE;
	}
}

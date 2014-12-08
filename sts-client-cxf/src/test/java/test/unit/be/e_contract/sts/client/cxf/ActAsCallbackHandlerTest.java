package test.unit.be.e_contract.sts.client.cxf;

import java.io.StringWriter;

import javax.security.auth.callback.Callback;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.ws.security.trust.delegation.DelegationCallback;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import be.e_contract.sts.client.cxf.ActAsCallbackHandler;
import be.e_contract.sts.client.cxf.SecurityDecorator;

public class ActAsCallbackHandlerTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ActAsCallbackHandlerTest.class);

	@Test
	public void testInstance() throws Exception {
		// setup
		String officeKey = "example-office-key";
		String softwareKey = "example-software-key";
		byte[] identity = "identity".getBytes();
		SecurityDecorator securityDecorator = new SecurityDecorator();
		securityDecorator.setOfficeKey(officeKey);
		securityDecorator.setSoftwareKey(softwareKey);
		securityDecorator.setIdentity(identity);
		ActAsCallbackHandler callbackHandler = new ActAsCallbackHandler(
				securityDecorator);

		DelegationCallback callback = new DelegationCallback();

		// operate
		callbackHandler.handle(new Callback[] { callback });

		// verify
		Element token = callback.getToken();
		LOGGER.debug("token: {}", toString(token));
	}

	private static String toString(Node node) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		StringWriter stringWriter = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(
				stringWriter));
		return stringWriter.toString();
	}
}
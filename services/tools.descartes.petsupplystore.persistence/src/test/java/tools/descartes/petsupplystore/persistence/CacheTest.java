/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.descartes.petsupplystore.persistence;

import javax.servlet.ServletException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tools.descartes.petsupplystore.entities.Category;
import tools.descartes.petsupplystore.entities.Product;
import tools.descartes.petsupplystore.persistence.domain.CategoryRepository;
import tools.descartes.petsupplystore.persistence.rest.CacheManagerEndpoint;
import tools.descartes.petsupplystore.persistence.rest.DatabaseGenerationEndpoint;
import tools.descartes.petsupplystore.persistence.rest.ProductEndpoint;
import tools.descartes.petsupplystore.registry.rest.Registry;
import tools.descartes.petsupplystore.registry.rest.RegistryREST;
import tools.descartes.petsupplystore.registry.rest.RegistryStartup;
import tools.descartes.petsupplystore.registryclient.Service;
import tools.descartes.petsupplystore.rest.NonBalancedCRUDOperations;
import tools.descartes.petsupplystore.rest.RESTClient;

/**
 * Test for the all federated cache utilities.
 * Also tests database generation endpoints.
 * @author Joakim von Kistowski
 *
 */
public class CacheTest {

private static final String CONTEXT = "/test";
	
	private static int testport = 43001;

	private Tomcat registryTomcat;
	private String testWorkingDir = System.getProperty("java.io.tmpdir");
	
	/**
	 * Setup the test by deploying an embedded tomcat and adding the rest endpoints.
	 * @throws Throwable Throws uncaught throwables for test to fail.
	 */
	@Before
	public void setup() throws Throwable {
		registryTomcat = new Tomcat();
		registryTomcat.setPort(0);
		registryTomcat.setBaseDir(testWorkingDir);
		Context context = registryTomcat.addWebapp(CONTEXT, testWorkingDir);
		context.addApplicationListener(RegistryStartup.class.getName());
		ResourceConfig restServletConfig = new ResourceConfig();
		restServletConfig.register(RegistryREST.class);
		restServletConfig.register(Registry.class);
		ServletContainer restServlet = new ServletContainer(restServletConfig);
		registryTomcat.addServlet(CONTEXT, "restServlet", restServlet);
		context.addServletMappingDecoded("/rest/*", "restServlet");
		registryTomcat.start();
		System.out.println("Initializing Database with size " + CategoryRepository.REPOSITORY.getAllEntities().size());
	}
	
	private Tomcat createClientTomcat(Service service, Tomcat tomcat) throws ServletException, LifecycleException {
		int clientPort = getNextClientPort();
		tomcat.getEngine().setName("Catalina" + clientPort);
		tomcat.setPort(clientPort);
		tomcat.setBaseDir(testWorkingDir);
		tomcat.enableNaming();
		Context context = tomcat.addWebapp("/" + service.getServiceName(), testWorkingDir);
		ContextEnvironment registryURL = new ContextEnvironment();
		registryURL.setDescription("");
		registryURL.setOverride(false);
		registryURL.setType("java.lang.String");
		registryURL.setName("registryURL");
		registryURL.setValue("http://localhost:" + getRegistryPort() + "/test/rest/services/");
		context.getNamingResources().addEnvironment(registryURL);
		ContextEnvironment servicePort = new ContextEnvironment();
		servicePort.setDescription("");
		servicePort.setOverride(false);
		servicePort.setType("java.lang.String");
	    servicePort.setName("servicePort");
	    servicePort.setValue("" + clientPort);
		context.getNamingResources().addEnvironment(servicePort);	
		ResourceConfig restServletConfig = new ResourceConfig();
		restServletConfig.register(CacheManagerEndpoint.class);
		restServletConfig.register(DatabaseGenerationEndpoint.class);
		restServletConfig.register(ProductEndpoint.class);
		ServletContainer restServlet = new ServletContainer(restServletConfig);
		tomcat.addServlet("/" + service.getServiceName(), "restServlet", restServlet);
		context.addServletMappingDecoded("/rest/*", "restServlet");
		context.addApplicationListener(TestRegistryClientStartup.class.getName());
		tomcat.start();
		return tomcat;
	}
	
	/**
	 * Run the test.
	 * @throws Throwable on failure.
	 */
	@Test
	public void testEndpoint() throws Throwable {
		Tomcat client1 = createClientTomcat(Service.PERSISTENCE, new Tomcat());
		Tomcat client2 = createClientTomcat(Service.PERSISTENCE, new Tomcat());
		//wait for clients to register
		Thread.sleep(6000);
		RESTClient<Product> p1c = new RESTClient<>("http://localhost:" 
				 + getPort(client1) + "/" + Service.PERSISTENCE.getServiceName(),
				 "rest", "products", Product.class);
		RESTClient<Product> p2c = new RESTClient<>("http://localhost:" 
				 + getPort(client2) + "/" + Service.PERSISTENCE.getServiceName(),
				 "rest", "products", Product.class);
		
		
		//create initial database
		RESTClient<String> dbc = new RESTClient<>("http://localhost:" 
				 + getPort(client1) + "/" + Service.PERSISTENCE.getServiceName(),
				 "rest", "generatedb", String.class);
		Response response = dbc.getService()
				.path(dbc.getApplicationURI()).path(dbc.getEndpointURI())
				.queryParam("categories", 3)
				.queryParam("products", 10)
				.queryParam("users", 20)
				.queryParam("orders", 4)
				.request(MediaType.TEXT_PLAIN).get();
		//wait for database to finish generating and check if the flag is correct
		String finishedGenerating = dbc.getService()
				.path(dbc.getApplicationURI()).path(dbc.getEndpointURI()).path("finished")
				.request(MediaType.TEXT_PLAIN).get().readEntity(String.class);
		Assert.assertEquals("false", finishedGenerating);
		while (finishedGenerating.equals("false")) {
			Thread.sleep(2000);
			finishedGenerating = dbc.getService()
					.path(dbc.getApplicationURI()).path(dbc.getEndpointURI()).path("finished")
					.request(MediaType.TEXT_PLAIN).get().readEntity(String.class);
		}
		Assert.assertEquals("true", finishedGenerating);
		Assert.assertFalse(response.readEntity(String.class).isEmpty());
		
		//clear cache for Categories
		Response response2 = ClientBuilder.newBuilder().build().target("http://localhost:" 
				 + getPort(client1) + "/" + Service.PERSISTENCE.getServiceName() + "/rest/cache/class")
				.path(Category.class.getName()).request(MediaType.TEXT_PLAIN).delete();
		Assert.assertEquals(Category.class.getName(), response2.readEntity(String.class));
		
		Response response3 = ClientBuilder.newBuilder().build().target("http://localhost:" 
				 + getPort(client2) + "/" + Service.PERSISTENCE.getServiceName() + "/rest/cache/cache")
				.request(MediaType.TEXT_PLAIN).delete();
		Assert.assertEquals("cleared", response3.readEntity(String.class));
		
		long id = NonBalancedCRUDOperations.getEntities(p1c, -1, 1).get(0).getId();
		boolean deleted = NonBalancedCRUDOperations.deleteEntity(p2c, id);
		Assert.assertTrue(deleted);
		Product gone = NonBalancedCRUDOperations.getEntity(p1c, id);
		Assert.assertNull(gone);
		gone = NonBalancedCRUDOperations.getEntity(p2c, id);
		Assert.assertNull(gone);
		
		destroy(client1);
		destroy(client2);
	}
	
	/**
	 * Dismantles the embedded Tomcat.
	 * @throws Throwable Throws uncaught throwables for test to fail.
	 */
	@After
	public void dismantle() throws Throwable {
		destroy(registryTomcat);
	}
	
	private void destroy(Tomcat tomcat) {
		if (tomcat.getServer() != null && registryTomcat.getServer().getState() != LifecycleState.DESTROYED) {
	        if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
	        	try {
					tomcat.stop();
				} catch (Exception e) {
					
				}
	        }
	        try {
				tomcat.destroy();
			} catch (Exception e) {

			}
	    }
	}
	
	private int getNextClientPort() {
		return testport++;
	}
	
	private int getRegistryPort() {
		return registryTomcat.getConnector().getLocalPort();
	}
	
	private int getPort(Tomcat tomcat) {
		return tomcat.getConnector().getLocalPort();
	}
}
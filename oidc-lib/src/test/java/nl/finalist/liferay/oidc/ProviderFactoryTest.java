package nl.finalist.liferay.oidc;

import org.junit.Test;

import nl.finalist.liferay.oidc.providers.AzureAD;
import nl.finalist.liferay.oidc.providers.UserInfoProvider;

import static org.junit.Assert.assertTrue;

public class ProviderFactoryTest {

	@Test
	public void testFactoryGenericProvider() {
		UserInfoProvider openIdProvider = ProviderFactory.getOpenIdProvider("none");
		assertTrue(openIdProvider.getClass() == UserInfoProvider.class);
	}
	
	@Test
	public void testFactoryGenericMatchProvider() {
		UserInfoProvider openIdProvider = ProviderFactory.getOpenIdProvider("GENERIC");
		assertTrue(openIdProvider.getClass() == UserInfoProvider.class);
	}
	
	@Test
	public void testFactoryGenericEmptyProvider() {
		UserInfoProvider openIdProvider = ProviderFactory.getOpenIdProvider("");
		assertTrue(openIdProvider.getClass() == UserInfoProvider.class);
	}
	
	
	@Test
	public void testFactoryAzureMatchProvider() {
		UserInfoProvider openIdProvider = ProviderFactory.getOpenIdProvider("AZURE");
		assertTrue(openIdProvider instanceof AzureAD);
	}
	
	@Test
	public void testFactoryAzureCaseNotMatchProvider() {
		UserInfoProvider openIdProvider = ProviderFactory.getOpenIdProvider("azure");
		assertTrue(openIdProvider instanceof AzureAD);
	}

}

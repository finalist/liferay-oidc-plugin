package nl.finalist.liferay.oidc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import nl.finalist.liferay.oidc.providers.AzureAD;
import nl.finalist.liferay.oidc.providers.GenericProvider;

public class ProviderFactoryTest {
	private static final String PROPKEY_PROVIDER = "openidconnect.provider";


	@Test
	public void testFactoryGenericProvider() {
		LiferayAdapter liferay = mockLiferayAdapterForProvider("none");
		LibAutoLogin openIdProvider = ProviderFactory.getOpenIdProvider(liferay);
		
		assertTrue(openIdProvider instanceof GenericProvider);
	}
	
	@Test
	public void testFactoryGenericMatchProvider() {
		LiferayAdapter liferay = mockLiferayAdapterForProvider("GENERIC");
		LibAutoLogin openIdProvider = ProviderFactory.getOpenIdProvider(liferay);
		
		assertTrue(openIdProvider instanceof GenericProvider);
	}
	
	@Test
	public void testFactoryGenericEmptyProvider() {
		LiferayAdapter liferay = mockLiferayAdapterForProvider("");
		LibAutoLogin openIdProvider = ProviderFactory.getOpenIdProvider(liferay);
		
		assertTrue(openIdProvider instanceof GenericProvider);
	}
	
	
	@Test
	public void testFactoryAzureMatchProvider() {
		LiferayAdapter liferay = mockLiferayAdapterForProvider("AZURE");
		LibAutoLogin openIdProvider = ProviderFactory.getOpenIdProvider(liferay);
		
		assertTrue(openIdProvider instanceof AzureAD);
	}
	
	@Test
	public void testFactoryAzureCaseNotMatchProvider() {
		LiferayAdapter liferay = mockLiferayAdapterForProvider("azure");
		LibAutoLogin openIdProvider = ProviderFactory.getOpenIdProvider(liferay);
		
		assertTrue(openIdProvider instanceof AzureAD);
	}
	
	private LiferayAdapter mockLiferayAdapterForProvider(String provider) {
		LiferayAdapter liferay = mock(LiferayAdapter.class);
		when(liferay.getPortalProperty(PROPKEY_PROVIDER, "")).thenReturn(provider);
		
		return liferay;
	}
}

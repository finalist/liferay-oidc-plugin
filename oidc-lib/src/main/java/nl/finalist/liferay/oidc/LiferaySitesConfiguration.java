package nl.finalist.liferay.oidc;

/**
 * Our own configuration definition, portable/usable across Liferay versions.
 * Implementations of this interface can be catered towards specific versions and their
 * respective configuration implementations.
 */
public interface LiferaySitesConfiguration{
    
    /**
     * Return list of sites to include
     * @return
     */
    String[] sitesToInclude();
}

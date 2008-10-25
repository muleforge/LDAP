package org.mule.transport.ldap;

public class FetchSchemaAction implements SynchronousLDAPAction
{
    private String dn;

    public FetchSchemaAction(final String dn)
    {
        super();
        this.dn = dn;
    }

    public String getDn()
    {
        return dn;
    }

    public void setDn(final String dn)
    {
        this.dn = dn;
    }
}

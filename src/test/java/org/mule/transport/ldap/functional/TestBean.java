package org.mule.transport.ldap.functional;

public  class TestBean
{
    
    
    
    public TestBean()
    {
        super();
       System.out.println("TestBean created");
    }


    private String cn;
    private String dn;
    private String sn;
    private String xx;
    private String objectClass;
    private String o;
    
    public String getCn()
    {
        return cn;
    }


    public void setCn(String cn)
    {
        this.cn = cn;
    }


    public String getDn()
    {
        return dn;
    }


    public void setDn(String dn)
    {
        this.dn = dn;
    }


    public String getSn()
    {
        return sn;
    }


    public void setSn(String sn)
    {
        this.sn = sn;
    }


    public String getXx()
    {
        return xx;
    }


    public void setXx(String xx)
    {
        this.xx = xx;
    }


    @Override
    public String toString()
    {
        // TODO Auto-generated method stub
        return o+objectClass;
    }


    public String getObjectClass()
    {
        return objectClass;
    }


    public void setObjectClass(String objectClass)
    {
        this.objectClass = objectClass;
    }


    public String getO()
    {
        return o;
    }


    public void setO(String o)
    {
        this.o = o;
    }
    
    
}

package org.mule.providers.ldap.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleServer;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.config.i18n.CoreMessages;
import org.mule.endpoint.AbstractEndpointBuilder;
import org.mule.util.StringUtils;
import org.mule.util.expression.ExpressionEvaluator;


public class EndpointURIExpressionEvaluator implements ExpressionEvaluator
{
    public static final String NAME = "endpointuri";

    /**
     * logger used by this class
     */
    protected transient final Log logger = LogFactory.getLog(EndpointURIExpressionEvaluator.class);

    public Object evaluate(String expression, Object message)
    {
        int i = expression.indexOf(".");
        String endpointName;
        String property;
        if(i > 0)
        {
            endpointName = expression.substring(0, i);
            property = expression.substring(i + 1);
        }
        else
        {
            throw new IllegalArgumentException(CoreMessages.expressionMalformed(expression, getName()).getMessage());
        }
        
        
        EndpointURI uri = null;

        //looking for endpoint in the registry, if not look for an EndpointBuilder
        Object tmp = MuleServer.getMuleContext().getRegistry().lookupObject(endpointName);
        
        if(tmp != null && tmp instanceof ImmutableEndpoint)
        {
        	ImmutableEndpoint ep = (ImmutableEndpoint) tmp;
        	uri = ep.getEndpointURI();
        }
        else
        {
        	 logger.info("There is no endpoint registered with name: " + endpointName + " Will look for an global one ...");
        	
        	 AbstractEndpointBuilder eb = (AbstractEndpointBuilder)MuleServer.getMuleContext().getRegistry().lookupEndpointBuilder(endpointName);
             
        	 if(eb!=null)
             {
        		 uri = eb.getEndpointBuilder().getEndpoint();
             }
        	 else
        	 {
        		 logger.warn("There is no endpointbuilder registered with name: " + endpointName);        		 
        	 }
        	 
        }    
        
        
        if(uri!=null)
        {

        	//${endpointuri:testendpoint.params:xxx}
        	if(property.toLowerCase().startsWith("params:"))
            {
        		String[] sa = property.split(":");
        		
                return uri.getParams().getProperty(sa[1]);
            }
        	
        	//${endpointuri:testendpoint.params:xxx}
        	if(property.toLowerCase().startsWith("userparams:"))
            {
        		String[] sa = property.split(":");
        		
                return uri.getUserParams().getProperty(sa[1]);
            }
            
        	//resovles dynamically to getXXX Method
             try {
   				return uri.getClass().getMethod("get"+StringUtils.capitalize(property.toLowerCase()),new Class[0]).invoke(uri,(Object[]) null);
			} catch (Exception e) {
				logger.error(e.toString(),e);
			} 
            
          
            throw new IllegalArgumentException(CoreMessages.expressionInvalidForProperty(property, expression).getMessage());
            
        }
        else
        {
            logger.warn("There is no endpoint registered with name: " + endpointName);
            return null;
        }
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return NAME;
    }

    /** {@inheritDoc} */
    public void setName(String name)
    {
        throw new UnsupportedOperationException("setName");
    }
}
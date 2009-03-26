package org.mule.transport.ldap.transformers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;
import org.mule.transport.ldap.util.LDAPUtils;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPSearchResults;

public class LDAPSearchResultsToJavaBean extends AbstractTransformer
{

    private String beanclass;

    @Override
    protected Object doTransform(final Object src, final String encoding)
            throws TransformerException
    {
        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        if (!(src instanceof LDAPSearchResults))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "wrong type " + src.getClass()
                            + ", LDAPSearchResults expected"));
        }

        final LDAPSearchResults res = (LDAPSearchResults) src;

        try
        {
            final LDAPResultsHandler h = new LDAPResultsHandler(beanclass);
            return h.toBeanList(res);
        }
        catch (final Exception e)
        {
            throw new TransformerException(this, e);
        }

    }

	/* Code contributed by Tomas Blohm, tomas.blohm@pulsen.se 
	*/
    private static class LDAPResultsHandler
    {
        protected Class beanClass;
        protected final Log logger = LogFactory.getLog(getClass());

        LDAPResultsHandler(final Class beanClass)
        {
            this.beanClass = beanClass;
        }

        LDAPResultsHandler(final String beanClass)
                throws ClassNotFoundException
        {
            if (beanClass != null)
            {
                this.beanClass = Class.forName(beanClass);
            }
        }

        public List toBeanList(final LDAPSearchResults rs) throws Exception
        {
            final List results = new ArrayList();
            if (!rs.hasMore())
            {
                return results;
            }
            final PropertyDescriptor props[] = propertyDescriptors(beanClass);
            int columnToProperty[] = null;
            while (rs.hasMore())
            {
                final LDAPEntry entry = rs.next();
                logger.debug("processing " + LDAPUtils.dumpLDAPEntry(entry));

                if (columnToProperty == null)
                {
                    columnToProperty = mapColumnsToProperties(entry
                            .getAttributeSet(), props);
                }

                results.add(createBean(entry, beanClass, props,
                        columnToProperty));

            }
            return results;
        }

        private Object createBean(final LDAPEntry entry, final Class type,
                final PropertyDescriptor props[], final int columnToProperty[])
                throws Exception
        {
            final Object bean = newInstance(type);
            logger.debug("bean " + bean.getClass() + " created");
            logger.debug("bean " + bean);
            for (int i = 1; i < columnToProperty.length; i++)
            {
                if (columnToProperty[i] == -1)
                {
                    continue;
                }
                final PropertyDescriptor prop = props[columnToProperty[i]];
                final Class propType = prop.getPropertyType();
                final ArrayList attributes = new ArrayList();
                org.apache.commons.collections.CollectionUtils.addAll(
                        attributes, entry.getAttributeSet().iterator());
                Object value = processColumn(attributes, i - 1, propType);
                if ((propType != null) && (value == null)
                        && propType.isPrimitive())
                {
                    value = primitiveDefaults.get(propType);
                }
                callSetter(bean, prop, value);
            }
            logger.debug("return: " + bean);
            return bean;
        }

        private void callSetter(final Object target,
                final PropertyDescriptor prop, final Object value)
                throws Exception
        {
            final Method setter = prop.getWriteMethod();
            if (setter == null)
            {
                return;
            }
            final Class params[] = setter.getParameterTypes();
            try
            {
                if (isCompatibleType(value, params[0]))
                {
                    setter.invoke(target, new Object[]
                    {value});
                }
                else
                {
                    throw new Exception("Cannot set " + prop.getName()
                            + ": incompatible types.");
                }
            }
            catch (final IllegalArgumentException e)
            {
                throw new Exception("Cannot set " + prop.getName() + ": "
                        + e.getMessage());
            }
            catch (final IllegalAccessException e)
            {
                throw new Exception("Cannot set " + prop.getName() + ": "
                        + e.getMessage());
            }
            catch (final InvocationTargetException e)
            {
                throw new Exception("Cannot set " + prop.getName() + ": "
                        + e.getMessage());
            }
        }

        private boolean isCompatibleType(final Object value, final Class type)
        {
            if ((value == null) || type.isInstance(value))
            {
                return true;
            }
            if (type.equals(Integer.TYPE)
                    && (java.lang.Integer.class).isInstance(value))
            {
                return true;
            }
            if (type.equals(Long.TYPE)
                    && (java.lang.Long.class).isInstance(value))
            {
                return true;
            }
            if (type.equals(Double.TYPE)
                    && (java.lang.Double.class).isInstance(value))
            {
                return true;
            }
            if (type.equals(Float.TYPE)
                    && (java.lang.Float.class).isInstance(value))
            {
                return true;
            }
            if (type.equals(Short.TYPE)
                    && (java.lang.Short.class).isInstance(value))
            {
                return true;
            }
            if (type.equals(Byte.TYPE)
                    && (java.lang.Byte.class).isInstance(value))
            {
                return true;
            }
            if (type.equals(Character.TYPE)
                    && (java.lang.Character.class).isInstance(value))
            {
                return true;
            }
            return type.equals(Boolean.TYPE)
                    && (java.lang.Boolean.class).isInstance(value);
        }

        protected Object newInstance(final Class c) throws Exception
        {
            logger.debug(c.getName());
            final Object in = c.newInstance();
            logger.debug("inst: " + in);
            return in;

        }

        private PropertyDescriptor[] propertyDescriptors(final Class c)
                throws Exception
        {
            BeanInfo beanInfo = null;
            try
            {
                beanInfo = Introspector.getBeanInfo(c);
            }
            catch (final IntrospectionException e)
            {
                throw new Exception("Bean introspection failed: "
                        + e.getMessage());
            }
            return beanInfo.getPropertyDescriptors();
        }

        protected int[] mapColumnsToProperties(final LDAPAttributeSet las,
                final PropertyDescriptor props[]) throws Exception
        {
            final int cols = las.size();
            final int columnToProperty[] = new int[cols + 1];
            Arrays.fill(columnToProperty, -1);
            final Iterator attrs = las.iterator();

            label0: for (int col = 1; col <= cols; col++)
            {
                // String columnName = (String) attrs.get(col);
                final String columnName = ((LDAPAttribute) attrs.next())
                        .getName();
                logger.debug("col " + columnName);
                int i = 0;
                do
                {
                    if (i >= props.length)
                    {
                        continue label0;
                    }
                    if (columnName.equalsIgnoreCase(props[i].getName()))
                    {
                        columnToProperty[col] = i;
                        continue label0;
                    }
                    i++;
                }
                while (true);
            }

            return columnToProperty;
        }

        protected Object processColumn(final ArrayList attrSet,
                final int index, final Class propType) throws Exception
        {
            if (propType.equals(java.lang.String.class))
            {
                return ((LDAPAttribute) attrSet.get(index)).getStringValue();
            }
            if (propType.equals(Integer.TYPE)
                    || propType.equals(java.lang.Integer.class))
            {
                return new Integer(((LDAPAttribute) attrSet.get(index))
                        .getStringValue());
            }
            if (propType.equals(Boolean.TYPE)
                    || propType.equals(java.lang.Boolean.class))
            {
                return new Boolean(((LDAPAttribute) attrSet.get(index))
                        .getStringValue());
            }
            if (propType.equals(Long.TYPE)
                    || propType.equals(java.lang.Long.class))
            {
                return new Long(((LDAPAttribute) attrSet.get(index))
                        .getStringValue());
            }
            if (propType.equals(Double.TYPE)
                    || propType.equals(java.lang.Double.class))
            {
                return new Double(((LDAPAttribute) attrSet.get(index))
                        .getStringValue());
            }
            if (propType.equals(Float.TYPE)
                    || propType.equals(java.lang.Float.class))
            {
                return new Float(((LDAPAttribute) attrSet.get(index))
                        .getStringValue());
            }
            if (propType.equals(Short.TYPE)
                    || propType.equals(java.lang.Short.class))
            {
                return new Short(((LDAPAttribute) attrSet.get(index))
                        .getStringValue());
            }
            if (propType.equals(Byte.TYPE)
                    || propType.equals(java.lang.Byte.class))
            {
                return new Byte(((LDAPAttribute) attrSet.get(index))
                        .getStringValue());
            }
            else
            {
                return (attrSet.get(index));
            }
        }

        protected static final int PROPERTY_NOT_FOUND = -1;
        private static final Map primitiveDefaults;

        static
        {
            primitiveDefaults = new HashMap();
            primitiveDefaults.put(Integer.TYPE, new Integer(0));
            primitiveDefaults.put(Short.TYPE, new Short((short) 0));
            primitiveDefaults.put(Byte.TYPE, new Byte((byte) 0));
            primitiveDefaults.put(Float.TYPE, new Float(0.0F));
            primitiveDefaults.put(Double.TYPE, new Double(0.0D));
            primitiveDefaults.put(Long.TYPE, new Long(0L));
            primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
            primitiveDefaults.put(Character.TYPE, new Character('\0'));
        }

    }

    public String getBeanclass()
    {
        return beanclass;
    }

    public void setBeanclass(final String beanclass)
    {
        this.beanclass = beanclass;
    }

}

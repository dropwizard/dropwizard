package com.codahale.dropwizard.views;


import com.google.common.base.Preconditions;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * Simple map-backed {@link View} implementation.
 * <p/>
 * This class eliminates the need to create individual classes for each view.
 * <p/>
 * Unlike base {@link View} class and its typical derivatives, this view does not take package
 * name into consideration when resolving template name -- it simply passes the template name
 * to the {@link ViewRenderer} as is.
 *
 * @author Aleksandar Seovic  2013.07.17
 */
public class ModelView
        extends View implements ModelContainer<Map<String, Object>> {
    private final Map<String, Object> model = new HashMap<>();

    /**
     * Creates a new view.
     *
     * @param templateName the name of the template resource
     */
    public ModelView(String templateName) {
        super(templateName);
    }

    /**
     * Creates a new view.
     *
     * @param templateName the name of the template resource
     * @param charset      the character set for {@code templateName}
     */
    public ModelView(String templateName, Charset charset) {
        super(templateName, charset);
    }

    /**
     * Adds a single key/value pair to this view.
     * <p/>
     * The specified value will be accessible within the view template using the specified key.
     *
     * @param key    key to add
     * @param value  value to add
     */
    public ModelView add(String key, Object value) {
        model.put(key, value);
        return this;
    }

    /**
     * Adds multiple key/value pairs to this view.
     * <p/>
     * The specified values will be accessible within the view template using their
     * corresponding keys.
     *
     * @param values  values to add
     */
    public ModelView add(Map values) {
        model.putAll(values);
        return this;
    }

    /**
     * Adds object properties to this view.
     * <p/>
     * This method will use introspection to find all readable properties of the specified
     * object instance and will add them to the view using property name as the key and property
     * value as the value.
     * <p/>
     * The property values will be accessible within the view template using their standard
     * JavaBean names.
     *
     * @param obj  object to add
     */
    public ModelView add(Object obj) {
        Preconditions.checkNotNull(obj);

        try {
            BeanInfo info = Introspector.getBeanInfo(obj.getClass());
            for (PropertyDescriptor property : info.getPropertyDescriptors()) {
                Method getter = property.getReadMethod();
                if (getter != null) {
                    model.put(property.getName(), getter.invoke(obj));
                }
            }
            return this;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getModel() {
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String resolveName(String templateName) {
        return templateName;
    }
}

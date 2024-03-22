package org.fourthline.cling.support.qplay;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChangeParser;

import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Description: QPlay服务变化监听 <br>
 *
 * @author: fy <br>
 * Date: 2024/2/21 <br>
 */
public class QPlayLastChangeParser extends LastChangeParser {

    public static final String NAMESPACE_URI = "urn:schemas-upnp-org:metadata-1-0/RCS/";
    public static final String SCHEMA_RESOURCE = "org/fourthline/cling/support/renderingcontrol/metadata-1.0-rcs.xsd";

    @Override
    protected String getNamespace() {
        return NAMESPACE_URI;
    }

    @Override
    protected Source[] getSchemaSources() {
        // TODO: Android 2.2 has a broken SchemaFactory, we can't validate
        // http://code.google.com/p/android/issues/detail?id=9491&q=schemafactory&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars
        if (!ModelUtil.ANDROID_RUNTIME) {
            return new Source[]{new StreamSource(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_RESOURCE)
            )};
        }
        return null;
    }

    @Override
    protected Set<Class<? extends EventedValue>> getEventedVariables() {
        return QPlayTransportVariable.ALL;
    }
}

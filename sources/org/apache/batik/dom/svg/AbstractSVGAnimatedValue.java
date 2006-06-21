/*

   Copyright 2006  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.dom.svg;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * An abstract base class for the <code>SVGAnimated*</code> classes, that
 * implements an {@link AnimatedAttributeListener} list.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id$
 */
public abstract class AbstractSVGAnimatedValue
    implements AnimatedLiveAttributeValue {

    /**
     * The associated element.
     */
    protected AbstractElement element;

    /**
     * The namespace URI of the attribute.
     */
    protected String namespaceURI;

    /**
     * The local name of the attribute.
     */
    protected String localName;

    /**
     * Whether there is a current animated value.
     */
    protected boolean hasAnimVal;

    /**
     * Listener list.
     */
    protected LinkedList listeners = new LinkedList();

    /**
     * Creates a new AbstractSVGAnimatedValue.
     */
    public AbstractSVGAnimatedValue(AbstractElement elt, String ns, String ln) {
        element = elt;
        namespaceURI = ns;
        localName = ln;
    }

    /**
     * Returns the namespace URI of the attribute.
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Returns the local name of the attribute.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Returns whether this animated value has a specified value.
     * @return true if the DOM attribute is specified or if the attribute has
     *         an animated value, false otherwise
     */
    public boolean isSpecified() {
        return hasAnimVal || element.hasAttributeNS(namespaceURI, localName);
    }

    /**
     * Adds a listener for changes to the animated value.
     */
    public void addAnimatedAttributeListener(AnimatedAttributeListener aal) {
        if (!listeners.contains(aal)) {
            listeners.add(aal);
        }
    }

    /**
     * Removes a listener for changes to the animated value.
     */
    public void removeAnimatedAttributeListener(AnimatedAttributeListener aal) {
        listeners.remove(aal);
    }

    /**
     * Fires the listeners for the animated value.
     */
    protected void fireAnimatedAttributeListeners() {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            ((AnimatedAttributeListener) i.next()).animatedAttributeChanged
                (element, this);
        }
    }
}

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
package org.apache.batik.anim;

import org.apache.batik.anim.values.AnimatableValue;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGLength;

/**
 * An interface for targets of animation to provide context information.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id$
 */
public interface AnimationTarget {

    // Constants for percentage interpretation.
    static final int PERCENTAGE_FONT_SIZE       = 0;
    static final int PERCENTAGE_VIEWPORT_WIDTH  = 1;
    static final int PERCENTAGE_VIEWPORT_HEIGHT = 2;
    static final int PERCENTAGE_VIEWPORT_SIZE   = 3;

    /**
     * Returns the element.
     */
    Element getElement();

    /**
     * Updates a property value in this target.
     */
    void updatePropertyValue(String pn, AnimatableValue val);

    /**
     * Updates an attribute value in this target.
     */
    void updateAttributeValue(String ns, String ln, AnimatableValue val);

    /**
     * Gets how percentage values are interpreted by the given attribute
     * or property.
     */
    int getPercentageInterpretation(String ns, String an, boolean isCSS);

    /**
     * Returns whether color interpolations should be done in linear RGB
     * color space rather than sRGB.
     */
    boolean useLinearRGBColorInterpolation();

    /**
     * Converts the given SVG length into user units.
     * @param v the SVG length value
     * @param type the SVG length units (one of the
     *             {@link SVGLength}.SVG_LENGTH_* constants)
     * @param pcInterp how to interpretet percentage values (one of the
     *             {@link AnimationTarget}.PERCENTAGE_* constants) 
     * @return the SVG value in user units
     */
    float svgToUserSpace(float v, int type, int pcInterp);

    // Listeners

    /**
     * Adds a listener for changes to the given attribute value.
     */
    void addTargetListener(String attributeName, boolean isCSS,
                           AnimationTargetListener l);

    /**
     * Removes a listener for changes to the given attribute value.
     */
    void removeTargetListener(String attributeName, boolean isCSS,
                              AnimationTargetListener l);
}

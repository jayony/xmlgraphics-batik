/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.refimpl.gvt.renderer;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNodeRenderContext;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.Selector;
import org.apache.batik.gvt.Selectable;
import org.apache.batik.gvt.filter.GraphicsNodeRable;
import org.apache.batik.gvt.filter.GraphicsNodeRableFactory;
import org.apache.batik.gvt.renderer.Renderer;
import org.apache.batik.gvt.event.GraphicsNodeMouseListener;

import org.apache.batik.refimpl.gvt.filter.ConcreteGraphicsNodeRableFactory;
import org.apache.batik.refimpl.gvt.text.ConcreteTextSelector;
import java.util.Iterator;
import java.util.Stack;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.RenderContext;

/**
 * Simple implementation of the Renderer that simply does static
 * rendering in an offscreen buffer image.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com>Vincent Hardy</a>
 * @version $Id$
 */
public class StaticRenderer implements Renderer {
    /**
     * Error messages
     */
    private static final String ILLEGAL_ARGUMENT_NULL_OFFSCREEN =
        "offScreen should not be null";
    private static final String ILLEGAL_ARGUMENT_ZERO_WIDTH_OR_HEIGHT =
        "offScreen should have positive width/height";

    /**
     * Tree this Renderer paints.
     */
    protected GraphicsNode treeRoot;

    /**
     * Flag for progressive rendering. Not used in this implementation
     */
    private boolean progressivePaintAllowed;

    /**
     * The Selector instance which listens to TextSelection gestures.
     */
    private Selector textSelector = null;

    /**
     * Offscreen image where the Renderer does its rendering
     */
    protected BufferedImage offScreen;

    /**
     * Passed to the GVT tree to describe the rendering environment
     */
    protected GraphicsNodeRenderContext nodeRenderContext;

    /**
     * The transform to go to device space.
     */
    protected AffineTransform usr2dev;

    /**
     * @param offScreen image where the Renderer should do its rendering
     */
    public StaticRenderer(BufferedImage offScreen){
        setOffScreen(offScreen);

        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_ON);

        hints.put(RenderingHints.KEY_INTERPOLATION,
                  RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        FontRenderContext fontRenderContext =
            new FontRenderContext(new AffineTransform(), true, true);

        TextPainter textPainter = new StrokingTextPainter();

        GraphicsNodeRableFactory gnrFactory =
            new ConcreteGraphicsNodeRableFactory();

        this.nodeRenderContext =
            new GraphicsNodeRenderContext(new AffineTransform(),
                                          null,
                                          hints,
                                          fontRenderContext,
                                          textPainter,
                                          gnrFactory);
    }

    /**
     * @param offScreen image where the Renderer should do its rendering
     */
    public void setOffScreen(BufferedImage offScreen){
        if(offScreen == null)
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_NULL_OFFSCREEN);
        if((offScreen.getWidth() <= 0) || (offScreen.getHeight() <= 0) )
            throw new IllegalArgumentException(
                ILLEGAL_ARGUMENT_ZERO_WIDTH_OR_HEIGHT +
                " : offScreen.getWidth() = " + offScreen.getWidth() +
                " / offScreen.getHeight() = " + offScreen.getHeight() );

        this.offScreen = offScreen;
    }

    /**
     * This associates the given GVT Tree with this renderer.
     * Any previous tree association is forgotten.
     * Not certain if this should be just GraphicsNode, or CanvasGraphicsNode.
     */
    public void setTree(GraphicsNode treeRoot){
        this.treeRoot = treeRoot;
        // associate selectable nodes with selector object(s)
        //initSelectors();
    }

    /**
     * @return the GVT tree associated with this renderer
     */
    public GraphicsNode getTree(){
        return treeRoot;
    }

    /**
     * Forces repaint of provided node. 'node' must be a node in the
     * currently associated GVT tree. Normally there is no need to
     * call this method explicitly as the Renderer listens for changes
     * on all nodes in the tree it is associated with.
     *
     * @param area region to be repainted, in the current user space
     * coordinate system.
     */
    public void repaint(Shape area){
        // First, set the Area Of Interest in the renderContext
        nodeRenderContext.setTransform(usr2dev);
        nodeRenderContext.setAreaOfInterest(area);

        // Now, paint into offscreen image
        Graphics2D g = offScreen.createGraphics();

        // Set default rendering hints
        g.addRenderingHints(nodeRenderContext.getRenderingHints());

        // Set initial transform as required by the render context
        g.transform(nodeRenderContext.getTransform());

        // Set initial clip
        g.clip(nodeRenderContext.getAreaOfInterest());

        // Render tree
        if(treeRoot != null) {
            treeRoot.paint(g, nodeRenderContext);
        }
    }

    /**
     * Sets the transform from the current user space (as defined by
     * the top node of the GVT tree, to the associated device space.
     *
     * @param usr2dev the new user space to device space transform. If null,
     *        the identity transform will be set.
     */
    public void setTransform(AffineTransform usr2dev){
        if(usr2dev == null) {
            usr2dev = new AffineTransform();
        }
        this.usr2dev = usr2dev;
        // Update the RenderContext in the nodeRenderContext
        nodeRenderContext.setTransform(usr2dev);
    }

    /**
     * Returns a copy of the transform from the current user space (as
     * defined by the top node of the GVT tree) to the device space (1
     * unit = 1/72nd of an inch / 1 pixel, roughly speaking
     */
    public AffineTransform getTransform(){
        return nodeRenderContext.getTransform();
    }

    /**
     * Returns true if the Renderer is currently allowed to do
     * progressive painting.
     */
    public boolean isProgressivePaintAllowed(){
        return progressivePaintAllowed;
    }

    /**
     * Turns on/off progressive painting. Turning off progressive
     * painting will cause a repaint if any progressive painting has
     * been made.
     */
    public void setProgressivePaintAllowed(boolean progressivePaintAllowed){
        this.progressivePaintAllowed = progressivePaintAllowed;
    }

    /**
     * Associate selectable elements in the current tree with
     * Selector instances.
     */
    public void initSelectors() {
        Iterator nodeIter = new GraphicsNodeTreeIterator(treeRoot);
        if (textSelector == null) {
	    textSelector =
                new ConcreteTextSelector(nodeRenderContext);
        }
        while (nodeIter.hasNext()) {
            GraphicsNode node = (GraphicsNode) nodeIter.next();
            if (node instanceof Selectable) {
                node.addGraphicsNodeMouseListener(
                             (GraphicsNodeMouseListener) textSelector);
                // should make sure this does not add duplicates
            }
        }
    }

    class GraphicsNodeTreeIterator implements Iterator {

        GraphicsNode root;
        GraphicsNode current = null;
        Iterator currentIter;
        Stack iterStack;
        Stack nodeStack;

        public GraphicsNodeTreeIterator(GraphicsNode root) {
            this.root = root;
            if (root instanceof CompositeGraphicsNode) {
                currentIter = ((CompositeGraphicsNode) root).getChildren().iterator();
            }
            iterStack = new Stack();
            nodeStack = new Stack();
        }

        public boolean hasNext() {
            return (current != root);
        }

        public Object next() {
            if (currentIter.hasNext()) {
                current = (GraphicsNode) currentIter.next();
                while (current instanceof CompositeGraphicsNode) {
                    iterStack.push(currentIter);
                    nodeStack.push(current);
                    currentIter = ((CompositeGraphicsNode) current).getChildren().iterator();
                    if (currentIter.hasNext()) {
                        current = (GraphicsNode) currentIter.next();
                    } else {
                        currentIter = (Iterator) iterStack.pop();
                        current = (GraphicsNode) nodeStack.pop();
                    }
                }
            } else {
                if (!iterStack.empty()) {
                    do {
                        currentIter = (Iterator) iterStack.pop();
                        current = (GraphicsNode) nodeStack.pop();
                    } while (!currentIter.hasNext() && !iterStack.isEmpty());
                } else {
                    current = root;
                }
            }
            return current;
        }

        public void remove() {
            ; // FIXME: should throw an exception, probably
        }
    }

}

















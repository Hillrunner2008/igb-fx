/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component.api;

import java.util.List;

/**
 *
 * @author jeckstei
 */
public abstract class Component<P extends Props, S extends State> {

    protected P props;
    protected S state;
    private List<Component> children;

    public P getProps() {
        return props;
    }

    public abstract Component beforeComponentReady();

    public abstract List<Component> render();

    public Component withAttributes(P props) {
        this.props = props;
        return this;
    }

    public void setState(S state) {
        //fire rerender
        this.state = state;
        renderComponents((Component<Props, State>) this);
    }

    public S getState() {
        return state;
    }

    public void renderComponents() {
        render().forEach(child -> {
            renderComponents(child);
        });
    }

    public void renderComponents(Component<Props, State> component) {
        component.render().forEach(child -> {
            renderComponents(child);
        });
    }

    public void close() {
    }

}

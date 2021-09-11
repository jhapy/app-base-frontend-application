package org.jhapy.frontend.views.dashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import org.jhapy.frontend.components.navigation.menubar.View;

public abstract class Dashlet extends Div {
    public Dashlet(View parentView) {
        this.parentView = parentView;
    }

    protected View parentView;
    protected abstract Component[] getComponents();

    protected abstract String getDashletClassName();

    protected abstract String[] getClasses();
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        addClassName(getDashletClassName());

        Div card = new Div();
        card.addClassNames(getClasses());
        card.add(getComponents());

        add(card);
    }
}
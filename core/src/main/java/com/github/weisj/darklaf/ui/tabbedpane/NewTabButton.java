/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.weisj.darklaf.ui.tabbedpane;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;

public class NewTabButton extends JPanel implements UIResource {

    protected final JButton button;
    protected DarkTabbedPaneUI ui;

    protected NewTabButton(final DarkTabbedPaneUI ui) {
        this.ui = ui;
        button = createButton();
        button.addActionListener(e -> {
            Action action = ui.getNewTabAction();
            if (action != null) {
                action.actionPerformed(e);
            }
        });
        add(button);
        setOpaque(false);
        setLayout(null);
    }

    protected JButton createButton() {
        JButton button = new JButton();
        button.setIcon(ui.getNewTabIcon());
        button.putClientProperty("JButton.variant", "shadow");
        button.putClientProperty("JButton.square", true);
        button.putClientProperty("JButton.alternativeArc", Boolean.TRUE);
        button.putClientProperty("JButton.thin", Boolean.TRUE);
        button.setRolloverEnabled(true);
        button.setOpaque(false);
        button.setMargin(UIManager.getInsets("TabbedPane.newTabButton.insets"));
        return button;
    }

    @Override
    public void doLayout() {
        Dimension b = button.getPreferredSize();
        int x = (getWidth() - b.width) / 2;
        int y = (getHeight() - b.height) / 2;
        button.setBounds(x, y, b.width, b.height);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        ui.paintTabAreaBorder(g, ui.tabPane.getTabPlacement(), 0, 0, getWidth() + 1, getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return button.getPreferredSize();
    }
}

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
package com.github.weisj.darklaf.ui.cell;

import com.github.weisj.darklaf.components.SelectableTreeNode;
import com.github.weisj.darklaf.decorators.CellRenderer;
import com.github.weisj.darklaf.ui.tree.DarkTreeCellRenderer;
import com.github.weisj.darklaf.util.DarkUIUtil;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * @author vincencopalazzo
 * @author atarw
 * @author Jannis Weis
 */
public class DarkCellRendererToggleButton<T extends JToggleButton & CellEditorToggleButton>
        implements TableCellRenderer, TreeCellRenderer, SwingConstants {

    private final T toggleButton;


    public DarkCellRendererToggleButton(final T toggleButton) {
        this.toggleButton = toggleButton;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                   final boolean isSelected, final boolean focus,
                                                   final int row, final int column) {
        if (value instanceof Boolean) {
            toggleButton.setSelected((Boolean) value);
        }
        toggleButton.setHorizontalAlignment(table.getComponentOrientation().isLeftToRight() ? LEFT : RIGHT);
        toggleButton.setHasFocus(focus);

        boolean alternativeRow = Boolean.TRUE.equals(table.getClientProperty("JTable.alternateRowColor"));
        Color alternativeRowColor = UIManager.getColor("Table.alternateRowBackground");
        Color normalColor = UIManager.getColor("Table.background");
        Color background = alternativeRow && row % 2 == 1 ? alternativeRowColor : normalColor;
        if (!(isSelected) || table.isEditing()) {
            toggleButton.setBackground(background);
            toggleButton.setForeground(table.getForeground());
        } else {
            if (DarkUIUtil.hasFocus(table)) {
                toggleButton.setForeground(UIManager.getColor("Table.selectionForeground"));
            } else {
                toggleButton.setForeground(UIManager.getColor("Table.selectionForegroundInactive"));
            }
            toggleButton.setBackground(table.getSelectionBackground());
        }
        return toggleButton;
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
                                                  final boolean expanded, final boolean leaf, final int row,
                                                  final boolean focus) {
        if (value instanceof Boolean) {
            toggleButton.setSelected((Boolean) value);
        } else {
            boolean sel = Boolean.TRUE.equals(DarkTreeCellRenderer.unwrapBooleanIfPossible(value));
            toggleButton.setSelected(sel);
            if (value instanceof SelectableTreeNode) {
                toggleButton.setText(((SelectableTreeNode) value).getLabel());
            }
        }
        if (selected) {
            if (DarkUIUtil.hasFocus(tree)) {
                toggleButton.setForeground(UIManager.getColor("Tree.selectionForeground"));
            } else {
                toggleButton.setForeground(UIManager.getColor("Tree.selectionForegroundInactive"));
            }
        } else {
            toggleButton.setForeground(tree.getForeground());
        }
        toggleButton.setHorizontalAlignment(tree.getComponentOrientation().isLeftToRight() ? LEFT : RIGHT);
        toggleButton.setHasFocus(false);

        return toggleButton;
    }

    public JToggleButton getButton() {
        return toggleButton;
    }

    public static class CellEditorCheckBox extends JCheckBox implements CellRenderer, CellEditorToggleButton {

        private boolean hasFocus;

        public CellEditorCheckBox(final boolean opaque) {
            setOpaque(opaque);
            putClientProperty("JToggleButton.isTreeCellEditor", true);
            putClientProperty("JToggleButton.isTableCellEditor", true);
        }

        public void setHasFocus(final boolean hasFocus) {
            this.hasFocus = hasFocus;
        }

        @Override
        public boolean hasFocus() {
            return hasFocus || super.hasFocus();
        }

        @Override
        public boolean isFocusOwner() {
            return super.hasFocus();
        }
    }

    public static class CellEditorRadioButton extends JRadioButton implements CellRenderer, CellEditorToggleButton {

        private boolean hasFocus;

        public CellEditorRadioButton(final boolean opaque) {
            setOpaque(opaque);
            putClientProperty("JToggleButton.isTreeCellEditor", true);
            putClientProperty("JToggleButton.isTableCellEditor", true);
        }

        public void setHasFocus(final boolean hasFocus) {
            this.hasFocus = hasFocus;
        }

        @Override
        public boolean hasFocus() {
            return hasFocus || super.hasFocus();
        }

        @Override
        public boolean isFocusOwner() {
            return super.hasFocus();
        }
    }

}

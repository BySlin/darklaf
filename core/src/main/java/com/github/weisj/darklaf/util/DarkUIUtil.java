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
package com.github.weisj.darklaf.util;

import com.github.weisj.darklaf.decorators.CellRenderer;
import com.github.weisj.darklaf.ui.popupmenu.DarkPopupMenuUI;
import sun.awt.SunToolkit;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * @author Konstantin Bulenkov
 * @author Jannis Weis
 */
public final class DarkUIUtil {

    public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
    public final static AlphaComposite GLOW_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    public final static AlphaComposite DROP_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
    public final static AlphaComposite SHADOW_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);
    public static final boolean USE_QUARTZ = "true".equals(System.getProperty("apple.awt.graphics.UseQuartz"));
    private static final Rectangle iconRect = new Rectangle();
    private static final Rectangle textRect = new Rectangle();

    private static Color getErrorGlow() {
        return UIManager.getColor("glowError");
    }

    private static Color getErrorFocusGlow() {
        return UIManager.getColor("glowFocusError");
    }

    private static Color getFocusGlow() {
        return UIManager.getColor("glowFocus");
    }

    private static Color getWarningGlow() {
        return UIManager.getColor("glowWarning");
    }

    public static void paintOutlineBorder(final Graphics2D g, final int width, final int height, final float arc,
                                          final float bw, final boolean hasFocus, final Outline type) {
        type.setGraphicsColor(g, hasFocus);
        doPaint(g, width, height, arc, bw);
    }

    private static void doPaint(final Graphics2D g, final int width, final int height, final float arc,
                                final float bw) {
        GraphicsContext context = GraphicsUtil.setupStrokePainting(g);

        Shape outerRect = new RoundRectangle2D.Float(0, 0, width, height, arc + bw, arc + bw);
        Shape innerRect = new RoundRectangle2D.Float(bw, bw, width - 2 * bw, height - 2 * bw, arc - bw, arc - bw);
        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        path.append(outerRect, false);
        path.append(innerRect, false);
        g.fill(path);
        context.restore();
    }

    public static void paintFocusBorder(final Graphics2D g, final int width, final int height, final float arc,
                                        final float bw) {
        GraphicsContext config = new GraphicsContext(g);
        g.setComposite(DarkUIUtil.GLOW_ALPHA);
        Outline.focus.setGraphicsColor(g, true);
        doPaint(g, width, height, arc, bw);
        config.restore();
    }

    public static void paintFocusOval(final Graphics2D g, final int x, final int y, final int width, final int height) {
        paintFocusOval(g, (float) x, (float) y, (float) width, (float) height);
    }

    public static void paintFocusOval(final Graphics2D g, final float x, final float y,
                                      final float width, final float height) {
        GraphicsContext config = new GraphicsContext(g);
        g.setComposite(DarkUIUtil.GLOW_ALPHA);
        Outline.focus.setGraphicsColor(g, true);

        float blw = 3.0f;
        Path2D shape = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        shape.append(new Ellipse2D.Float(x - blw, y - blw, width + blw * 2, height + blw * 2), false);
        shape.append(new Ellipse2D.Float(x, y, width, height), false);
        g.fill(shape);
        config.restore();
    }

    public static void paintLineBorder(final Graphics2D g, final float x, final float y,
                                       final float width, final float height, final int arc) {
        GraphicsContext config = GraphicsUtil.setupStrokePainting(g);
        Stroke stroke = g.getStroke();
        float lw = stroke instanceof BasicStroke ? ((BasicStroke) stroke).getLineWidth() : 1;
        g.draw(new RoundRectangle2D.Float(x - lw, y - lw, width + lw, height + lw, arc, arc));
        config.restore();
    }

    public static void fillRoundRect(final Graphics2D g, final float x, final float y,
                                     final float width, final float height, final int arc) {
        Stroke stroke = g.getStroke();
        float lw = stroke instanceof BasicStroke ? ((BasicStroke) stroke).getLineWidth() : 1;
        g.fill(new RoundRectangle2D.Float(x - lw, y - lw, width + lw, height + lw, arc, arc));
    }


    public static Color blendColors(final Color color1, final Color color2, final double percent) {
        if (percent == 1) return color1;
        if (percent == 0) return color2;
        double inverse_percent = 1.0 - percent;
        int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }

    public static void applyInsets(final Rectangle rect, final Insets insets) {
        if (insets != null && rect != null) {
            rect.x += insets.left;
            rect.y += insets.top;
            rect.width -= (insets.right + rect.x);
            rect.height -= (insets.bottom + rect.y);
        }
    }

    public static void removeInsets(final Rectangle rectangle, final Insets insets) {
        if (insets != null) {
            rectangle.x += insets.left;
            rectangle.y += insets.top;
            rectangle.width -= insets.left + insets.right;
            rectangle.height -= insets.top + insets.bottom;
        }

    }

    public static boolean hasFocus(final Component c) {
        if (c.hasFocus()) return true;
        if (c instanceof Window) {
            return hasFocus(c);
        }
        final Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return (owner != null && SwingUtilities.isDescendingFrom(owner, c));
    }

    public static boolean hasFocus(final Window w) {
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (owner == null) return false;
        return SwingUtilities.getWindowAncestor(owner) == w;
    }

    public static int getFocusAcceleratorKeyMask() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            return ((SunToolkit) tk).getFocusAcceleratorKeyMask();
        }
        return ActionEvent.ALT_MASK;
    }


    public static Object getUIOfType(final ComponentUI ui, final Class<?> klass) {
        if (klass.isInstance(ui)) {
            return ui;
        }
        return null;
    }

    public static void doNotCancelPopupSetup(final JComponent component) {
        component.putClientProperty("doNotCancelPopup", DarkPopupMenuUI.HIDE_POPUP_KEY);
        component.putClientProperty("doNotCancelOnScroll", Boolean.TRUE);
    }

    public static boolean isInCell(final Component c) {
        return getParentOfType(CellRendererPane.class, c) != null
                || getParentOfType(TableCellRenderer.class, c) != null
                || getParentOfType(TreeCellRenderer.class, c) != null
                || getParentOfType(CellRenderer.class, c) != null
                || getParentOfType(CellEditor.class, c) != null;
    }

    @SuppressWarnings("unchecked")

    public static <T> T getParentOfType(final Class<? extends T> cls, final Component c) {
        for (Component eachParent = c; eachParent != null; eachParent = eachParent.getParent()) {
            if (cls.isAssignableFrom(eachParent.getClass())) {
                return (T) eachParent;
            }
        }
        return null;
    }


    public static Window getWindow(final Component component) {
        if (component == null) {
            return null;
        }
        return component instanceof Window ? (Window) component : SwingUtilities.getWindowAncestor(component);
    }

    public static boolean isTooltipShowing(final JComponent component) {
        AbstractAction hideTipAction = (AbstractAction) component.getActionMap().get("hideTip");
        return hideTipAction.isEnabled();
    }


    public static MenuElement findEnabledChild(final MenuElement[] e, final MenuElement elem, final boolean forward) {
        for (int i = 0; i < e.length; i++) {
            if (e[i] == elem) {
                return findEnabledChild(e, i, forward);
            }
        }
        return null;
    }

    public static MenuElement findEnabledChild(final MenuElement[] e, final int fromIndex, final boolean forward) {
        MenuElement result;
        if (forward) {
            result = nextEnabledChild(e, fromIndex + 1, e.length - 1);
            if (result == null) result = nextEnabledChild(e, 0, fromIndex - 1);
        } else {
            result = previousEnabledChild(e, fromIndex - 1, 0);
            if (result == null) result = previousEnabledChild(e, e.length - 1, fromIndex + 1);
        }
        return result;
    }


    private static MenuElement nextEnabledChild(final MenuElement[] e, final int fromIndex, final int toIndex) {
        for (int i = fromIndex; i <= toIndex; i++) {
            if (e[i] != null) {
                Component comp = e[i].getComponent();
                if (comp != null
                        && (comp.isEnabled() || UIManager.getBoolean("MenuItem.disabledAreNavigable"))
                        && comp.isVisible()) {
                    return e[i];
                }
            }
        }
        return null;
    }


    private static MenuElement previousEnabledChild(final MenuElement[] e, final int fromIndex, final int toIndex) {
        for (int i = fromIndex; i >= toIndex; i--) {
            if (e[i] != null) {
                Component comp = e[i].getComponent();
                if (comp != null
                        && (comp.isEnabled() || UIManager.getBoolean("MenuItem.disabledAreNavigable"))
                        && comp.isVisible()) {
                    return e[i];
                }
            }
        }
        return null;
    }

    public static void drawRect(final Graphics g, final int x, final int y,
                                final int width, final int height, final int thickness) {
        g.fillRect(x, y, width, thickness);
        g.fillRect(x, y + thickness, thickness, height - 2 * thickness);
        g.fillRect(x + width - thickness, y + thickness, thickness, height - 2 * thickness);
        g.fillRect(x, y + height - thickness, width, thickness);
    }

    public static boolean isOverText(final MouseEvent e, final int index, final JList list) {
        Rectangle bounds = list.getCellBounds(index, index);
        if (!bounds.contains(e.getPoint())) return false;
        //noinspection unchecked
        Component cellRenderer = ((ListCellRenderer<Object>) list.getCellRenderer())
                .getListCellRendererComponent(list, list.getModel().getElementAt(index),
                                              index, false, false);
        if (cellRenderer instanceof JLabel) {
            return isOverText((JLabel) cellRenderer, bounds, e.getPoint());
        } else {
            return true;
        }
    }

    public static boolean isOverText(final JLabel label, final Rectangle bounds, final Point p) {
        textRect.setBounds(0, 0, 0, 0);
        iconRect.setBounds(0, 0, 0, 0);
        SwingUtilities.layoutCompoundLabel(label, label.getFontMetrics(label.getFont()), label.getText(),
                                           label.getIcon(), label.getVerticalAlignment(),
                                           label.getHorizontalAlignment(),
                                           label.getVerticalTextPosition(), label.getHorizontalTextPosition(),
                                           bounds, iconRect, textRect, label.getIconTextGap());
        return textRect.contains(p);
    }

    public static boolean isOverText(final MouseEvent e, final int row, final int column,
                                     final JTable table) {
        Rectangle bounds = table.getCellRect(row, column, false);
        if (!bounds.contains(e.getPoint())) return false;
        Component cellRenderer = table.getCellRenderer(row, column).getTableCellRendererComponent(
                table, table.getValueAt(row, column), false, false, row, column);
        if (cellRenderer instanceof JLabel) {
            return isOverText((JLabel) cellRenderer, bounds, e.getPoint());
        } else {
            return true;
        }
    }

    public static boolean isMenuShortcutKeyDown(final InputEvent event) {
        return (event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static void rotateRectangle(final Rectangle rect) {
        int tmp = rect.x;
        rect.x = rect.y;
        rect.y = tmp;
        tmp = rect.width;
        rect.width = rect.height;
        rect.height = tmp;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static void rotatePoint(final Point p) {
        int tmp = p.x;
        p.x = p.y;
        p.y = tmp;
    }

    public static Insets getBorderInsets(final JComponent comp) {
        Border border = comp.getBorder();
        if (border == null) return new InsetsUIResource(0, 0, 0, 0);
        return border.getBorderInsets(comp);
    }

    public static Point adjustForOrientation(final Point p, final int w, final Component c) {
        if (!c.getComponentOrientation().isLeftToRight()) {
            p.x = c.getWidth() - p.x - w;
        }
        return p;
    }

    public enum Outline {
        error {
            @Override
            public void setGraphicsColor(final Graphics2D g, final boolean focused) {
                if (focused) {
                    g.setColor(getErrorFocusGlow());
                } else {
                    g.setColor(getErrorGlow());
                }
            }
        },

        warning {
            @Override
            public void setGraphicsColor(final Graphics2D g, final boolean focused) {
                g.setColor(getWarningGlow());
            }
        },

        defaultButton {
            @Override
            public void setGraphicsColor(final Graphics2D g, final boolean focused) {
                if (focused) {
                    g.setColor(getFocusGlow());
                }
            }
        },

        focus {
            @Override
            public void setGraphicsColor(final Graphics2D g, final boolean focused) {
                if (focused) {
                    g.setColor(getFocusGlow());
                }
            }
        };

        public abstract void setGraphicsColor(Graphics2D g, boolean focused);
    }
}

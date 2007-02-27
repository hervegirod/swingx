/*
 * $Id$
 *
 * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.jdesktop.swingx.renderer;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.InteractiveTestCase;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.ConditionalHighlighter;
import org.jdesktop.test.AncientSwingTeam;

/**
 * Examples of interplay of Highlighters and extended Swingx renderers.
 * 
 * @author Jeanette Winzenburg
 */
public class HighlighterVisualCheck extends InteractiveTestCase {
    protected Color background = Color.RED;
    protected Color foreground = Color.BLUE;

    public static void main(String args[]) {
//      setSystemLF(true);
      HighlighterVisualCheck test = new HighlighterVisualCheck();
      try {
          test.runInteractiveTests();
//         test.runInteractiveTests(".*Table.*");
      } catch (Exception e) {
          System.err.println("exception when executing interactive tests:");
          e.printStackTrace();
      }
  }
    
    /**
     * Example to highlight against a value/color map.
     * 
     */
    public void interactiveColorValueMappedHighlighter() {
        JXTable table = new JXTable(new AncientSwingTeam());
        // build a quick color lookup to simulate multi-value value-based
        // coloring
        int numberColumn = 3;
        table.toggleSortOrder(numberColumn);
        final Map<Integer, Color> lookup = new HashMap<Integer, Color>();
        Color[] colors = new Color[] { Color.YELLOW, Color.CYAN, Color.MAGENTA,
                Color.GREEN };
        int rowsPerColor = (table.getRowCount() - 5) / colors.length;
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < rowsPerColor; j++) {
                lookup.put((Integer) table.getValueAt(i * rowsPerColor + j,
                        numberColumn), colors[i]);
            }
        }
        table.resetSortOrder();
        ConditionalHighlighter highlighter = new ConditionalHighlighter() {

            @Override
            protected Color computeUnselectedBackground(Component renderer,
                    ComponentAdapter adapter) {
                return lookup.get(adapter.getFilteredValueAt(adapter.row,
                        testColumn));
            }

            /**
             * Traditional: Override test to look-up if the value is mapped to a
             * color.
             */
            @Override
            protected boolean test(ComponentAdapter adapter) {
                if (adapter.isTestable(testColumn)) {
                    Object value = adapter.getFilteredValueAt(adapter.row,
                            testColumn);
                    return lookup.containsKey(value);
                }
                return false;
            }

        };
        highlighter.setTestColumnIndex(numberColumn);
        highlighter.setHighlightColumnIndex(-1);
        table.addHighlighter(highlighter);
        showWithScrollingInFrame(table,
                "conditional highlighter with value-based color mapping");
    }
    
    /**
     * Issue #258-swingx: Background Highlighter must not change custom
     * foreground.
     * <p>
     * 
     * Use SwingX extended default renderer.
     */
    public void interactiveTableCustomRendererColor() {
        TableModel model = new AncientSwingTeam();
        JXTable table = new JXTable(model);
        DefaultTableRenderer renderer = new DefaultTableRenderer();
        renderer.setForeground(foreground);
        renderer.setBackground(background);
        table.addHighlighter(AlternateRowHighlighter.genericGrey);
        table.setDefaultRenderer(Object.class, renderer);
        JXTable nohighlight = new JXTable(model);
        nohighlight.setDefaultRenderer(Object.class, renderer);
        showWithScrollingInFrame(table, nohighlight,
                "ext: custom colored renderer with bg highlighter <--> shared without highl");
    }
    


    /**
     * 
     * Note: in Swingx' context it's not recommended to change 
     * visual renderer properties on the renderer layer - use
     * a conditional highlighter instead. So here is the above 
     * example going the SwingX way.<p>
     * 
     * This is more complicated that it should be ..
     * 
     */
    public void interactiveTableConditionalColorBasedOnValue() {
        TableModel model = new AncientSwingTeam();
        JXTable table = new JXTable(model);
        table.setForeground(Color.GREEN);
//    
//        table.setDefaultRenderer(Object.class, new DefaultTableRenderer());
        ConditionalHighlighter highlighter = new ConditionalHighlighter() {
            @Override
            protected void applyForeground(Component renderer, ComponentAdapter adapter) {
                // needsHighlight/test don't have access to the renderer
                // so we check again here
                Color foregroundx;
                if (testAgainstComponent(renderer, adapter)) {
                    foregroundx = Color.RED;
                    renderer.setForeground(foregroundx);
                } 
            }

            @Override
            public Component doHighlight(Component renderer, ComponentAdapter adapter) {
                if (adapter.isSelected()) return renderer;
                return super.doHighlight(renderer, adapter);
            }

            private boolean testAgainstComponent(Component renderer, ComponentAdapter adapter) {
                if (!(renderer instanceof JLabel)) return false;
                String text = ((JLabel) renderer).getText();
                 return text.contains("y");
            }

            /**
             * Overridden to always return true. We test against
             * the text property of the label.
             */
            @Override
            protected boolean test(ComponentAdapter adapter) {
                return true;
            }
            
        };
        highlighter.setHighlightColumnIndex(-1);
        highlighter.setTestColumnIndex(-1);
        table.addHighlighter(AlternateRowHighlighter.genericGrey);
        table.addHighlighter(highlighter);
        JXTable nohighlight = new JXTable(model);
        nohighlight.setForeground(table.getForeground());
//        nohighlight.setDefaultRenderer(Object.class, new DefaultTableRenderer());
        nohighlight.addHighlighter(highlighter);
        showWithScrollingInFrame(table, nohighlight,
                "value-based rendering by ConditionalHighlighter");
    }

//------------------ helper

}

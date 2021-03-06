package org.hunmr.acejump.marker;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import org.hunmr.options.PluginConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class MarkersPanel extends JComponent {
    public static final Color PANEL_BACKGROUND_COLOR = new Color(128, 138, 142);
    public Editor _editor;
    private MarkerCollection _markerCollection;

    final PluginConfig _config = ServiceManager.getService(PluginConfig.class);

    public MarkersPanel(Editor editor, MarkerCollection markerCollection) {
        _editor = editor;
        _markerCollection = markerCollection;
        setupLocationAndBoundsOfPanel(editor);
    }

    private boolean isLineEndOffset(Marker marker) {
        int offset = marker.getOffset().offset;
        Editor editor = marker.getOffset().editor;

        int lineA = editor.getDocument().getLineNumber(offset);
        int lineEndOffset = editor.getDocument().getLineEndOffset(lineA);

        return offset == lineEndOffset;
    }

    @Override
    public void paint(Graphics g) {
        Font font = _editor.getColorsScheme().getFont(EditorFontType.BOLD);
        FontMetrics fontMetrics = _editor.getContentComponent().getFontMetrics(font);

        g.setFont(font);
        drawPanelBackground(g);

        HashSet<JOffset> firstJumpOffsets = new HashSet<JOffset>();
        for (Marker marker : _markerCollection.values()) {
            if (marker.getOffset().editor != _editor) continue;

            for (JOffset offset : marker.getOffsets()) {
                firstJumpOffsets.add(offset);

                Rectangle2D fontRect = fontMetrics.getStringBounds(String.valueOf(marker.getMarkerChar()), g);
                drawBackground(g, __x(offset), __y(offset), _config.getFirstJumpBackground(), fontRect);
                drawMarkerChar(g, __x(offset), __y(offset) + font.getSize() * 0.9, marker.getMarkerChar(), _config.getFirstJumpForeground());
            }
        }

        for (Marker marker : _markerCollection.values()) {
            if (marker.getOffset().editor != _editor)                                                   continue;
            if (marker.getMarker().length() == 1 || marker.isMappingToMultipleOffset())                 continue;
            if (isAlreadyHasFirstJumpCharInPlace(firstJumpOffsets, marker) && !isLineEndOffset(marker)) continue;

            for (JOffset offset : marker.getOffsets()) {
                Rectangle2D fontRect = fontMetrics.getStringBounds(String.valueOf(marker.getMarker().charAt(1)), g);
                drawBackground(g, __x(offset) + fontRect.getWidth(), __y(offset), _config.getSecondJumpBackground(), fontRect);
                drawMarkerChar(g, __x(offset) + fontRect.getWidth(), __y(offset) + font.getSize() * 0.9, marker.getMarker().charAt(1), _config.getSecondJumpForeground());
            }
        }

        super.paint(g);
    }

    private boolean isAlreadyHasFirstJumpCharInPlace(HashSet<JOffset> firstJumpOffsets, Marker marker) {
        JOffset o = new JOffset(marker.getOffset().editor, marker.getOffset().offset + 1);
        return firstJumpOffsets.contains(o);
    }

    private double __y(JOffset offset) {
        Point parentLocation = _editor.getContentComponent().getLocation();
        return getVisiblePosition(offset).getY() + parentLocation.getY();
    }

    private double __x(JOffset offset) {
        Point parentLocation = _editor.getContentComponent().getLocation();
        return getVisiblePosition(offset).getX() + parentLocation.getX();
    }

    private void drawMarkerChar(Graphics g, double x, double y, char markerChar, Color firstJumpForeground) {
        g.setColor(firstJumpForeground);
        ((Graphics2D)g).drawString(String.valueOf(markerChar), (float)x, (float)y);
    }

    private void drawBackground(Graphics g, double x, double y, Color firstJumpBackground, Rectangle2D fontRect) {
        g.setColor(firstJumpBackground);
        g.fillRect((int)x, (int)y, (int) (fontRect.getWidth() * 1.02), (int) (fontRect.getHeight() * 1.08));
    }

    private Point getVisiblePosition(JOffset offset) {
        return offset.editor.visualPositionToXY(offset.editor.offsetToVisualPosition(offset.offset));
    }

    private void drawPanelBackground(Graphics g) {
        ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(PANEL_BACKGROUND_COLOR);
        g.fillRect(0, 0, (int) this.getBounds().getWidth(), (int) this.getBounds().getHeight());
        ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    private void setupLocationAndBoundsOfPanel(Editor editor) {
        this.setLocation(0, 0);
        Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
        JComponent parent = editor.getContentComponent();
        int x = (int) (parent.getLocation().getX() + visibleArea.getX() + editor.getScrollingModel().getHorizontalScrollOffset());
        this.setBounds(x, (int) (visibleArea.getY()), (int) visibleArea.getWidth(), (int) visibleArea.getHeight());
    }
}

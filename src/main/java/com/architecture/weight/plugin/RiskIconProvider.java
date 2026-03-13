package com.architecture.weight.plugin;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.ColorsIcon;

import javax.swing.*;
import java.awt.*;

/**
 * Provides color-coded gutter icons based on the risk score.
 * <p>
 * Color thresholds:
 * <ul>
 *     <li>Score > 50 → Red (High Risk)</li>
 *     <li>Score > 20 → Orange (Medium Risk)</li>
 *     <li>Score ≤ 20 → Green (Low Risk)</li>
 * </ul>
 */
public class RiskIconProvider {

    /**
     * Returns a color-coded icon based on the risk score.
     *
     * @param riskScore the calculated risk score
     * @return a 12px colored icon (Red, Orange, or Green)
     */
    public static Icon getIcon(double riskScore) {
        if (riskScore > 50.0) {
            return new ColorsIcon(12, new Color[]{JBColor.RED});
        } else if (riskScore > 20.0) {
            return new ColorsIcon(12, new Color[]{JBColor.ORANGE});
        } else {
            return new ColorsIcon(12, new Color[]{JBColor.GREEN});
        }
    }
}

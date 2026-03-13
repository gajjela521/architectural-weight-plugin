package com.architecture.weight.plugin;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides gutter icons for Java classes showing their architectural risk score.
 * <p>
 * The risk score is calculated based on:
 * <ul>
 *     <li>Inbound dependencies (how many other classes reference this class) — weighted at 0.7</li>
 *     <li>Git commit frequency (how often this file has been changed in the last 6 months) — weighted at 0.3</li>
 * </ul>
 * Formula: RiskScore = (InboundDependencies × 0.7) + (CommitFrequency × 0.3)
 */
public class ArchitecturalWeightLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element.getParent();
            double riskScore = RiskCalculator.calculateRisk(psiClass);
            String tooltip = RiskCalculator.getTooltip(psiClass, riskScore);

            return new LineMarkerInfo<>(
                    element,
                    element.getTextRange(),
                    RiskIconProvider.getIcon(riskScore),
                    (Function<PsiElement, String>) e -> tooltip,
                    (GutterIconNavigationHandler<PsiElement>) (mouseEvent, elt) ->
                            RiskCalculator.showPopup(mouseEvent, tooltip),
                    GutterIconRenderer.Alignment.RIGHT
            );
        }
        return null;
    }
}

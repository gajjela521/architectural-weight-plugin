package com.architecture.weight.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.awt.event.MouseEvent;

/**
 * Calculates the architectural risk score for a given Java class.
 * <p>
 * Risk Score = (InboundDependencies × 0.7) + (CommitFrequency × 0.3)
 * <p>
 * Thresholds:
 * <ul>
 *     <li>Score > 50 → High Risk (Red)</li>
 *     <li>Score > 20 → Medium Risk (Orange)</li>
 *     <li>Score ≤ 20 → Low Risk (Green)</li>
 * </ul>
 */
public class RiskCalculator {

    private static final Logger LOG = Logger.getInstance(RiskCalculator.class);

    /**
     * Calculates the risk score for a PsiClass based on inbound dependencies and commit frequency.
     *
     * @param psiClass the Java class to analyze
     * @return the calculated risk score
     */
    public static double calculateRisk(PsiClass psiClass) {
        Project project = psiClass.getProject();
        VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            return 0.0;
        }

        int inboundDeps = getInboundDependencies(psiClass, project);
        int commitFreq = getCommitFrequency(virtualFile, project);

        return (inboundDeps * 0.7) + (commitFreq * 0.3);
    }

    /**
     * Counts the number of references to this class across the entire project.
     */
    private static int getInboundDependencies(PsiClass psiClass, Project project) {
        try {
            return ReferencesSearch.search(psiClass, GlobalSearchScope.projectScope(project))
                    .findAll()
                    .size();
        } catch (Exception e) {
            LOG.warn("Failed to find inbound dependencies", e);
            return 0;
        }
    }

    /**
     * Counts the number of Git commits that touched this file in the last 6 months.
     */
    private static int getCommitFrequency(VirtualFile virtualFile, Project project) {
        try {
            GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
            GitRepository repo = (GitRepository) repoManager.getRepositoryForFileQuick(virtualFile);
            if (repo == null) {
                return 0;
            }

            return GitHistoryUtils.history(project, repo.getRoot(),
                    "--since=6.months", "--", virtualFile.getPath()).size();
        } catch (Exception e) {
            LOG.warn("Error fetching git history", e);
            return 0;
        }
    }

    /**
     * Generates a human-readable tooltip describing the risk level.
     *
     * @param psiClass  the class being analyzed
     * @param riskScore the calculated risk score
     * @return formatted tooltip string
     */
    public static String getTooltip(PsiClass psiClass, double riskScore) {
        Project project = psiClass.getProject();
        VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();
        int inboundDeps = getInboundDependencies(psiClass, project);
        int commitFreq = virtualFile != null ? getCommitFrequency(virtualFile, project) : 0;

        String riskLevel;
        if (riskScore > 50.0) {
            riskLevel = "High Risk";
        } else if (riskScore > 20.0) {
            riskLevel = "Medium Risk";
        } else {
            riskLevel = "Low Risk";
        }

        return String.format(
                "%s: This service is used by %d components and changes %d times in last 6 months.",
                riskLevel, inboundDeps, commitFreq
        );
    }

    /**
     * Shows a balloon popup with the risk information when the user clicks the gutter icon.
     *
     * @param mouseEvent the mouse event triggering the popup
     * @param tooltip    the tooltip text to display
     */
    public static void showPopup(MouseEvent mouseEvent, String tooltip) {
        if (mouseEvent == null) {
            return;
        }

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(tooltip, null, JBColor.background(), null)
                .setFadeoutTime(5000)
                .createBalloon()
                .show(new RelativePoint(mouseEvent), Balloon.Position.above);
    }
}
